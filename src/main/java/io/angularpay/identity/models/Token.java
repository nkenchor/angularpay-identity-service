package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Token {
    private String accessToken;
    private long accessTokenExpiresIn;
    private String refreshToken;
    private long refreshTokenExpiresIn;
}
