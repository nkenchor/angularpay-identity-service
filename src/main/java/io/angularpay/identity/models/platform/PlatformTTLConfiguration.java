
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformTTLConfiguration {

    @JsonProperty("time_unit")
    private String timeUnit;
    private Long value;

}
