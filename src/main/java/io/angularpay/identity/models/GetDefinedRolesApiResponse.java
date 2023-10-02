
package io.angularpay.identity.models;

import io.angularpay.identity.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDefinedRolesApiResponse {

    private Role role;
    private String name;
}
