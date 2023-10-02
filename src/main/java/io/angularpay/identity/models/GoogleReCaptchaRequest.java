package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class GoogleReCaptchaRequest {

    @NotEmpty
    @JsonProperty("recaptcha_token")
    private String recaptchaToken;

    @NotEmpty
    @JsonProperty("action_name")
    private String actionName;
}
