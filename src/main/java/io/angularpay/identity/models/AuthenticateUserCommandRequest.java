package io.angularpay.identity.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class AuthenticateUserCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private UserLoginApiModel userLoginApiModel;

    AuthenticateUserCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
