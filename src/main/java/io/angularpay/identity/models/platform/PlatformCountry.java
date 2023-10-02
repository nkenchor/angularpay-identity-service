
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformCountry {

    private String code;
    @JsonProperty("dialing_code")
    private String dialingCode;
    private Boolean enabled;
    @JsonProperty("iso_code_2")
    private String isoCode2;
    @JsonProperty("iso_code_3")
    private String isoCode3;
    private String name;
    private String reference;

}
