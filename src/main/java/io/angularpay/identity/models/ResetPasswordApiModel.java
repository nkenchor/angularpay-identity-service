package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ResetPasswordApiModel {

    @NotEmpty
    @JsonProperty("new_password")
    private String newPassword;
}
