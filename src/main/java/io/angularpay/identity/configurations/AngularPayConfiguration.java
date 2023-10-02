package io.angularpay.identity.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("angularpay")
@Data
public class AngularPayConfiguration {

    private String otpUrl;
    private String notificationUrl;
    private String userconfigUrl;
    private String cipherUrl;
    private int pageSize;
    private int codecSizeInMB;
    private Session session;
    private Security security;
    private Redis redis;
    private BruteForceGuardConfiguration bruteForceGuard;
    private GoogleRecaptcha googleRecaptcha;
    private PasswordResetTTL passwordResetTTL;

    @Data
    public static class Redis {
        private String host;
        private int port;
        private int timeout;
    }

    @Data
    public static class Session {
        private long defaultAccessTokenExpirySeconds;
        private long defaultRefreshTokenExpirySeconds;
    }

    @Data
    public static class Security {
        private String privateKey;
        private String publicKey;
        private String issuer;
    }

    @Data
    public static class BruteForceGuardConfiguration {
        private int maxLoginAttempts;
        private int blockDurationInHours;
    }

    @Data
    public static class GoogleRecaptcha {
        private String url;
        private String key;
        private String secret;
        private float threshold;
        private boolean enabled;
    }

    @Data
    public static class PasswordResetTTL {
        private int confirmationTTLSeconds;
        private int passwordResetTTLSeconds;
    }

}
