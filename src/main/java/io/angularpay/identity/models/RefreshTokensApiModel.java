
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RefreshTokensApiModel {

    @NotEmpty
    @JsonProperty("refresh_token")
    private String refreshToken;
}
