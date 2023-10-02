package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.models.SendNotificationRequestApiModel;
import io.angularpay.identity.models.SendNotificationResponseApiModel;

import java.util.Map;
import java.util.Optional;

public interface NotificationServicePort {
    Optional<SendNotificationResponseApiModel> sendNotification(
            SendNotificationRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers);
}
