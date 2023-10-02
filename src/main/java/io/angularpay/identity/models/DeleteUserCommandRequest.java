package io.angularpay.identity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DeleteUserCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    DeleteUserCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
