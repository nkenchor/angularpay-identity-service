package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.AuthenticationResponse;
import io.angularpay.identity.models.GenericAuthenticateUserCommandRequest;
import io.angularpay.identity.models.Token;
import io.angularpay.identity.security.BruteForceGuard;
import io.angularpay.identity.util.AccessTokenUtil;
import io.angularpay.identity.util.TokenGenerationParameters;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.angularpay.identity.domain.Role.ROLE_FORGOT_PASSWORD_USER;
import static io.angularpay.identity.exceptions.ErrorCode.INVALID_CREDENTIALS;
import static io.angularpay.identity.exceptions.ErrorCode.TOKEN_GENERATION_ERROR;
import static io.angularpay.identity.helpers.CommandHelper.buildAuthenticationResponse;

@Service
public class AuthenticateForgotPasswordUserCommand extends AbstractCommand<GenericAuthenticateUserCommandRequest, AuthenticationResponse>
        implements SensitiveDataCommand<GenericAuthenticateUserCommandRequest>, BruteForceGuardCommand {

    private final DefaultConstraintValidator validator;
    private final AngularPayConfiguration configuration;
    private final AccessTokenUtil accessTokenUtil;
    private final BruteForceGuard bruteForceGuard;

    public AuthenticateForgotPasswordUserCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            AngularPayConfiguration configuration,
            AccessTokenUtil accessTokenUtil,
            BruteForceGuard bruteForceGuard) {
        super("AuthenticateForgotPasswordUserCommand", mapper);
        this.validator = validator;
        this.configuration = configuration;
        this.accessTokenUtil = accessTokenUtil;
        this.bruteForceGuard = bruteForceGuard;
    }

    @Override
    protected String getResourceOwner(GenericAuthenticateUserCommandRequest request) {
        return request.getAuthenticatedUser().getDeviceId();
    }

    @Override
    protected AuthenticationResponse handle(GenericAuthenticateUserCommandRequest request) {
        if (!request.getUserLoginApiModel().getUsername().equalsIgnoreCase(request.getAuthenticatedUser().getDeviceId())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(INVALID_CREDENTIALS)
                    .message(INVALID_CREDENTIALS.getDefaultMessage())
                    .build();
        }

        if (!request.getUserLoginApiModel().getPassword().equalsIgnoreCase(this.configuration.getSecurity().getPublicKey())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(INVALID_CREDENTIALS)
                    .message(INVALID_CREDENTIALS.getDefaultMessage())
                    .build();
        }

        Optional<Token> tokenOptional = this.accessTokenUtil.generateToken(
                TokenGenerationParameters.builder()
                        .userReference(request.getUserLoginApiModel().getUsername())
                        .username(request.getUserLoginApiModel().getUsername())
                        .deviceId(request.getAuthenticatedUser().getDeviceId())
                        .roles(Collections.singleton(ROLE_FORGOT_PASSWORD_USER))
                        .build()
        );
        if (tokenOptional.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(TOKEN_GENERATION_ERROR)
                    .message(TOKEN_GENERATION_ERROR.getDefaultMessage())
                    .build();
        }

        return buildAuthenticationResponse(tokenOptional.get());
    }


    @Override
    protected List<ErrorObject> validate(GenericAuthenticateUserCommandRequest request) {
        return this.validator.validate(request);
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
