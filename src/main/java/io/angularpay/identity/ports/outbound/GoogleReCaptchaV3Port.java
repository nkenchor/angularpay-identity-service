package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.models.GoogleReCaptchaRequest;
import io.angularpay.identity.models.GoogleReCaptchaResponse;

import java.util.Optional;

public interface GoogleReCaptchaV3Port {
    Optional<GoogleReCaptchaResponse> recapatcha(GoogleReCaptchaRequest request);
}
