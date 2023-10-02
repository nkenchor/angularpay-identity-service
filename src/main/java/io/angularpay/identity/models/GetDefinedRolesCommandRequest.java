package io.angularpay.identity.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class GetDefinedRolesCommandRequest extends AccessControl {

    GetDefinedRolesCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
