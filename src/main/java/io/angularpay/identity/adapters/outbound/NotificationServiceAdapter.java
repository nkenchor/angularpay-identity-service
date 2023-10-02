package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.models.SendNotificationRequestApiModel;
import io.angularpay.identity.models.SendNotificationResponseApiModel;
import io.angularpay.identity.ports.outbound.NotificationServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceAdapter implements NotificationServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public Optional<SendNotificationResponseApiModel> sendNotification(
            SendNotificationRequestApiModel sendNotificationRequestApiModel,
            Map<String, String> headers) {
        URI notificationUrl = UriComponentsBuilder.fromUriString(configuration.getNotificationUrl())
                .path("/notification/requests")
                .build().toUri();

        SendNotificationResponseApiModel sendNotificationResponseApiModel = webClient
                .post()
                .uri(notificationUrl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .body(Mono.just(sendNotificationRequestApiModel), SendNotificationRequestApiModel.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(SendNotificationResponseApiModel.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .block();
        return Objects.nonNull(sendNotificationResponseApiModel)? Optional.of(sendNotificationResponseApiModel) : Optional.empty();
    }
}
