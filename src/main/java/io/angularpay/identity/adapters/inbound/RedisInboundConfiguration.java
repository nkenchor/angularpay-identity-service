package io.angularpay.identity.adapters.inbound;

import io.angularpay.identity.adapters.common.RedisConfiguration;
import io.angularpay.identity.configurations.AngularPayConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static io.angularpay.identity.models.platform.PlatformConfigurationIdentifier.*;

@Configuration
@RequiredArgsConstructor
public class RedisInboundConfiguration {

    private final RedisConfiguration redisConfiguration;

    @Bean
    ChannelTopic banksTopic() {
        return new ChannelTopic(PLATFORM_BANKS.getTopic());
    }

    @Bean
    ChannelTopic countriesTopic() {
        return new ChannelTopic(PLATFORM_COUNTRIES.getTopic());
    }

    @Bean
    ChannelTopic countryFeaturesTopic() {
        return new ChannelTopic(PLATFORM_COUNTRY_FEATURES.getTopic());
    }

    @Bean
    ChannelTopic currenciesTopic() {
        return new ChannelTopic(PLATFORM_CURRENCIES.getTopic());
    }

    @Bean
    ChannelTopic maturityConfigurationsTopic() {
        return new ChannelTopic(PLATFORM_MATURITY_CONFIGURATIONS.getTopic());
    }

    @Bean
    ChannelTopic notificationTypesTopic() {
        return new ChannelTopic(PLATFORM_NOTIFICATION_TYPES.getTopic());
    }

    @Bean
    ChannelTopic otpTypesTopic() {
        return new ChannelTopic(PLATFORM_OTP_TYPES.getTopic());
    }

    @Bean
    ChannelTopic servicesTopic() {
        return new ChannelTopic(PLATFORM_SERVICES.getTopic());
    }

    @Bean
    ChannelTopic ttlConfigurationTopic() {
        return new ChannelTopic(PLATFORM_TTL_CONFIGURATION.getTopic());
    }


    @Bean("banksListenerAdapter")
    MessageListenerAdapter banksListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_BANKS));
    }

    @Bean("countriesListenerAdapter")
    MessageListenerAdapter countriesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_COUNTRIES));
    }

    @Bean("countryFeaturesListenerAdapter")
    MessageListenerAdapter countryFeaturesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_COUNTRY_FEATURES));
    }

    @Bean("currenciesListenerAdapter")
    MessageListenerAdapter currenciesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_CURRENCIES));
    }

    @Bean("maturityConfigurationsListenerAdapter")
    MessageListenerAdapter maturityConfigurationsListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_MATURITY_CONFIGURATIONS));
    }

    @Bean("notificationTypesListenerAdapter")
    MessageListenerAdapter notificationTypesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_NOTIFICATION_TYPES));
    }

    @Bean("otpTypesListenerAdapter")
    MessageListenerAdapter otpTypesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_OTP_TYPES));
    }

    @Bean("servicesListenerAdapter")
    MessageListenerAdapter servicesListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_SERVICES));
    }

    @Bean("ttlConfigurationListenerAdapter")
    MessageListenerAdapter ttlConfigurationListenerAdapter(RedisMessageAdapter redisMessageAdapter) {
        return new MessageListenerAdapter(new UpdatesMessageSubscriber(redisMessageAdapter, PLATFORM_TTL_CONFIGURATION));
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisMessageAdapter redisMessageAdapter, AngularPayConfiguration angularPayConfiguration) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConfiguration.connectionFactory(angularPayConfiguration));
        container.addMessageListener(banksListenerAdapter(redisMessageAdapter), banksTopic());
        container.addMessageListener(countriesListenerAdapter(redisMessageAdapter), countriesTopic());
        container.addMessageListener(countryFeaturesListenerAdapter(redisMessageAdapter), countryFeaturesTopic());
        container.addMessageListener(currenciesListenerAdapter(redisMessageAdapter), currenciesTopic());
        container.addMessageListener(maturityConfigurationsListenerAdapter(redisMessageAdapter), maturityConfigurationsTopic());
        container.addMessageListener(notificationTypesListenerAdapter(redisMessageAdapter), notificationTypesTopic());
        container.addMessageListener(otpTypesListenerAdapter(redisMessageAdapter), otpTypesTopic());
        container.addMessageListener(servicesListenerAdapter(redisMessageAdapter), servicesTopic());
        container.addMessageListener(ttlConfigurationListenerAdapter(redisMessageAdapter), ttlConfigurationTopic());
        return container;
    }

}
