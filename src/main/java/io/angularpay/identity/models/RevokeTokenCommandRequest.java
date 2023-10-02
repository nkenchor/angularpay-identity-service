package io.angularpay.identity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RevokeTokenCommandRequest extends AccessControl {

    private RevokeTokensApiModel revokeTokensApiModel;

    RevokeTokenCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
