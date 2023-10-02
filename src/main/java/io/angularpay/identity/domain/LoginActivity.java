
package io.angularpay.identity.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginActivity {

    @JsonProperty("last_logged_in_at")
    private String lastLoggedInAt;
    @JsonProperty("access_token_jti")
    private String lastAccessTokenJti;
    @JsonProperty("refresh_token_jti")
    private String lastRefreshTokenJti;
    @JsonProperty("access_token_expires_at")
    private String lastAccessTokenExpiresAt;
    @JsonProperty("refresh_token_expires_at")
    private String lastRefreshTokenExpiresAt;
}
