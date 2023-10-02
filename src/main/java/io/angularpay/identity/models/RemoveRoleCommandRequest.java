package io.angularpay.identity.models;

import io.angularpay.identity.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RemoveRoleCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    @NotNull
    private Role role;

    RemoveRoleCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
