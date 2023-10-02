package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.models.CreateCipherResponseModel;

import java.util.Map;
import java.util.Optional;

public interface CipherServicePort {
    Optional<CreateCipherResponseModel> createCipher(Map<String, String> headers);
}
