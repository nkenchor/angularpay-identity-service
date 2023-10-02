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
public class RemoveDeviceCommandRequest extends AccessControl {

    @NotEmpty
    private String userReference;

    @NotEmpty
    private String deviceId;

    RemoveDeviceCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
