package io.angularpay.identity.domain.commands;

import io.angularpay.identity.domain.UserIdentity;

import java.util.Map;
import java.util.Objects;

import static io.angularpay.identity.helpers.Helper.getTokenIfExists;

public interface AuthActivityPublisherCommand<T> {

    UserIdentity getUserIdentity(T t);

    void publishRevokedTokens(Map<String, String> revokedTokens);

    default void publishAuthActivity(T t) {
        UserIdentity userIdentity = this.getUserIdentity(t);
        if (Objects.nonNull(userIdentity)) {
            Map<String, String> existingJtis = getTokenIfExists(userIdentity);
            if (!existingJtis.isEmpty()) {
                this.publishRevokedTokens(existingJtis);
            }
        }
    }
}
