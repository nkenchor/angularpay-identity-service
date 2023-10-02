package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ForgotPasswordStartApiModel {

    @NotEmpty
    private String username;
}
