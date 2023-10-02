
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformMaturityConfiguration {

    private String cron;
    private String reference;
    @JsonProperty("service_code")
    private String serviceCode;
    @JsonProperty("sla_days")
    private Long slaDays;

}
