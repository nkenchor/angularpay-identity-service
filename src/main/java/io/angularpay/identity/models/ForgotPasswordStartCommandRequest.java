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
public class ForgotPasswordStartCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private ForgotPasswordStartApiModel forgotPasswordStartApiModel;

    ForgotPasswordStartCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
