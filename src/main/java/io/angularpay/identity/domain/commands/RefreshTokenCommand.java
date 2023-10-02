package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.CipherServicePortAdapter;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.RedisAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.JwtHelper;
import io.angularpay.identity.models.*;
import io.angularpay.identity.util.AccessTokenUtil;
import io.angularpay.identity.util.TokenGenerationParameters;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.angularpay.identity.exceptions.ErrorCode.*;
import static io.angularpay.identity.helpers.CommandHelper.*;

@Service
public class RefreshTokenCommand extends AbstractCommand<RefreshTokenCommandRequest, AuthenticationResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CipherServicePortAdapter cipherServicePortAdapter;
    private final AccessTokenUtil accessTokenUtil;
    private final RedisAdapter redisAdapter;
    private final JwtHelper jwtHelper;

    public RefreshTokenCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CipherServicePortAdapter cipherServicePortAdapter,
            AccessTokenUtil accessTokenUtil,
            RedisAdapter redisAdapter,
            JwtHelper jwtHelper) {
        super("RefreshTokenCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.cipherServicePortAdapter = cipherServicePortAdapter;
        this.accessTokenUtil = accessTokenUtil;
        this.redisAdapter = redisAdapter;
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected String getResourceOwner(RefreshTokenCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected AuthenticationResponse handle(RefreshTokenCommandRequest request) {
        JwtPayload jwtPayload = this.jwtHelper.getJwtPayload(request.getRefreshTokensApiModel().getRefreshToken());

        if (!this.accessTokenUtil.verifyToken(request.getRefreshTokensApiModel().getRefreshToken())) {
            this.redisAdapter.removeIfExpired(jwtPayload.getJti());
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(INVALID_CREDENTIALS)
                    .message(INVALID_CREDENTIALS.getDefaultMessage())
                    .build();
        }

        UserIdentity found = getRequestByUsernameOrThrow(
                this.mongoAdapter,
                jwtPayload.getSub()
        );
        validStatusAndDeviceOrThrow(request.getAuthenticatedUser().getDeviceId(), found);

        if (this.redisAdapter.isTokenRevoked(jwtPayload.getJti())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .errorCode(INVALID_CREDENTIALS)
                    .message(INVALID_CREDENTIALS.getDefaultMessage())
                    .build();
        }

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

    @Override
    protected List<ErrorObject> validate(RefreshTokenCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}
