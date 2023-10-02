package io.angularpay.identity.helpers;

import io.angularpay.identity.domain.LoginActivity;
import io.angularpay.identity.domain.UserIdentity;

import java.util.HashSet;

public class ObjectFactory {

    public static UserIdentity userIdentityWithDefaults() {
        return UserIdentity.builder()
                .devices(new HashSet<>())
                .loginActivity(LoginActivity.builder().build())
                .deleted(false)
                .enabled(true)
                .build();
    }
}