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
public class EnableUserCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    private boolean enable;

    EnableUserCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
