package io.angularpay.identity.adapters.inbound;

import io.angularpay.identity.models.platform.PlatformConfigurationIdentifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

@RequiredArgsConstructor
public class UpdatesMessageSubscriber implements MessageListener {

    private final RedisMessageAdapter redisMessageAdapter;
    private final PlatformConfigurationIdentifier identifier;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        redisMessageAdapter.onMessage(String.valueOf(message), this.identifier);
    }
}
