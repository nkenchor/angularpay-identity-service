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
public class GetByUsernameCommandRequest extends AccessControl {

    @NotEmpty
    private String username;

    GetByUsernameCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
