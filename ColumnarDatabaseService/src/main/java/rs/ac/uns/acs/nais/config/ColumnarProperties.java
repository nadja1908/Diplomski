package rs.ac.uns.acs.nais.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nais")
public class ColumnarProperties {

    private String relationalBaseUrl = "http://localhost:9030";
    private String internalApiKey = "";
    private final Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret = "";
    }
}
