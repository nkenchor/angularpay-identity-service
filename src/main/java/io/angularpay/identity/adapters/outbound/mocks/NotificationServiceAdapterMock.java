package io.angularpay.identity.adapters.outbound.mocks;

import io.angularpay.identity.models.NotificationStatus;
import io.angularpay.identity.models.SendNotificationRequestApiModel;
import io.angularpay.identity.models.SendNotificationResponseApiModel;
import io.angularpay.identity.ports.outbound.NotificationServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceAdapterMock implements NotificationServicePort {

    @Override
    public Optional<SendNotificationResponseApiModel> sendNotification(
            SendNotificationRequestApiModel sendNotificationRequestApiModel,
            Map<String, String> headers) {
        SendNotificationResponseApiModel notificationResponseApiModel = new SendNotificationResponseApiModel();
        notificationResponseApiModel.setReference(UUID.randomUUID().toString());
        notificationResponseApiModel.setStatus(NotificationStatus.SENT);
        return Optional.of(notificationResponseApiModel);
    }
}
