
package io.angularpay.identity.models.platform;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PlatformCountryFeature {

    @JsonProperty("country_name")
    private String countryName;
    @JsonProperty("country_reference")
    private String countryReference;
    private String reference;
    private List<SupportedService> services;
}
