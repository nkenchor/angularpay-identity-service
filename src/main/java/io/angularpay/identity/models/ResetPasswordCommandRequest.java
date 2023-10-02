package io.angularpay.identity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResetPasswordCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    @NotNull
    @Valid
    private ResetPasswordApiModel resetPasswordApiModel;

    ResetPasswordCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
