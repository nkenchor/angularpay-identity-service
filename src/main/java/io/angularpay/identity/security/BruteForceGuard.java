package io.angularpay.identity.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.models.BruteForceCacheEntry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class BruteForceGuard {

    private final AngularPayConfiguration configuration;
    private final LoadingCache<String, BruteForceCacheEntry> bruteForceCacheLoadingCache;

    public BruteForceGuard(AngularPayConfiguration configuration) {
        super();
        this.configuration = configuration;
        bruteForceCacheLoadingCache = CacheBuilder.newBuilder().
                expireAfterWrite(configuration.getBruteForceGuard().getBlockDurationInHours(), TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public BruteForceCacheEntry load(String key) {
                        return BruteForceCacheEntry.builder()
                                .blocked(false)
                                .loginAttempts(0)
                                .build();
                    }
                });
    }

    public void onLoginSuccess(String key) {
        bruteForceCacheLoadingCache.invalidate(key);
    }

    public void onLoginFailure(String key) {
        BruteForceCacheEntry entry;
        try {
            entry = bruteForceCacheLoadingCache.get(key);
        } catch (ExecutionException e) {
            entry = BruteForceCacheEntry.builder()
                    .blocked(false)
                    .loginAttempts(0)
                    .build();
        }

        int attempts = entry.getLoginAttempts() + 1;
        entry.setLoginAttempts(attempts);

        if (attempts >= configuration.getBruteForceGuard().getMaxLoginAttempts()) {
            entry.setBlocked(true);
            entry.setBlockedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        }

        bruteForceCacheLoadingCache.put(key, entry);
    }

    public boolean isBlocked(String key) {
        try {
            BruteForceCacheEntry entry = bruteForceCacheLoadingCache.get(key);
            return entry.isBlocked();
        } catch (ExecutionException e) {
            return false;
        }
    }
}
