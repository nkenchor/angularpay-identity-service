package io.angularpay.identity.models;

import io.angularpay.identity.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateRolesCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    @NotEmpty
    private List<Role> roles;

    UpdateRolesCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
