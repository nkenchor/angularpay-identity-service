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
public class VerifyTokenCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private VerifyTokenApiModel verifyTokenApiModel;

    VerifyTokenCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
