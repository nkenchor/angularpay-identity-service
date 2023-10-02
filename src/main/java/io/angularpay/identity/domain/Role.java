package io.angularpay.identity.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Role {
    ROLE_ONBOARDING_USER(false),
    ROLE_UNVERIFIED_USER,
    ROLE_VERIFIED_USER,
    ROLE_FORGOT_PASSWORD_USER(false),
    ROLE_PLATFORM_MODERATOR,
    ROLE_SERVICE_ACCOUNT,
    ROLE_PLATFORM_USER,
    ROLE_USERCONFIG_ADMIN,
    ROLE_SECRETS_ADMIN,
    ROLE_REPORTING_ADMIN,
    ROLE_TRANSACTION_ADMIN,
    ROLE_IDENTITY_ADMIN,
    ROLE_INTEGRATION_ADMIN,
    ROLE_FRAUD_ADMIN,
    ROLE_DOCUMENTS_ADMIN,
    ROLE_SCHEDULER_ADMIN,
    ROLE_MATURITY_ADMIN,
    ROLE_VAULT_ADMIN,
    ROLE_LANGUAGE_ADMIN,
    ROLE_RESOURCE_ADMIN,
    ROLE_SERVICE_MANAGER_ADMIN,
    ROLE_MEMES_ADMIN,
    ROLE_SUBSCRIPTION_ADMIN,
    ROLE_PURITY_ADMIN,
    ROLE_BLOG_ADMIN,
    ROLE_INQUIRY_ADMIN,
    ROLE_CMS_ADMIN,
    ROLE_KYC_ADMIN,
    ROLE_BANK_ADMIN,
    ROLE_PLATFORM_ADMIN,
    ROLE_SUPER_ADMIN;

    private final boolean knownUser;

    Role() {
        this.knownUser = true;
    }

    Role(boolean knownUser) {
        this.knownUser = knownUser;
    }

    public boolean isKnownUser() {
        return knownUser;
    }

    public static String[] knownUserRoles() {
        return Arrays.stream(Role.values())
                .filter(Role::isKnownUser)
                .map(Enum::name)
                .toArray(String[]::new);
    }

    public static String[] unknownUserRoles() {
        return Arrays.stream(Role.values())
                .filter(x -> !x.isKnownUser())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    public static List<Role> assignableUserRoles() {
        return Arrays.stream(Role.values())
                .filter(x -> x.isKnownUser() && x != Role.ROLE_VERIFIED_USER && x != Role.ROLE_UNVERIFIED_USER
                        && x != Role.ROLE_SERVICE_ACCOUNT && x != Role.ROLE_SUPER_ADMIN
                ).collect(Collectors.toList());
    }

    public static List<Role> nonAssignableUserRoles() {
        return Arrays.stream(Role.values())
                .filter(x -> !x.isKnownUser() || x == Role.ROLE_VERIFIED_USER || x == Role.ROLE_UNVERIFIED_USER
                        || x == Role.ROLE_SERVICE_ACCOUNT || x == Role.ROLE_SUPER_ADMIN
                ).collect(Collectors.toList());
    }
}
