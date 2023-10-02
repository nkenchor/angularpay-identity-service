package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.models.CreateCipherResponseModel;
import io.angularpay.identity.ports.outbound.CipherServicePort;
import lombok.RequiredArgsConstructor;
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
public class CipherServicePortAdapter implements CipherServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public Optional<CreateCipherResponseModel> createCipher(Map<String, String> headers) {
        URI cipherUrl = UriComponentsBuilder.fromUriString(configuration.getCipherUrl())
                .path("/cipher/entries")
                .build().toUri();

        CreateCipherResponseModel createCipherResponseModel = webClient
                .post()
                .uri(cipherUrl.toString())
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(CreateCipherResponseModel.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .block();

        return Objects.nonNull(createCipherResponseModel) ? Optional.of(createCipherResponseModel) : Optional.empty();
    }
}
