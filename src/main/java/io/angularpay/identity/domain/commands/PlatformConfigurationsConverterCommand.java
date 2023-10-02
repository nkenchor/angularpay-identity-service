package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.models.platform.*;
import io.angularpay.identity.ports.outbound.OutboundMessagingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static io.angularpay.identity.exceptions.ErrorCode.INVALID_MESSAGE_ERROR;
import static io.angularpay.identity.models.platform.PlatformConfigurationSource.HASH;

@Slf4j
@Service
public class PlatformConfigurationsConverterCommand {

    private final PlatformConfigurations platformConfigurations;
    private final ObjectMapper mapper;

    public PlatformConfigurationsConverterCommand(
            PlatformConfigurations platformConfigurations,
            ObjectMapper mapper,
            OutboundMessagingPort outboundMessagingPort) {
        this.platformConfigurations = platformConfigurations;
        this.mapper = mapper;

        log.info("reading platform configurations from message hash...");
        Map<String, String> configurations = outboundMessagingPort.getPlatformConfigurations(
                PlatformConfigurationIdentifier.getHashName()
        );
        if (CollectionUtils.isEmpty(configurations)) {
            log.info("platform configurations message hash is empty");
        } else {
            log.info("read {} items from platform configurations message hash", configurations.size());
            configurations.forEach((key, value) -> {
                PlatformConfigurationIdentifier.fromHashField(key).ifPresent(identifier -> {
                    this.execute(value, identifier, HASH);
                });
            });
        }
    }

    public void execute(String message, PlatformConfigurationIdentifier identifier, PlatformConfigurationSource source) {
        log.info("updating global platform configurations {} values from {} -> {}", identifier.name(), source.name(), message);
        try {
            switch (identifier) {
                case PLATFORM_BANKS:
                    List<PlatformBank> platformBanks = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformBanks(platformBanks);
                    break;
                case PLATFORM_COUNTRIES:
                    List<PlatformCountry> platformCountries = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCountries(platformCountries);
                    break;
                case PLATFORM_COUNTRY_FEATURES:
                    List<PlatformCountryFeature> platformCountryFeatures = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCountryFeatures(platformCountryFeatures);
                    break;
                case PLATFORM_CURRENCIES:
                    List<PlatformCurrency> platformCurrencies = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCurrencies(platformCurrencies);
                    break;
                case PLATFORM_MATURITY_CONFIGURATIONS:
                    List<PlatformMaturityConfiguration> platformMaturityConfigurations = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformMaturityConfigurations(platformMaturityConfigurations);
                    break;
                case PLATFORM_NOTIFICATION_TYPES:
                    List<PlatformNotificationType> platformNotificationTypes = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformNotificationTypes(platformNotificationTypes);
                    break;
                case PLATFORM_OTP_TYPES:
                    List<PlatformOTPType> platformOTPTypes = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformOTPTypes(platformOTPTypes);
                    break;
                case PLATFORM_SERVICES:
                    List<PlatformService> platformServices = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformServices(platformServices);
                    break;
                case PLATFORM_TTL_CONFIGURATION:
                    PlatformTTLConfiguration platformTTLConfiguration = mapper.readValue(message, PlatformTTLConfiguration.class);
                    this.platformConfigurations.setPlatformTTLConfiguration(platformTTLConfiguration);
                    break;
            }
        } catch (Exception exception) {
            log.error("An error occurred while processing platform configurations {} value from {}", identifier.name(), source.name(), exception);
            throw new RuntimeException(CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(INVALID_MESSAGE_ERROR)
                    .message(exception.getMessage())
                    .cause(exception)
                    .build());
        }
    }
}
