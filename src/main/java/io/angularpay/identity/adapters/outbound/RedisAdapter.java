package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.ports.outbound.OutboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisAdapter implements OutboundMessagingPort {

    private final RedisHashClient redisHashClient;

    @Override
    public void publishRevokedTokens(Map<String, String> revokedTokens) {
        this.redisHashClient.publishRevokedTokens(revokedTokens);
    }

    @Override
    public boolean isTokenRevoked(String reference) {
        return this.redisHashClient.isTokenRevoked(reference);
    }

    @Override
    public void removeIfExpired(String reference) {
        this.redisHashClient.removeIfExpired(reference);
    }

    @Override
    public Map<String, String> getPlatformConfigurations(String hashName) {
        return this.redisHashClient.getPlatformConfigurations(hashName);
    }
}
