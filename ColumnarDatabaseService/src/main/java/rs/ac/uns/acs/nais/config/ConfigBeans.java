package rs.ac.uns.acs.nais.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ColumnarProperties.class)
public class ConfigBeans {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
