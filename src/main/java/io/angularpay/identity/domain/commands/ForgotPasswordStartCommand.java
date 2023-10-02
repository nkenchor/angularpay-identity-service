package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.NotificationServiceAdapter;
import io.angularpay.identity.adapters.outbound.OtpServiceAdapter;
import io.angularpay.identity.adapters.outbound.UserConfigurationServiceAdapter;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.PasswordReset;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.*;
import io.angularpay.identity.ports.outbound.NotificationServicePort;
import io.angularpay.identity.ports.outbound.OtpServicePort;
import io.angularpay.identity.ports.outbound.UserConfigurationServicePort;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.angularpay.identity.common.Constants.OTP_NOTIFICATION_SENDER;
import static io.angularpay.identity.domain.PasswordResetStatus.DEFAULT_STATE;
import static io.angularpay.identity.domain.PasswordResetStatus.REQUESTED;
import static io.angularpay.identity.domain.PasswordResetType.FORGOT_PASSWORD;
import static io.angularpay.identity.exceptions.ErrorCode.*;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.getsRequestByUsernameOrThrow;
import static io.angularpay.identity.helpers.Helper.maskEmail;
import static io.angularpay.identity.models.NotificationChannel.EMAIL;
import static io.angularpay.identity.models.NotificationChannel.SMS;
import static io.angularpay.identity.models.NotificationType.INSTANT;

@Slf4j
@Service
public class ForgotPasswordStartCommand extends AbstractCommand<ForgotPasswordStartCommandRequest, ResourceReferenceResponse>
        implements SensitiveDataCommand<ForgotPasswordStartCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final OtpServicePort otpServiceAdapter;
    private final NotificationServicePort notificationServiceAdapter;
    private final UserConfigurationServicePort userConfigurationServicePort;
    private final AngularPayConfiguration configuration;

    public ForgotPasswordStartCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            OtpServiceAdapter otpServiceAdapter,
            NotificationServiceAdapter notificationServiceAdapter,
            UserConfigurationServiceAdapter userConfigurationServiceAdapter,
            AngularPayConfiguration configuration) {
        super("ForgotPasswordStartCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.otpServiceAdapter = otpServiceAdapter;
        this.notificationServiceAdapter = notificationServiceAdapter;
        this.userConfigurationServicePort = userConfigurationServiceAdapter;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(ForgotPasswordStartCommandRequest request) {
        return request.getAuthenticatedUser().getDeviceId();
    }

    @Override
    protected ResourceReferenceResponse handle(ForgotPasswordStartCommandRequest request) {
        UserIdentity found = getsRequestByUsernameOrThrow(this.mongoAdapter, request.getForgotPasswordStartApiModel().getUsername());

        if (!found.getDevices().contains(request.getAuthenticatedUser().getDeviceId())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(USER_DEVICE_MISMATCH_ERROR)
                    .message(USER_DEVICE_MISMATCH_ERROR.getDefaultMessage())
                    .build();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", request.getForgotPasswordStartApiModel().getUsername());
        headers.put("x-angularpay-device-id", request.getAuthenticatedUser().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());
        headers.put("x-angularpay-user-reference", found.getUserReference());

        UserProfileResponseModel userProfile = userConfigurationServicePort.getUserProfile(found.getUserReference(), headers)
                .orElseThrow(() -> CommandException.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .errorCode(USERCONFIG_SERVICE_ERROR)
                        .message(USERCONFIG_SERVICE_ERROR.getDefaultMessage())
                        .build()
                );

        CreateOtpResponseApiModel otpResponse = this.otpServiceAdapter.createOtp(CreateOtpRequestApiModel.builder()
                        .deviceReference(request.getAuthenticatedUser().getDeviceId())
                        .userReference(found.getUserReference())
                        .strict(true)
                        .build(),
                headers
        ).orElseThrow(() -> CommandException.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode(OTP_SERVICE_ERROR)
                .message(OTP_SERVICE_ERROR.getDefaultMessage())
                .build()
        );

        this.sendOtpNotificationOrThrow(userProfile, otpResponse.getCode(), headers);

        found.setPasswordReset(PasswordReset.builder()
                .type(FORGOT_PASSWORD)
                .status(REQUESTED)
                .build());

        UserIdentity response = this.mongoAdapter.updateUser(found);

        String userReference = response.getUserReference();
        Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    UserIdentity userIdentity = getRequestByReferenceOrThrow(this.mongoAdapter, userReference);
                    if (userIdentity.getPasswordReset().getStatus() == REQUESTED) {
                        userIdentity.setPasswordReset(PasswordReset.builder()
                                .type(FORGOT_PASSWORD)
                                .status(DEFAULT_STATE)
                                .build());
                        mongoAdapter.updateUser(userIdentity);
                    }
                },
                configuration.getPasswordResetTTL().getConfirmationTTLSeconds(),
                TimeUnit.SECONDS
        );

        return new ResourceReferenceResponse(found.getUserReference());
    }

    @Override
    protected List<ErrorObject> validate(ForgotPasswordStartCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    private void sendOtpNotificationOrThrow(UserProfileResponseModel userProfileResponseModel, String otp, Map<String, String> headers) {
//        String message = String.format(OTP_NOTIFICATION_TEMPLATE, otp);
        // TODO: remove when we register with carriers
        String message = "Test OTP: " + otp;

        List<Supplier<Optional<SendNotificationResponseApiModel>>> notifications = Arrays.asList(
                () -> this.notificationServiceAdapter.sendNotification(
                        SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(SMS)
                                .type(INSTANT)
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(userProfileResponseModel.getPhone())
                                .message(message)
                                .build(),
                        headers),
                () -> this.notificationServiceAdapter.sendNotification(
                        SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(EMAIL)
                                .type(INSTANT)
                                .subject("AngularPay Forgot Password OTP")
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(userProfileResponseModel.getEmail())
                                .message(message)
                                .build(),
                        headers)
        );

        if (notifications.parallelStream().map(Supplier::get).allMatch(Optional::isEmpty)) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(NOTIFICATION_SERVICE_ERROR)
                    .message(NOTIFICATION_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }
    }

    @Override
    public ForgotPasswordStartCommandRequest mask(ForgotPasswordStartCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode createSignupRequest = node.get("forgotPasswordStartApiModel");
            ((ObjectNode) createSignupRequest).put("username", maskEmail(raw.getForgotPasswordStartApiModel().getUsername()));
            return mapper.treeToValue(node, ForgotPasswordStartCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
