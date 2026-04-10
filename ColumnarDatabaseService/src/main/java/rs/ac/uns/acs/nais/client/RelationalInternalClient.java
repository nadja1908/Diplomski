package rs.ac.uns.acs.nais.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import rs.ac.uns.acs.nais.config.ColumnarProperties;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse;
import rs.ac.uns.acs.nais.internal.dto.StudentProgramPredmetMin;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RelationalInternalClient {

    private final ColumnarProperties properties;
    private final RestClient.Builder restClientBuilder;

    private RestClient client() {
        return restClientBuilder.baseUrl(properties.getRelationalBaseUrl()).build();
    }

    private void internalHeaders(org.springframework.http.HttpHeaders h) {
        h.set("X-Internal-Key", properties.getInternalApiKey());
    }

    public StatisticsAggregatesResponse fetchAggregates() {
        return client().get()
                .uri("/internal/statistics/aggregates")
                .headers(this::internalHeaders)
                .retrieve()
                .body(StatisticsAggregatesResponse.class);
    }

    public List<Long> studentPredmetIds(Long korisnikId) {
        return client().get()
                .uri("/internal/student/korisnik/{id}/predmet-ids", korisnikId)
                .headers(this::internalHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Long>>() {
                });
    }

    public List<StudentProgramPredmetMin> studentProgramPredmeti(Long korisnikId) {
        return client().get()
                .uri("/internal/student/korisnik/{id}/program-predmeti", korisnikId)
                .headers(this::internalHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StudentProgramPredmetMin>>() {
                });
    }

    public Long katedraForSef(Long korisnikId) {
        return client().get()
                .uri("/internal/sef/korisnik/{id}/katedra-id", korisnikId)
                .headers(this::internalHeaders)
                .retrieve()
                .body(Long.class);
    }

    public List<Long> predmetIdsForKatedra(Long katedraId) {
        return client().get()
                .uri("/internal/katedra/{id}/predmet-ids", katedraId)
                .headers(this::internalHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Long>>() {
                });
    }
}
