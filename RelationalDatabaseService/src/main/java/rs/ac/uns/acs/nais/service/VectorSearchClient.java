package rs.ac.uns.acs.nais.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import rs.ac.uns.acs.nais.config.NaisProperties;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VectorSearchClient {

    private final NaisProperties naisProperties;
    private final RestClient.Builder restClientBuilder;

    public List<VectorMatch> search(String query, int limit) {
        String uri = UriComponentsBuilder
                .fromUriString(naisProperties.getVectorService().getBaseUrl() + "/api/v1/search")
                .queryParam("q", query)
                .queryParam("limit", limit)
                .build()
                .toUriString();

        RestClient client = restClientBuilder.build();
        try {
            Map<String, Object> body = client.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (body == null || !body.containsKey("results")) {
                return List.of();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
            return results.stream().map(this::mapRow).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private VectorMatch mapRow(Map<String, Object> row) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = row.get("payload") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        String text = payload.getOrDefault("text", "").toString();
        String predmet = payload.getOrDefault("predmet_sifra", "").toString();
        String tip = payload.getOrDefault("tip", "").toString();
        double score = row.get("score") instanceof Number n ? n.doubleValue() : 0;
        return new VectorMatch(text, predmet, tip, score);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VectorMatch(String text, String predmetSifra, String fragmentType, double score) {
    }
}
