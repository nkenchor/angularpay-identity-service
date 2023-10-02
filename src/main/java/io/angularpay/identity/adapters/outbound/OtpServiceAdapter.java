package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.models.CreateOtpRequestApiModel;
import io.angularpay.identity.models.CreateOtpResponseApiModel;
import io.angularpay.identity.models.ValidateOtpRequestApiModel;
import io.angularpay.identity.models.ValidateOtpResponseApiModel;
import io.angularpay.identity.ports.outbound.OtpServicePort;
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
public class OtpServiceAdapter implements OtpServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public Optional<CreateOtpResponseApiModel> createOtp(
            CreateOtpRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers) {
        URI otpUrl = UriComponentsBuilder.fromUriString(configuration.getOtpUrl())
                .path("/otp/requests")
                .build().toUri();

        CreateOtpResponseApiModel createOtpResponseApiModel = webClient
                .post()
                .uri(otpUrl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .body(Mono.just(createOtpRequestApiModel), CreateOtpRequestApiModel.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(CreateOtpResponseApiModel.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .block();
        return Objects.nonNull(createOtpResponseApiModel)? Optional.of(createOtpResponseApiModel) : Optional.empty();
    }

    @Override
    public Optional<ValidateOtpResponseApiModel> validateOtp(
            ValidateOtpRequestApiModel validateOtpRequestApiModel,
            Map<String, String> headers) {
        URI otpUrl = UriComponentsBuilder.fromUriString(configuration.getOtpUrl())
                .path("/otp/requests/validate")
                .build().toUri();

        ValidateOtpResponseApiModel validateOtpResponseApiModel = webClient
                .post()
                .uri(otpUrl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .body(Mono.just(validateOtpRequestApiModel), ValidateOtpRequestApiModel.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ValidateOtpResponseApiModel.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .block();
        return Objects.nonNull(validateOtpResponseApiModel)? Optional.of(validateOtpResponseApiModel) : Optional.empty();
    }
}
