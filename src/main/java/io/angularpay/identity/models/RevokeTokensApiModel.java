
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RevokeTokensApiModel {

    @NotEmpty
    @JsonProperty("access_token")
    private String accessToken;
    @NotEmpty
    @JsonProperty("refresh_token")
    private String refreshToken;
}
