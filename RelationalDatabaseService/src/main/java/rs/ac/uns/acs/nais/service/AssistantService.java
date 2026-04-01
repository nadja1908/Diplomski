package rs.ac.uns.acs.nais.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import rs.ac.uns.acs.nais.config.NaisProperties;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final VectorSearchClient vectorSearchClient;
    private final NaisProperties naisProperties;
    private final RestClient.Builder restClientBuilder;

    public AssistantResponse answer(String question) {
        var matches = vectorSearchClient.search(question, 8);
        List<String> sources = matches.stream()
                .map(m -> m.predmetSifra() + " (" + m.fragmentType() + "): " + truncate(m.text(), 240))
                .collect(Collectors.toList());

        String context = matches.stream()
                .map(VectorSearchClient.VectorMatch::text)
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer;
        String apiKey = naisProperties.getLlm().getOpenaiApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            answer = callOpenAi(question, context, apiKey);
        } else {
            answer = fallbackAnswer(question, context, matches);
        }
        return new AssistantResponse(answer, sources);
    }

    private String callOpenAi(String question, String context, String apiKey) {
        try {
            Map<String, Object> body = Map.of(
                    "model", naisProperties.getLlm().getOpenaiModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "Ti si asistent za studentske informacije. Odgovaraj na srpskom, "
                                            + "kratko i tačno, samo na osnovu datog konteksta. Ako kontekst "
                                            + "nije dovoljan, reci to."),
                            Map.of("role", "user", "content",
                                    "Kontekst:\n" + context + "\n\nPitanje: " + question)
                    ),
                    "temperature", 0.3
            );
            String jsonBody = MAPPER.writeValueAsString(body);
            RestClient client = restClientBuilder
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            String raw = client.post()
                    .uri("/chat/completions")
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(raw);
            return root.path("choices").path(0).path("message").path("content").asText(
                    "Nije moguće generisati odgovor."
            );
        } catch (Exception e) {
            return fallbackAnswer(question, context, List.of());
        }
    }

    private String fallbackAnswer(String question, String context, List<VectorSearchClient.VectorMatch> matches) {
        if (context == null || context.isBlank()) {
            return "(LLM ključ nije podešen i vektorska pretraga nije vratila rezultate.) Odgovor na pitanje „"
                    + question + "” zahteva podešavanje OPENAI_API_KEY ili popunjenu Qdrant kolekciju.";
        }
        var sb = new StringBuilder();
        sb.append("Rezultati semantičke pretrage (bez LLM-a): pronađeno ").append(matches.size())
                .append(" segmenata. Sažetak konteksta:\n\n");
        sb.append(truncate(context, 4000));
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\r", " ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
