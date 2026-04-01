package rs.ac.uns.acs.nais.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import rs.ac.uns.acs.nais.config.NaisProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchClient {

    private final NaisProperties naisProperties;
    private final RestClient.Builder restClientBuilder;

    public List<VectorMatch> search(String query, int limit, List<Long> predmetIds) {
        String url = naisProperties.getVectorService().getBaseUrl() + "/api/v1/search";
        RestClient client = restClientBuilder.build();
        try {
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("q", query);
            req.put("limit", limit);
            if (predmetIds != null && !predmetIds.isEmpty()) {
                req.put("predmet_ids", predmetIds);
            }
            Map<String, Object> body = client.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
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
            log.warn("Vector search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private VectorMatch mapRow(Map<String, Object> row) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = row.get("payload") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        double score = row.get("score") instanceof Number n ? n.doubleValue() : 0;
        long predmetId = toLong(payload.get("predmet_id"));
        int espb = payload.get("espb") instanceof Number n ? n.intValue() : 0;
        return new VectorMatch(
                predmetId,
                str(payload.get("predmet_sifra")),
                str(payload.get("predmet_naziv")),
                espb,
                str(payload.get("profesor")),
                str(payload.get("cilj")),
                str(payload.get("ishodi_ucenja")),
                str(payload.get("teme_kursa")),
                str(payload.get("metode_nastave")),
                str(payload.get("text")),
                str(payload.get("tip")),
                score
        );
    }

    private static long toLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        if (o != null) {
            try {
                return Long.parseLong(o.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VectorMatch(
            long predmetId,
            String predmetSifra,
            String predmetNaziv,
            int espb,
            String profesor,
            String cilj,
            String ishodiUcenja,
            String temeKursa,
            String metodeNastave,
            String text,
            String fragmentType,
            double score
    ) {
    }
}
