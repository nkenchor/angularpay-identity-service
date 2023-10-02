
package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserLoginApiModel {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
}
