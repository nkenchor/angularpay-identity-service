package io.angularpay.identity.ports.outbound;

import java.util.Map;

public interface OutboundMessagingPort {
    void publishRevokedTokens(Map<String, String> revokedTokens);
    boolean isTokenRevoked(String reference);
    void removeIfExpired(String reference);
    Map<String, String> getPlatformConfigurations(String hashName);
}
