package io.angularpay.identity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericAuthenticateUserCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private GenericUserLoginApiModel userLoginApiModel;

    GenericAuthenticateUserCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
