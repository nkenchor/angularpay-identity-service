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
public class CreateUserCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private CreateUserApiModel createUserApiModel;

    CreateUserCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
