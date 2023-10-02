
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VerifyTokenResponse {

    @JsonProperty("is_valid")
    private final boolean valid;

}
