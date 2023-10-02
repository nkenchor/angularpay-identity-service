package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateCipherResponseModel {

    private String reference;
    @JsonProperty("public_key")
    private String publicKey;
}
