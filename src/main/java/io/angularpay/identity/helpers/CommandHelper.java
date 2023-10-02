package io.angularpay.identity.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.RedisAdapter;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorCode;
import io.angularpay.identity.models.*;
import io.angularpay.identity.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.angularpay.identity.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    private final MongoAdapter mongoAdapter;
    private final ObjectMapper mapper;
    private final AngularPayConfiguration configuration;
    private final PasswordUtil passwordUtil;

    public String getRequestOwner(String userReference) {
        UserIdentity found = this.mongoAdapter.findUserByUserReference(userReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        return found.getUserReference();
    }

    private static CommandException commandException(HttpStatus status, ErrorCode errorCode) {
        return CommandException.builder()
                .status(status)
                .errorCode(errorCode)
                .message(errorCode.getDefaultMessage())
                .build();
    }

    public <T> void addItemToCollection(UserIdentity userIdentity, T newProperty, Supplier<Set<T>> collectionGetter, Consumer<Set<T>> collectionSetter) {
        if (CollectionUtils.isEmpty(collectionGetter.get())) {
            collectionSetter.accept(new HashSet<>());
        }
        collectionGetter.get().add(newProperty);
        this.mongoAdapter.updateUser(userIdentity);
    }

    public <T> void updateCollection(UserIdentity userIdentity, Set<T> newList, Supplier<Set<T>> collectionGetter, Consumer<Set<T>> collectionSetter) {
        if (CollectionUtils.isEmpty(collectionGetter.get())) {
            collectionSetter.accept(new HashSet<>());
        }
        collectionGetter.get().addAll(newList);
        this.mongoAdapter.updateUser(userIdentity);
    }

    public <T> AssociatedReferenceResponse updateProperty(UserIdentity userIdentity, Supplier<T> getter, Consumer<T> setter) {
        setter.accept(getter.get());
        UserIdentity response = this.mongoAdapter.updateUser(userIdentity);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    public <T> String toJsonString(T t) throws JsonProcessingException {
        return this.mapper.writeValueAsString(t);
    }

    public static UserIdentity getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String userReference) {
        return mongoAdapter.findUserByUserReference(userReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

    public static UserIdentity getRequestByUsernameAndPasswordOrThrow(MongoAdapter mongoAdapter, String username, String password) {
        return mongoAdapter.findUserByUsernameAndPassword(
                username, password
        ).orElseThrow(
                () -> commandException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS)
        );
    }

    public static UserIdentity getRequestByUsernameOrThrow(MongoAdapter mongoAdapter, String username) {
        return mongoAdapter.findUserByUsername(username).orElseThrow(
                () -> commandException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS)
        );
    }

    public static void validUserStatusOrThrow(UserIdentity found) {
        if (found.isDeleted() || !found.isEnabled()) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, INVALID_USER_STATUS_ERROR);
        }
    }

    public static void validUserStatusOrThrowUnauthorized(UserIdentity found) {
        if (found.isDeleted() || !found.isEnabled()) {
            throw commandException(HttpStatus.UNAUTHORIZED, INVALID_USER_STATUS_ERROR);
        }
    }

    public static void validUserRoleOrThrowUnauthorized(UserIdentity found) {
        if (CollectionUtils.isEmpty(found.getRoles()) || found.getRoles().stream().noneMatch(x -> x == Role.ROLE_PLATFORM_USER)) {
            throw commandException(HttpStatus.FORBIDDEN, AUTHORIZATION_ERROR);
        }
    }

    public static void validUDeviceOrThrowUnauthorized(String deviceId, UserIdentity found) {
        if (found.getDevices().stream().noneMatch(x -> x.equalsIgnoreCase(deviceId))) {
            throw commandException(HttpStatus.FORBIDDEN, UNRECOGNIZED_DEVICE_ERROR);
        }
    }

    public static void nonDeletedStatusOrThrow(UserIdentity found) {
        if (found.isDeleted()) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, INVALID_USER_STATUS_ERROR);
        }
    }

    public static void validateNotExistOrThrow(MongoAdapter mongoAdapter, String username) {
        mongoAdapter.findUserByUsername(username).ifPresent(
                (x) -> {
                    throw commandException(HttpStatus.CONFLICT, DUPLICATE_REQUEST_ERROR);
                }
        );
    }

    public static void validateNotExistByReferenceOrThrow(MongoAdapter mongoAdapter, String userReference) {
        mongoAdapter.findUserByUserReference(userReference).ifPresent(
                (x) -> {
                    throw commandException(HttpStatus.CONFLICT, DUPLICATE_REQUEST_ERROR);
                }
        );
    }

    public void isSamePasswordOrThrow(IsSamePasswordParameter isSamePasswordParameter) {
        boolean isSamePassword = this.passwordUtil.isSamePassword((isSamePasswordParameter));
        if (!isSamePassword) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, INVALID_CREDENTIALS);
        }
    }

    public static void validStatusAndDeviceOrThrow(String deviceId, UserIdentity found) {
        validUserStatusOrThrowUnauthorized(found);
        validUDeviceOrThrowUnauthorized(deviceId, found);
    }

    public static void updateLoginActivity(JwtHelper jwtHelper, UserIdentity found, Token token) {
        String lastAccessTokenExpiresAt = new Date(jwtHelper.getJwtPayload(token.getAccessToken()).getExp() * 1000)
                .toInstant()
                .truncatedTo(ChronoUnit.SECONDS).toString();
        String lLastRefreshTokenExpiresAt = new Date(jwtHelper.getJwtPayload(token.getRefreshToken()).getExp() * 1000)
                .toInstant()
                .truncatedTo(ChronoUnit.SECONDS).toString();

        found.getLoginActivity().setLastLoggedInAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        found.getLoginActivity().setLastAccessTokenJti(jwtHelper.getJwtPayload(token.getAccessToken()).getJti());
        found.getLoginActivity().setLastRefreshTokenJti(jwtHelper.getJwtPayload(token.getRefreshToken()).getJti());
        found.getLoginActivity().setLastAccessTokenExpiresAt(lastAccessTokenExpiresAt);
        found.getLoginActivity().setLastRefreshTokenExpiresAt(lLastRefreshTokenExpiresAt);
    }

    public static AuthenticationResponse buildAuthenticationResponse(CreateCipherResponseModel cipherResponse, Token token) {
        return AuthenticationResponse.builder()
                .accessToken(token.getAccessToken())
                .accessTokenExpirySeconds(token.getAccessTokenExpiresIn())
                .refreshToken(token.getRefreshToken())
                .refreshTokenExpirySeconds(token.getRefreshTokenExpiresIn())
                .cipher(Cipher.builder()
                        .cipherReference(cipherResponse.getReference())
                        .publicKey(cipherResponse.getPublicKey())
                        .build())
                .build();
    }

    public static AuthenticationResponse buildAuthenticationResponse(Token token) {
        return AuthenticationResponse.builder()
                .accessToken(token.getAccessToken())
                .accessTokenExpirySeconds(token.getAccessTokenExpiresIn())
                .refreshToken(token.getRefreshToken())
                .refreshTokenExpirySeconds(token.getRefreshTokenExpiresIn())
                .build();
    }

    public static void revokeExistingTokens(RedisAdapter redisAdapter, Map<String, String> revokedTokens) {
        Executors.newSingleThreadExecutor().submit(() -> {
            if (!revokedTokens.isEmpty()) {
                redisAdapter.publishRevokedTokens(revokedTokens);
            }
        });
    }

    public static UserIdentity getsRequestByUsernameOrThrow(MongoAdapter mongoAdapter, String username) {
        return mongoAdapter.findUserByUsername(username).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

}
