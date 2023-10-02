
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformService {

    @JsonProperty("cipher_protected")
    private Boolean cipherProtected;
    private String code;
    private String description;
    private boolean enabled;
    private String name;
    private String reference;
    private String type;
    @JsonProperty("written_in")
    private String writtenIn;

}
