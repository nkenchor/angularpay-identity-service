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
public class RefreshTokenCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private RefreshTokensApiModel refreshTokensApiModel;

    RefreshTokenCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
