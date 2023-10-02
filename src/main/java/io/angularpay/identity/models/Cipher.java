
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cipher {

    @JsonProperty("cipher_reference")
    private String cipherReference;
    @JsonProperty("public_key")
    private String publicKey;
}
