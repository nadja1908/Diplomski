package rs.ac.uns.acs.nais.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NaisProperties.class)
public class ConfigBeans {
}
