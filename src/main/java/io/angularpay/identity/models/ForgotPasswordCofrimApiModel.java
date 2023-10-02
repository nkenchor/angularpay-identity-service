package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ForgotPasswordCofrimApiModel {

    @NotEmpty
    private String otp;
}
