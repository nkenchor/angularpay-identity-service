
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformBank {

    private String address;
    private String code;
    @JsonProperty("country_reference")
    private String countryReference;
    private boolean enabled;
    private String name;
    private String reference;
    @JsonProperty("swift_code")
    private String swiftCode;

}
