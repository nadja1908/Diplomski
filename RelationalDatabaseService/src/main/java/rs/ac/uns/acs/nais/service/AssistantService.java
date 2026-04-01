package rs.ac.uns.acs.nais.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.config.NaisProperties;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Nakon deduplikacije po predmetu: zadrži samo pogotke čiji je skor blizu najboljeg.
     * Smanjuje „šum“ kad mnogi predmeti dele isti generički opis kursa u embedding prostoru.
     */
    private static final double VECTOR_SCORE_GAP_FROM_BEST = 0.10;
    /** Ne suviše visoko — kratki / jednorečeni upiti često imaju niži kosinus u odnosu na chunk. */
    private static final double VECTOR_MIN_ABSOLUTE_SCORE = 0.22;

    private static final Set<String> STOPWORDS = Set.of(
            "koji", "koja", "koje", "šta", "sta", "predmet", "predmeta", "predmeti", "predmete",
            "kurs", "kursa", "sadržaj", "sadrzaj", "obuhvata", "bavi", "bave", "nastave",
            "se", "i", "ili", "the", "what", "which", "how", "je", "su", "da", "li"
    );

    private final VectorSearchClient vectorSearchClient;
    private final NaisProperties naisProperties;
    private final RestClient.Builder restClientBuilder;
    private final StudentRepository studentRepository;
    private final PredmetRepository predmetRepository;

    public AssistantResponse answerForStudent(String question, Long korisnikId) {
        var student = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo studenti"));
        Long programId = student.getStudijskiProgram().getId();
        List<Long> allowed = predmetRepository.findIdsByStudijskiProgramId(programId);
        if (allowed.isEmpty()) {
            return new AssistantResponse(
                    "Na tvom studijskom programu još nema upisanih predmeta u sistemu.",
                    List.of()
            );
        }

        var matches = vectorSearchClient.search(question, 24, allowed);
        matches = dedupeByPredmetKeepBestScore(matches);
        matches = pruneMatchesByRelativeScore(matches);
        if (matches.isEmpty()) {
            return new AssistantResponse(
                    "Nijedan predmet sa tvog studijskog programa nije dovoljno sličan tom pitanju u vektorskoj bazi. "
                            + "Probaj drugačiju formulaciju ili proveri da li je Qdrant pun podacima.",
                    List.of()
            );
        }

        List<String> sources = matches.stream()
                .limit(8)
                .map(m -> String.format(
                        "ID %d · %s (%s) · %d ESPB · %s",
                        m.predmetId(),
                        m.predmetNaziv(),
                        m.predmetSifra(),
                        m.espb(),
                        m.profesor().isBlank() ? "profesor nije dodeljen u seed podacima" : m.profesor()
                ))
                .collect(Collectors.toList());

        String context = buildStructuredContext(matches);

        String answer;
        String apiKey = naisProperties.getLlm().getOpenaiApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            answer = callOpenAi(question, context, apiKey);
        } else {
            answer = fallbackAnswer(question, context, matches);
        }
        return new AssistantResponse(answer, sources);
    }

    private static String buildStructuredContext(List<VectorSearchClient.VectorMatch> matches) {
        var sb = new StringBuilder();
        for (VectorSearchClient.VectorMatch m : matches) {
            sb.append("---\n");
            sb.append("PREDMET_ID: ").append(m.predmetId()).append('\n');
            sb.append("ŠIFRA: ").append(m.predmetSifra()).append('\n');
            sb.append("NAZIV: ").append(m.predmetNaziv()).append('\n');
            sb.append("ESPB: ").append(m.espb()).append('\n');
            sb.append("PREDAVAČ: ").append(m.profesor()).append('\n');
            sb.append("CILJ (obrazovni): ").append(m.cilj()).append('\n');
            sb.append("ISHODI UČENJA: ").append(m.ishodiUcenja()).append('\n');
            sb.append("METODE NASTAVE: ").append(m.metodeNastave()).append('\n');
            sb.append("SADRŽAJ KURSA (TEME): ").append(m.temeKursa()).append('\n');
            if (!m.text().isBlank()) {
                sb.append("DODATNI TEKST ZA PRETRAGU: ").append(m.text()).append('\n');
            }
        }
        return sb.toString();
    }

    private String callOpenAi(String question, String context, String apiKey) {
        try {
            Map<String, Object> body = Map.of(
                    "model", naisProperties.getLlm().getOpenaiModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content",
                                    "Kontekst (samo predmeti sa studijskog programa studenta):\n"
                                            + context + "\n\nPitanje: " + question)
                    ),
                    "temperature", 0.2
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

    private static String systemPrompt() {
        return String.join("\n",
                "Ti si asistent za studentske informacije u sistemu NAIS.",
                "Odgovaraj na srpskom, jasno i kratko.",
                "Koristi ISKLJUČIVO podatke iz datog konteksta. Ne izmišljaj predmete.",
                "Kontekst sadrži samo predmete koje student ima na svom studijskom programu (kurikulum).",
                "Za svaki predmet koji je relevantan za pitanje, navedi: PREDMET_ID, šifru, naziv, ESPB, "
                        + "predavača, cilj, ishode učenja i sadržaj kursa (teme).",
                "Ako nijedan predmet iz konteksta ne odgovara pitanju, reci to."
        );
    }

    private String fallbackAnswer(String question, String context, List<VectorSearchClient.VectorMatch> matches) {
        if (context == null || context.isBlank()) {
            return "(LLM ključ nije podešen i vektorska pretraga nije vratila rezultate.) Za pitanje „"
                    + question + "” podesi OPENAI_API_KEY na relational-database-service ili popuni Qdrant.";
        }
        var sb = new StringBuilder();
        sb.append("Napomena: nije podešen OPENAI_API_KEY, pa nema generisanog čet-odgovora. ")
                .append("Ispod su predmeti sa tvog studijskog programa koji su najsličniji pitanju u Qdrantu ")
                .append("(to nije uvek doslovno „odgovor“ na pitanje).\n");
        if (matches.isEmpty()) {
            return sb.toString();
        }
        if (!questionMatchesAnyCourseTitle(question, matches)) {
            sb.append("\nVažno: nijedan naziv predmeta na tvom programu ne sadrži glavne reči iz pitanja ")
                    .append("(u demo podacima „Mašinsko učenje“ je na drugom studijskom programu). ")
                    .append("Semantička pretraga vraća srodne predmete sa tvog kurikuluma.\n");
        }
        int show = Math.min(4, matches.size());
        sb.append("\nNajsličniji predmeti (prvih ").append(show).append("):\n");
        for (int i = 0; i < show; i++) {
            VectorSearchClient.VectorMatch m = matches.get(i);
            sb.append("\n• ").append(m.predmetNaziv()).append(" (").append(m.predmetSifra()).append("), ")
                    .append(m.espb()).append(" ESPB — ")
                    .append(m.profesor().isBlank() ? "predavač —" : m.profesor()).append('\n')
                    .append("  ID: ").append(m.predmetId()).append('\n')
                    .append("  Sadržaj (skraćeno): ").append(truncate(m.temeKursa(), 320)).append('\n');
        }
        sb.append("\nZa kratak odgovor u prirodnom jeziku postavi OPENAI_API_KEY na relational-database-service.\n");
        return sb.toString();
    }

    private static boolean questionMatchesAnyCourseTitle(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        String foldedQ = foldSerbian(question);
        String[] tokens = foldedQ.split("[^a-zđščćž0-9]+");
        return Arrays.stream(tokens)
                .filter(t -> t.length() >= 4 && !STOPWORDS.contains(t))
                .anyMatch(token -> matches.stream()
                        .anyMatch(m -> foldSerbian(m.predmetNaziv()).contains(token)));
    }

    private static String foldSerbian(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String t = s.toLowerCase(Locale.ROOT);
        return t
                .replace('š', 's').replace('đ', 'd').replace('č', 'c').replace('ć', 'c').replace('ž', 'z');
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\r", " ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }

    /** Jedan red po predmetu — najbolji chunk (Qdrant često vrati više tačaka po kursu). */
    private static List<VectorSearchClient.VectorMatch> dedupeByPredmetKeepBestScore(
            List<VectorSearchClient.VectorMatch> matches
    ) {
        if (matches.isEmpty()) {
            return matches;
        }
        Map<Long, VectorSearchClient.VectorMatch> byId = new HashMap<>();
        for (VectorSearchClient.VectorMatch m : matches) {
            byId.merge(m.predmetId(), m, (a, b) -> a.score() >= b.score() ? a : b);
        }
        return byId.values().stream()
                .sorted(Comparator.comparingDouble(VectorSearchClient.VectorMatch::score).reversed())
                .toList();
    }

    private static List<VectorSearchClient.VectorMatch> pruneMatchesByRelativeScore(
            List<VectorSearchClient.VectorMatch> matches
    ) {
        if (matches.isEmpty()) {
            return matches;
        }
        double best = matches.get(0).score();
        double floor = Math.max(VECTOR_MIN_ABSOLUTE_SCORE, best - VECTOR_SCORE_GAP_FROM_BEST);
        List<VectorSearchClient.VectorMatch> kept = matches.stream()
                .filter(m -> m.score() >= floor)
                .toList();
        return kept.isEmpty() ? List.of(matches.get(0)) : kept;
    }
}
