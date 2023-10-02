
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import static io.angularpay.identity.common.Constants.REGEX_EMAIL_ADDRESS;

@Data
public class OnboardUserApiModel {

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
    @NotEmpty
    @JsonProperty("user_reference")
    private String userReference;
}
