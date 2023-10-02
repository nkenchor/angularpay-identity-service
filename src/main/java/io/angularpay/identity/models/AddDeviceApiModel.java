package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AddDeviceApiModel {

    @NotEmpty
    private String device;
}
