package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static io.angularpay.identity.common.Constants.REVOKED_TOKEN_HASH;

@Service
@RequiredArgsConstructor
public class RedisHashClient {

    private final AngularPayConfiguration configuration;

    public void publishRevokedTokens(Map<String, String> revokedTokens) {
        try (Jedis jedis = jedisInstance()) {
            jedis.hmset(REVOKED_TOKEN_HASH, revokedTokens);
        }
    }

    public boolean isTokenRevoked(String reference) {
        try (Jedis jedis = jedisInstance()) {
            return jedis.hexists(REVOKED_TOKEN_HASH, reference);
        }
    }

    public void removeIfExpired(String reference) {
        try (Jedis jedis = jedisInstance()) {
            String value = jedis.hget(REVOKED_TOKEN_HASH, reference);
            if (Instant.now().truncatedTo(ChronoUnit.SECONDS).isAfter(Instant.parse(value))) {
                jedis.hdel(REVOKED_TOKEN_HASH, reference);
            }
        }
    }

    private Jedis jedisInstance() {
        return new Jedis(
                configuration.getRedis().getHost(),
                configuration.getRedis().getPort(),
                configuration.getRedis().getTimeout()
        );
    }

    public Map<String, String> getPlatformConfigurations(String hashName) {
        try (Jedis jedis = jedisInstance()) {
            return jedis.hgetAll(hashName);
        }
    }
}
