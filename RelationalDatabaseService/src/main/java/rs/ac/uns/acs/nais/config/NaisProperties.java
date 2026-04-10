package rs.ac.uns.acs.nais.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nais")
public class NaisProperties {

    private String internalApiSecret = "";
    private final Jwt jwt = new Jwt();
    private final VectorService vectorService = new VectorService();
    private final Llm llm = new Llm();

    @Data
    public static class Jwt {
        private String secret = "nais-demo-secret-change-me-for-demo-32b";
        private long expirationMs = 86400000L;
    }

    @Data
    public static class VectorService {
        private String baseUrl = "http://localhost:8000";
    }

    @Data
    public static class Llm {
        private String openaiApiKey = "";
        private String openaiBaseUrl = "https://api.groq.com/openai/v1";
        private String openaiModel = "llama-3.3-70b-versatile";
        private double openaiTemperature = 0.4;
        private int openaiMaxTokens = 900;
    }
}
