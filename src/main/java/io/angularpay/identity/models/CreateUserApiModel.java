
package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import static io.angularpay.identity.common.Constants.REGEX_EMAIL_ADDRESS;

@Data
public class CreateUserApiModel {

    @NotEmpty
    private String device;
    @NotEmpty
    @Pattern(regexp = REGEX_EMAIL_ADDRESS, message = "Invalid email address", flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String email;
    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;
    @NotEmpty
    private String password;
    @NotEmpty
    private String username;
}
