package io.angularpay.identity.adapters.inbound;

import io.angularpay.identity.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.identity.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.identity.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.identity.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
