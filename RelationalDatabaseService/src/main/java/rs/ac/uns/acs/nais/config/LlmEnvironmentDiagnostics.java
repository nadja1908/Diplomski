package rs.ac.uns.acs.nais.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Jednokratno pri startu: da u Docker logu vidiš da li je Groq ključ stvarno u kontejneru.
 */
@Slf4j
@Component
@Order(10_000)
@RequiredArgsConstructor
public class LlmEnvironmentDiagnostics implements ApplicationRunner {

    private final NaisProperties naisProperties;

    @Override
    public void run(ApplicationArguments args) {
        String g = System.getenv("GROQ_API_KEY");
        boolean hasKey = g != null && !g.isBlank();
        if (hasKey) {
            log.info(
                    "Groq LLM: GROQ_API_KEY je učitan (dužina {}), baseUrl={}, model={}",
                    g.trim().length(),
                    naisProperties.getLlm().getOpenaiBaseUrl(),
                    naisProperties.getLlm().getOpenaiModel());
        } else {
            log.warn(
                    "Groq LLM: GROQ_API_KEY NEDOSTAJE u okruženju — asistent ide bez LLM-a. "
                            + "U fajl .env pored docker-compose.yml dodaj GROQ_API_KEY=gsk_... (bez razmaka oko =), "
                            + "pa: docker compose up -d --force-recreate relational-database-service");
        }
    }
}
