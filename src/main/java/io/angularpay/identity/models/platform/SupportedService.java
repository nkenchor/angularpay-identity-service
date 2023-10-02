
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SupportedService {

    @JsonProperty("service_code")
    private String serviceCode;
    @JsonProperty("service_reference")
    private String serviceReference;
}
