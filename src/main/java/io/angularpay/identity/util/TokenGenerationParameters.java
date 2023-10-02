package io.angularpay.identity.util;

import io.angularpay.identity.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TokenGenerationParameters {
    private String userReference;
    private String username;
    private String deviceId;
    private Set<Role> roles;
}
