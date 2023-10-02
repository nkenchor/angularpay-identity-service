package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.adapters.outbound.CipherServicePortAdapter;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.RedisAdapter;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.JwtHelper;
import io.angularpay.identity.models.*;
import io.angularpay.identity.ports.outbound.GoogleReCaptchaV3Port;
import io.angularpay.identity.security.BruteForceGuard;
import io.angularpay.identity.util.AccessTokenUtil;
import io.angularpay.identity.util.PasswordUtil;
import io.angularpay.identity.util.TokenGenerationParameters;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static io.angularpay.identity.common.Constants.ERROR_SOURCE;
import static io.angularpay.identity.exceptions.ErrorCode.*;
import static io.angularpay.identity.helpers.CommandHelper.*;
import static io.angularpay.identity.helpers.Helper.getTokenIfExists;
import static io.angularpay.identity.helpers.Helper.maskEmail;

@Service
public class AuthenticatePlatformAdminUserCommand extends AbstractCommand<GenericAuthenticateUserCommandRequest, AuthenticationResponse>
        implements SensitiveDataCommand<GenericAuthenticateUserCommandRequest>, BruteForceGuardCommand {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final PasswordUtil passwordUtil;
    private final CipherServicePortAdapter cipherServicePortAdapter;
    private final AccessTokenUtil accessTokenUtil;
    private final JwtHelper jwtHelper;
    private final RedisAdapter redisAdapter;
    private final BruteForceGuard bruteForceGuard;
    private final GoogleReCaptchaV3Port googleReCaptchaV3Port;
    private final AngularPayConfiguration configuration;

    public AuthenticatePlatformAdminUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            PasswordUtil passwordUtil,
            CipherServicePortAdapter cipherServicePortAdapter,
            AccessTokenUtil accessTokenUtil,
            JwtHelper jwtHelper,
            RedisAdapter redisAdapter,
            BruteForceGuard bruteForceGuard,
            GoogleReCaptchaV3Port googleReCaptchaV3Port,
            AngularPayConfiguration configuration) {
        super("AuthenticatePlatformAdminUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.passwordUtil = passwordUtil;
        this.cipherServicePortAdapter = cipherServicePortAdapter;
        this.accessTokenUtil = accessTokenUtil;
        this.jwtHelper = jwtHelper;
        this.redisAdapter = redisAdapter;
        this.bruteForceGuard = bruteForceGuard;
        this.googleReCaptchaV3Port = googleReCaptchaV3Port;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(GenericAuthenticateUserCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected AuthenticationResponse handle(GenericAuthenticateUserCommandRequest request) {
        boolean success = !this.configuration.getGoogleRecaptcha().isEnabled() || isRecaptchaSuccess(request.getUserLoginApiModel().getRecaptcha());
        if (!success) {
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(AUTHORIZATION_ERROR)
                    .message(AUTHORIZATION_ERROR.getDefaultMessage())
                    .build();
        }

        UserIdentity found = getRequestByUsernameAndPasswordOrThrow(
                this.mongoAdapter,
                request.getUserLoginApiModel().getUsername(),
                this.passwordUtil.toHashedPassword(request.getUserLoginApiModel().getPassword())
        );
        validUserStatusOrThrowUnauthorized(found);
        validUserRoleOrThrowUnauthorized(found);

        Map<String, String> existingJtis = getTokenIfExists(found);
        revokeExistingTokens(this.redisAdapter, existingJtis);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", found.getUsername());
        headers.put("x-angularpay-device-id", request.getAuthenticatedUser().getDeviceId());
        headers.put("x-angularpay-user-reference", found.getUserReference());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());

        Optional<CreateCipherResponseModel> cipherResponse = this.cipherServicePortAdapter.createCipher(headers);

        if (cipherResponse.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(CIPHER_SERVICE_ERROR)
                    .message(CIPHER_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }

        Optional<Token> tokenOptional = this.accessTokenUtil.generateToken(
                TokenGenerationParameters.builder()
                        .userReference(found.getUserReference())
                        .username(found.getUsername())
                        .deviceId(request.getAuthenticatedUser().getDeviceId())
                        .roles(found.getRoles())
                        .build()
        );
        if (tokenOptional.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(TOKEN_GENERATION_ERROR)
                    .message(TOKEN_GENERATION_ERROR.getDefaultMessage())
                    .build();
        }

        Token token = tokenOptional.get();
        updateLoginActivity(this.jwtHelper, found, token);
        this.mongoAdapter.updateUser(found);
        return buildAuthenticationResponse(cipherResponse.get(), token);
    }

    private boolean isRecaptchaSuccess(GoogleReCaptchaRequest reCaptchaRequest) {
        Optional<GoogleReCaptchaResponse> optional = this.googleReCaptchaV3Port.recapatcha(GoogleReCaptchaRequest.builder()
                .recaptchaToken(reCaptchaRequest.getRecaptchaToken())
                .actionName(reCaptchaRequest.getActionName())
                .build());

        if (optional.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(AUTHORIZATION_ERROR)
                    .message(AUTHORIZATION_ERROR.getDefaultMessage())
                    .build();
        }

        GoogleReCaptchaResponse googleReCaptchaResponse = optional.get();

        String captchaRequestActionName = reCaptchaRequest.getActionName();
        float expectedScore = configuration.getGoogleRecaptcha().getThreshold();
        String captchaResponseActionName = googleReCaptchaResponse.getAction();
        float scoreFromResponse = googleReCaptchaResponse.getScore();
        boolean verificationResultOk = googleReCaptchaResponse.isSuccess();

        return captchaResponseActionName.equals(captchaRequestActionName) && verificationResultOk && scoreFromResponse > expectedScore;
    }


    @Override
    protected List<ErrorObject> validate(GenericAuthenticateUserCommandRequest request) {
        List<ErrorObject> errors = new ArrayList<>();
        if (Objects.isNull(request.getUserLoginApiModel().getRecaptcha())) {
            errors.add(ErrorObject.builder()
                    .code(VALIDATION_ERROR)
                    .message("recaptcha must not be null")
                    .source(ERROR_SOURCE)
                    .build());
            errors.addAll(this.validator.validate(request));
            return errors;
        }
        if (!StringUtils.hasText(request.getUserLoginApiModel().getRecaptcha().getRecaptchaToken())) {
            errors.add(ErrorObject.builder()
                    .code(VALIDATION_ERROR)
                    .message("recaptcha.recaptcha_token must not be empty for WEB_PLATFORM_ADMIN_LOGIN request")
                    .source(ERROR_SOURCE)
                    .build());
        }
        if (!StringUtils.hasText(request.getUserLoginApiModel().getRecaptcha().getActionName())) {
            errors.add(ErrorObject.builder()
                    .code(VALIDATION_ERROR)
                    .message("recaptcha.action_name must not be empty for WEB_PLATFORM_ADMIN_LOGIN request")
                    .source(ERROR_SOURCE)
                    .build());
        }
        errors.addAll(this.validator.validate(request));
        return errors;
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public GenericAuthenticateUserCommandRequest mask(GenericAuthenticateUserCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode userLoginApiModel = node.get("userLoginApiModel");
            ((ObjectNode) userLoginApiModel).put("username", maskEmail(raw.getUserLoginApiModel().getUsername()));
            ((ObjectNode) userLoginApiModel).put("password", "*****");
            return mapper.treeToValue(node, GenericAuthenticateUserCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }

    @Override
    public void onLoginSuccess(String clientIp) {
        this.bruteForceGuard.onLoginSuccess(clientIp);
    }

    @Override
    public void onLoginFailure(String clientIp) {
        this.bruteForceGuard.onLoginFailure(clientIp);
    }

    @Override
    public boolean isBlocked(String clientIp) {
        return this.bruteForceGuard.isBlocked(clientIp);
    }
}
