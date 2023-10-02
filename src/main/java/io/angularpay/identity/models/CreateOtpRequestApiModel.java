
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOtpRequestApiModel {

    @JsonProperty("device_reference")
    private String deviceReference;
    private Boolean strict;
    @JsonProperty("user_reference")
    private String userReference;
}
