package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChangePasswordApiModel {

    @NotEmpty
    @JsonProperty("old_password")
    private String oldPassword;
    @NotEmpty
    @JsonProperty("new_password")
    private String newPassword;
}
