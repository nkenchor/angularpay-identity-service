package io.angularpay.identity.ports.inbound;

import io.angularpay.identity.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
