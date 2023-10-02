
package io.angularpay.identity.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class PlatformConfigurations {

    @JsonProperty("platform_banks")
    private List<PlatformBank> platformBanks;

    @JsonProperty("platform_countries")
    private List<PlatformCountry> platformCountries;

    @JsonProperty("platform_country_features")
    private List<PlatformCountryFeature> platformCountryFeatures;

    @JsonProperty("platform_currencies")
    private List<PlatformCurrency> platformCurrencies;

    @JsonProperty("platform_maturity_configurations")
    private List<PlatformMaturityConfiguration> platformMaturityConfigurations;

    @JsonProperty("platform_notification_types")
    private List<PlatformNotificationType> platformNotificationTypes;

    @JsonProperty("platform_otp_types")
    private List<PlatformOTPType> platformOTPTypes;

    @JsonProperty("platform_services")
    private List<PlatformService> platformServices;

    @JsonProperty("platform_ttl_configuration")
    private PlatformTTLConfiguration platformTTLConfiguration;

}
