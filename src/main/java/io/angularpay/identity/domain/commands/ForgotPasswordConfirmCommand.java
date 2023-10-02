package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.OtpServiceAdapter;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.PasswordReset;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.ForgotPasswordConfirmCommandRequest;
import io.angularpay.identity.models.ValidateOtpRequestApiModel;
import io.angularpay.identity.models.ValidateOtpResponseApiModel;
import io.angularpay.identity.ports.outbound.OtpServicePort;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.angularpay.identity.domain.PasswordResetStatus.*;
import static io.angularpay.identity.domain.PasswordResetType.FORGOT_PASSWORD;
import static io.angularpay.identity.exceptions.ErrorCode.INVALID_OTP_ERROR;
import static io.angularpay.identity.exceptions.ErrorCode.OTP_SERVICE_ERROR;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class ForgotPasswordConfirmCommand extends AbstractCommand<ForgotPasswordConfirmCommandRequest, Void> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final OtpServicePort otpServiceAdapter;
    private final AngularPayConfiguration configuration;

    public ForgotPasswordConfirmCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            OtpServiceAdapter otpServiceAdapter,
            AngularPayConfiguration configuration) {
        super("ForgotPasswordConfirmCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.otpServiceAdapter = otpServiceAdapter;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(ForgotPasswordConfirmCommandRequest request) {
        return request.getAuthenticatedUser().getDeviceId();
    }

    @Override
    protected Void handle(ForgotPasswordConfirmCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", found.getUsername());
        headers.put("x-angularpay-device-id", request.getAuthenticatedUser().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());
        headers.put("x-angularpay-user-reference", found.getUserReference());

        Optional<ValidateOtpResponseApiModel> otpResponse = this.otpServiceAdapter.validateOtp(
                ValidateOtpRequestApiModel.builder()
                        .deviceReference(request.getAuthenticatedUser().getDeviceId())
                        .userReference(found.getUserReference())
                        .code(request.getForgotPasswordCofrimApiModel().getOtp())
                        .build(),
                headers
        );
        if (otpResponse.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(OTP_SERVICE_ERROR)
                    .message(OTP_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }
        if (!otpResponse.get().isValid()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(INVALID_OTP_ERROR)
                    .message(INVALID_OTP_ERROR.getDefaultMessage())
                    .build();
        }

        found.setPasswordReset(PasswordReset.builder()
                .type(FORGOT_PASSWORD)
                .status(CONFIRMED)
                .build());

        UserIdentity response = this.mongoAdapter.updateUser(found);

        String userReference = response.getUserReference();
        Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    UserIdentity userIdentity = getRequestByReferenceOrThrow(this.mongoAdapter, userReference);
                    if (userIdentity.getPasswordReset().getStatus() != COMPLETED) {
                        userIdentity.setPasswordReset(PasswordReset.builder()
                                .type(FORGOT_PASSWORD)
                                .status(DEFAULT_STATE)
                                .build());
                        mongoAdapter.updateUser(userIdentity);
                    }
                },
                configuration.getPasswordResetTTL().getPasswordResetTTLSeconds(),
                TimeUnit.SECONDS
        );

        return null;
    }

    @Override
    protected List<ErrorObject> validate(ForgotPasswordConfirmCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

}
