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
public class PasswordReset {

    @JsonProperty("last_modified")
    private String lastModified;
    private PasswordResetType type;
    private PasswordResetStatus status;
}
