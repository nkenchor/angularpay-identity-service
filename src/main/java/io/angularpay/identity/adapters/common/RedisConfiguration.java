package io.angularpay.identity.adapters.common;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

    @Bean
    public JedisConnectionFactory connectionFactory(AngularPayConfiguration angularPayConfiguration) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(angularPayConfiguration.getRedis().getHost());
        configuration.setPort(angularPayConfiguration.getRedis().getPort());
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}
