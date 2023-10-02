package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.models.GoogleReCaptchaRequest;
import io.angularpay.identity.models.GoogleReCaptchaResponse;
import io.angularpay.identity.ports.outbound.GoogleReCaptchaV3Port;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleReCaptchaV3Adapter implements GoogleReCaptchaV3Port {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public Optional<GoogleReCaptchaResponse> recapatcha(GoogleReCaptchaRequest request) {
        URI notificationUrl = UriComponentsBuilder.fromUriString(configuration.getGoogleRecaptcha().getUrl())
                .build().toUri();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", configuration.getGoogleRecaptcha().getSecret());
        formData.add("response", request.getRecaptchaToken());

        GoogleReCaptchaResponse googleReCaptchaResponse = webClient
                .post()
                .uri(notificationUrl.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(GoogleReCaptchaResponse.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .block();
        return Objects.nonNull(googleReCaptchaResponse) ? Optional.of(googleReCaptchaResponse) : Optional.empty();
    }



}
