
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateOtpRequestApiModel {

    @JsonProperty("device_reference")
    private String deviceReference;
    private String code;
    @JsonProperty("user_reference")
    private String userReference;
}
