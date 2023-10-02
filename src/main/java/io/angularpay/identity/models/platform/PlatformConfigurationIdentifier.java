
package io.angularpay.identity.models.platform;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PlatformConfigurationIdentifier {

    PLATFORM_BANKS("platform-banks"),
    PLATFORM_COUNTRIES("platform-countries"),
    PLATFORM_COUNTRY_FEATURES("platform-country-features"),
    PLATFORM_CURRENCIES("platform-currencies"),
    PLATFORM_MATURITY_CONFIGURATIONS("platform-maturity-configurations"),
    PLATFORM_NOTIFICATION_TYPES("platform-notification-types"),
    PLATFORM_OTP_TYPES("platform-otp-types"),
    PLATFORM_SERVICES("platform-services"),
    PLATFORM_TTL_CONFIGURATION("platform-ttl-configuration");

    private static final String hashName = "platform-configurations";
    private final String hashField;
    private final String topic;

    PlatformConfigurationIdentifier(String topic) {
        this.hashField = topic;
        this.topic = topic;
    }

    public static Optional<PlatformConfigurationIdentifier> fromHashField(String hashField) {
        return Arrays.stream(PlatformConfigurationIdentifier.values())
                .filter(x -> x.getHashField().equalsIgnoreCase(hashField)).findFirst();
    }

    public static String getHashName() {
        return hashName;
    }
}
