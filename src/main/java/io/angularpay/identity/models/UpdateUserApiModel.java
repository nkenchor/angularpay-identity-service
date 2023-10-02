package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdateUserApiModel {

    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;
}
