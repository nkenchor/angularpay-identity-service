
package io.angularpay.identity.models;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Data
public class GenericUserLoginApiModel {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;

    @Valid
    private GoogleReCaptchaRequest recaptcha;
}
