
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformCurrency {

    private String code;
    @JsonProperty("country_code")
    private String countryCode;
    private Boolean enabled;
    private String name;
    private String references;

}
