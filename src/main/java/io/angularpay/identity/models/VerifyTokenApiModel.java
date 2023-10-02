
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VerifyTokenApiModel {

    @NotEmpty
    @JsonProperty("access_token")
    private String accessToken;
}
