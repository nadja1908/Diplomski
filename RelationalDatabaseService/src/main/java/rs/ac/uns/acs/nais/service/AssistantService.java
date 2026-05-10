package rs.ac.uns.acs.nais.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.config.NaisProperties;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.SadrzajPredmeta;
import rs.ac.uns.acs.nais.domain.Student;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.ProgramStatisticsResponse;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.RankingsBundle;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsQueryParams;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.SubjectStatisticsRow;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.SadrzajPredmetaRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final double VECTOR_SCORE_GAP_FROM_BEST = 0.14;
    private static final double VECTOR_MIN_ABSOLUTE_SCORE = 0.22;

    /** U leksičkom fallbacku (kad Qdrant vrati 0 pogodaka) ignoriši ove reči — ne donose sadržaj teme. */
    private static final Set<String> LEXICAL_FALLBACK_SKIP = Set.of(
            "imam", "imas", "imate", "imaju", "imamo", "bio", "bila", "bili", "bile");

    private static final String MSG_VECTOR_LLM_EMPTY =
            "Jezički model je vratio prazan odgovor. Probaj drugačije pitanje ili proveri OPENAI_MODEL i OPENAI_BASE_URL.";
    /** Direktno iz SQL evidencije — ne zahteva API ključ za LLM. */
    private static final String SRC_RELATIONAL =
            "Izvor: PostgreSQL (odgovor iz baze, bez jezičkog modela).";
    private static final String SRC_SYLLABUS =
            "Izvor: PostgreSQL + Qdrant fragmenti predmeta (bez LLM).";
    private static final String SRC_SYLLABUS_PG =
            "Izvor: PostgreSQL (sadržaj predmeta; bez Qdrant fragmenta).";
    private static final String SRC_VECTOR_MISS =
            "Izvor: Qdrant nije našao pogodak za ovo pitanje.";
    /** Semantički pogodaci iz Qdranta, bez poziva LLM-a (kada nema API ključa ili kao rezerva). */
    private static final String SRC_VECTOR_NO_LLM =
            "Izvor: Qdrant (semantička pretraga; bez LLM — za prirodniji odgovor podesi GROQ_API_KEY i ponovo podigni servis).";
    private static final String SRC_PROGRAM_STATS =
            "Izvor: statistika programa (isti proračun kao u delu Statistika na portalu).";

    private static final Set<String> STOPWORDS = Set.copyOf(Arrays.asList(
            "koji", "koja", "koje", "koju", "čiji", "ciji", "šta", "sta", "predmet", "predmeta", "predmeti", "predmete",
            "kurs", "kursa", "sadržaj", "sadrzaj", "obuhvata", "bavi", "bave", "nastave",
            "se", "i", "ili", "the", "what", "which", "how", "je", "su", "da", "li", "bi", "bih",
            "zanima", "zanimaju", "raditi", "radis", "radim", "radimo", "radite", "cemo", "ćemo", "cu", "ću",
            "hocu", "hoću", "zelim", "želim", "znaci", "znači", "ovako", "sve", "ovo", "onako", "pls", "molim",
            "meni", "mene", "tebe", "nama", "vama", "jos", "još", "vec", "već", "bas", "baš",
            "treba", "mora", "moram", "mogu", "mozes", "možeš", "mozemo", "možemo", "biti", "bio", "bila",
            "ovo", "tamo", "ovde", "ovdje", "gde", "gdje", "kada", "kad", "samo", "jos", "još"
    ));

    private static final String[] SR_STEM_SUFFIXES = {
            "ovima", "avanje", "ovanje", "ivali", "ivao", "ujes", "ujem", "uješ",
            "ama", "ima", "omu", "ome", "oj", "og", "om", "em", "im", "ih", "ci", "ca", "cu", "ce"
    };

    private final VectorSearchClient vectorSearchClient;
    private final NaisProperties naisProperties;
    private final RestClient.Builder restClientBuilder;
    private final StudentRepository studentRepository;
    private final PredmetRepository predmetRepository;
    private final AcademicQueryService academicQueryService;
    private final SadrzajPredmetaRepository sadrzajPredmetaRepository;
    private final ProgramSubjectAnalyticsService programSubjectAnalyticsService;
    private final AssistantIntentClassifier assistantIntentClassifier;
    private final ConcurrentMap<Long, Long> lastReferencedPredmetByKorisnikId = new ConcurrentHashMap<>();

    public AssistantResponse answerForStudent(String question, Long korisnikId) {
        if (korisnikId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niste prijavljeni (nedostaje korisnik iz tokena).");
        }
        var student = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Korisnik nema studentski zapis u bazi (nema veze student–korisnik). "
                                + "Asistent radi samo za naloge koji su u tabeli student."));
        Long programId = student.getStudijskiProgram().getId();
        List<Long> allowed = predmetRepository.findIdsByStudijskiProgramId(programId);

        if (allowed.isEmpty()) {
            return new AssistantResponse(
                    "Na tvom studijskom programu još nema upisanih predmeta u sistemu.",
                    List.of(),
                    SRC_RELATIONAL);
        }

        ParsedQuestion parsed = parseQuestionWithLlm(question);
        String routingQuestion = buildRoutingQuestionFromParsed(question, parsed);
        String fq = foldSerbian(routingQuestion);
        AssistantIntentDecision intent = parsed.valid()
                ? decisionFromParsedIntent(parsed)
                : assistantIntentClassifier.classify(routingQuestion, fq);
        log.debug("Assistant intent={}, confidence={}, rationale={}",
                intent.type(), intent.confidence(), intent.rationale());

        if (intent.is(AssistantIntentType.CURRICULUM_LIST) || asksForFullCurriculumList(routingQuestion)) {
            return buildCurriculumListResponse(student, programId, routingQuestion);
        }

        rememberExplicitSubjectMentionFromQuestion(routingQuestion, programId, korisnikId);
        if (intent.is(AssistantIntentType.SUBJECT_PRESENCE)) {
            AssistantResponse subjectPresence = trySubjectPresenceResponse(routingQuestion, fq, programId, allowed, korisnikId);
            if (subjectPresence != null) {
                return subjectPresence;
            }
        }
        if (wantsUnpassedSubjects(fq)) {
            return buildUnpassedSubjectsResponse(korisnikId, programId, question);
        }
        if (wantsCurriculumRemainder(fq)) {
            return buildCurriculumRemainderResponse(korisnikId, question);
        }
        if (wantsTotalExamAttempts(fq)) {
            return buildExamAttemptsSummaryResponse(korisnikId, question);
        }

        AssistantResponse statsRanking = tryStatisticsRankingsResponse(student, programId, fq, routingQuestion);
        if (statsRanking != null) {
            return statsRanking;
        }
        AssistantResponse subjectPassRate = trySubjectPassRateResponse(student, programId, fq, routingQuestion, allowed);
        if (subjectPassRate != null) {
            return subjectPassRate;
        }

        AssistantResponse syllabus = trySubjectSyllabusDetailResponse(routingQuestion, fq, programId, allowed, korisnikId);
        if (syllabus != null) {
            return syllabus;
        }

        if (asksEverythingAboutStudent(fq)) {
            return buildStudentDataResponse(korisnikId, true, true, true, question);
        }
        if (wantsPassRokSummary(fq)) {
            return buildPassRokSummaryResponse(korisnikId, question, programId);
        }
        boolean wantProf = wantsProfileSection(question, fq);
        boolean wantGr = wantsGradesSection(fq);
        boolean wantGpa = wantsGpaSection(fq);
        if (wantProf || wantGr || wantGpa) {
            return buildStudentDataResponse(korisnikId, wantProf, wantGr, wantGpa, routingQuestion);
        }

        List<VectorSearchClient.VectorMatch> matches = searchVectorWithExplicitCourseBoost(routingQuestion, allowed, programId);
        if (matches.isEmpty()) {
            String hint = naisProperties.getAssistant().isQdrantEnabled()
                    ? "Među predmetima na tvom programu nisam našao pouzdan pogodak za ovo pitanje u pretrazi kurikuluma. "
                    + "Probaj drugačije reči ili tačan naziv kursa."
                    : "Među predmetima na tvom programu nisam našao pogodak po nazivu i kratkom opisu za ove reči. "
                    + "Probaj tačan naziv predmeta ili pitanje za sadržaj konkretnog kursa.";
            return new AssistantResponse(hint, List.of(), naisProperties.getAssistant().isQdrantEnabled()
                    ? SRC_VECTOR_MISS
                    : SRC_RELATIONAL);
        }

        List<String> sources = matches.stream()
                .limit(8)
                .map(m -> String.format(
                        "ID %d · %s (%s) · %d ESPB · %s",
                        m.predmetId(),
                        m.predmetNaziv(),
                        m.predmetSifra(),
                        m.espb(),
                        m.profesor().isBlank() ? "predavač nije dodeljen u nastavnoj evidenciji" : m.profesor()
                ))
                .collect(Collectors.toList());

        List<VectorSearchClient.VectorMatch> forLlm = orderMatchesForLlm(routingQuestion, matches);
        rememberContextSubjectFromMatches(korisnikId, routingQuestion, forLlm);

        if (effectiveLlmApiKey().isBlank()) {
            return new AssistantResponse(vectorFallbackAnswerFromMatches(forLlm), sources, SRC_VECTOR_NO_LLM);
        }

        String context = buildStructuredContext(forLlm);
        LlmOrFallback gen = callOpenAi(routingQuestion, context, effectiveLlmApiKey());
        if (gen.usedLlm()) {
            return new AssistantResponse(gen.text(), sources, buildLlmSuccessAnswerSource(matchesInvolveQdrant(forLlm)));
        }
        String fallback = vectorFallbackAnswerFromMatches(forLlm);
        String note =
                "—\nJezički model nije vratio odgovor (proveri GROQ_API_KEY, OPENAI_BASE_URL za Groq, OPENAI_MODEL i mrežu). "
                        + (matchesInvolveQdrant(forLlm)
                        ? "Gore je sažetak iz pretrage kurikuluma (Qdrant / tekst predmeta)."
                        : "Gore je sažetak iz evidencije predmeta u bazi (PostgreSQL).");
        return new AssistantResponse(fallback + "\n\n" + note, sources, SRC_VECTOR_NO_LLM);
    }

    /** Oznaka za korisnika (čet): jasno „GPT“ ili „Llama“ + provajder i tačan model. */
    private String buildLlmSuccessAnswerSource(boolean contextUsesQdrant) {
        String model = Optional.ofNullable(naisProperties.getLlm().getOpenaiModel()).orElse("").trim();
        if (model.isEmpty()) {
            model = "nepoznat model";
        }
        String baseUrl = Optional.ofNullable(naisProperties.getLlm().getOpenaiBaseUrl()).orElse("").trim().toLowerCase(Locale.ROOT);
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String m = model.toLowerCase(Locale.ROOT);
        boolean groq = baseUrl.contains("groq.com");
        boolean openaiOfficial = baseUrl.contains("api.openai.com") || baseUrl.endsWith("openai.com/v1");

        String family;
        if (m.contains("llama") || m.contains("mixtral") || m.contains("gemma")) {
            if (m.contains("llama")) {
                family = "Llama";
            } else if (m.contains("mixtral")) {
                family = "Mixtral";
            } else {
                family = "Gemma";
            }
        } else if (m.contains("gpt") || m.startsWith("o1") || m.startsWith("o3") || m.startsWith("o4")) {
            family = "GPT";
        } else if (groq) {
            family = "Llama";
        } else if (openaiOfficial || baseUrl.isEmpty() || baseUrl.equals("https://api.openai.com/v1")) {
            family = "GPT";
        } else {
            family = "LLM";
        }

        String via;
        if (groq) {
            via = "Groq";
        } else if (openaiOfficial || baseUrl.isEmpty() || baseUrl.contains("openai.com")) {
            via = "OpenAI";
        } else {
            via = "kompatibilan OpenAI API";
        }

        String ctx = contextUsesQdrant ? "Qdrant + kurikulum (PostgreSQL)" : "PostgreSQL kurikulum (bez Qdranta)";
        return "Odgovor generisao " + family + " preko " + via + " · model: " + model + " · kontekst: " + ctx + ".";
    }

    private record LlmOrFallback(String text, boolean usedLlm) {
    }

    private record ParsedQuestion(
            AssistantIntentType intent,
            String subjectName,
            String subjectCode,
            String topic,
            double confidence,
            boolean valid
    ) {
        static ParsedQuestion invalid() {
            return new ParsedQuestion(AssistantIntentType.UNKNOWN, "", "", "", 0.0, false);
        }
    }

    private ParsedQuestion parseQuestionWithLlm(String question) {
        String apiKey = effectiveLlmApiKey();
        if (apiKey.isBlank()) {
            return ParsedQuestion.invalid();
        }
        try {
            String prompt = """
                    Izvuci strukturu pitanja studenta kao JSON.
                    Vrati SAMO JSON objekat oblika:
                    {
                      "intent": "CURRICULUM_LIST|SUBJECT_PRESENCE|SUBJECT_DETAIL|STATISTICS_RANKING|UNPASSED_SUBJECTS|CURRICULUM_REMAINDER|EXAM_ATTEMPTS|PASS_ROK_SUMMARY|STUDENT_ALL|STUDENT_PROFILE|STUDENT_GRADES|STUDENT_GPA|SEMANTIC_TEXT_SEARCH|HYBRID_FILTERED_SEMANTIC|UNKNOWN",
                      "subject_name": "",
                      "subject_code": "",
                      "topic": "",
                      "confidence": 0.0
                    }
                    Pravila:
                    - Ako korisnik traži sadržaj/ishode/cilj/metode/teme jednog predmeta -> SUBJECT_DETAIL
                    - Ako pita "da li imam predmet X" -> SUBJECT_PRESENCE
                    - Ako pita najtezi/najlaksi/prolaznost rang -> STATISTICS_RANKING
                    - Ako traži semantičko spominjanje termina po predmetima -> SEMANTIC_TEXT_SEARCH
                    - Ako nisi siguran -> UNKNOWN i confidence < 0.5
                    """;
            var body = new LinkedHashMap<String, Object>();
            body.put("model", naisProperties.getLlm().getOpenaiModel());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "Ti si parser namere. Vrati isključivo validan JSON, bez teksta."),
                    Map.of("role", "user", "content", prompt + "\n\nPITANJE: " + question)
            ));
            body.put("temperature", 0.0);
            body.put("max_tokens", 220);

            String jsonBody = MAPPER.writeValueAsString(body);
            String baseUrl = naisProperties.getLlm().getOpenaiBaseUrl().trim();
            while (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            if (baseUrl.isEmpty()) {
                baseUrl = "https://api.openai.com/v1";
            }
            RestClient client = restClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            String raw = client.post()
                    .uri("/chat/completions")
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (content.isBlank()) {
                return ParsedQuestion.invalid();
            }
            String json = extractJsonObject(content);
            if (json.isBlank()) {
                return ParsedQuestion.invalid();
            }
            JsonNode p = MAPPER.readTree(json);
            AssistantIntentType intent = parseIntentType(p.path("intent").asText("UNKNOWN"));
            String subjectName = p.path("subject_name").asText("");
            String subjectCode = p.path("subject_code").asText("");
            String topic = p.path("topic").asText("");
            double confidence = p.path("confidence").asDouble(0.0);
            boolean valid = confidence >= 0.45 && intent != AssistantIntentType.UNKNOWN;
            return new ParsedQuestion(intent, subjectName.trim(), subjectCode.trim(), topic.trim(), confidence, valid);
        } catch (Exception e) {
            log.debug("LLM parse failed: {}", e.getMessage());
            return ParsedQuestion.invalid();
        }
    }

    private static String buildRoutingQuestionFromParsed(String original, ParsedQuestion p) {
        if (p == null || !p.valid()) {
            return original;
        }
        StringBuilder sb = new StringBuilder(original == null ? "" : original.trim());
        if (p.subjectName() != null && !p.subjectName().isBlank() && !foldSerbian(sb.toString()).contains(foldSerbian(p.subjectName()))) {
            sb.append(" ").append(p.subjectName());
        }
        if (p.subjectCode() != null && !p.subjectCode().isBlank() && !sb.toString().toLowerCase(Locale.ROOT).contains(p.subjectCode().toLowerCase(Locale.ROOT))) {
            sb.append(" ").append(p.subjectCode());
        }
        if (p.topic() != null && !p.topic().isBlank() && !foldSerbian(sb.toString()).contains(foldSerbian(p.topic()))) {
            sb.append(" ").append(p.topic());
        }
        return sb.toString().trim();
    }

    private static AssistantIntentDecision decisionFromParsedIntent(ParsedQuestion p) {
        return switch (p.intent()) {
            case CURRICULUM_LIST -> AssistantIntentDecision.of(p.intent(), p.confidence(), true, false, false, "LLM parser");
            case SUBJECT_PRESENCE -> AssistantIntentDecision.of(p.intent(), p.confidence(), true, false, false, "LLM parser");
            case SUBJECT_DETAIL -> AssistantIntentDecision.of(p.intent(), p.confidence(), true, false, true, "LLM parser");
            case STATISTICS_RANKING -> AssistantIntentDecision.of(p.intent(), p.confidence(), false, true, false, "LLM parser");
            case SEMANTIC_TEXT_SEARCH -> AssistantIntentDecision.of(p.intent(), p.confidence(), true, false, true, "LLM parser");
            case HYBRID_FILTERED_SEMANTIC -> AssistantIntentDecision.of(p.intent(), p.confidence(), true, true, true, "LLM parser");
            case UNPASSED_SUBJECTS, CURRICULUM_REMAINDER, EXAM_ATTEMPTS, PASS_ROK_SUMMARY,
                    STUDENT_ALL, STUDENT_PROFILE, STUDENT_GRADES, STUDENT_GPA ->
                    AssistantIntentDecision.of(p.intent(), p.confidence(), true, true, false, "LLM parser");
            default -> AssistantIntentDecision.of(AssistantIntentType.UNKNOWN, 0.0, true, false, true, "LLM parser unknown");
        };
    }

    private static AssistantIntentType parseIntentType(String raw) {
        if (raw == null || raw.isBlank()) {
            return AssistantIntentType.UNKNOWN;
        }
        try {
            return AssistantIntentType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return AssistantIntentType.UNKNOWN;
        }
    }

    private static String extractJsonObject(String text) {
        int s = text.indexOf('{');
        int e = text.lastIndexOf('}');
        if (s < 0 || e <= s) {
            return "";
        }
        return text.substring(s, e + 1);
    }

    private List<VectorSearchClient.VectorMatch> searchVectorWithExplicitCourseBoost(
            String question,
            List<Long> allowed,
            Long programId
    ) {
        List<VectorSearchClient.VectorMatch> lexical = dedupeByPredmetKeepBestScore(
                fallbackLexicalCurriculumHits(question, programId, allowed));
        Optional<Predmet> explicit = findPredmetExplicitlyNamedInQuestion(question, programId);

        List<VectorSearchClient.VectorMatch> broad;
        if (!naisProperties.getAssistant().isQdrantEnabled()) {
            broad = lexical;
        } else if (lexicalStrongConfidence(lexical)) {
            broad = lexical;
        } else {
            broad = vectorSearchClient.search(question, 32, allowed);
            broad = dedupeByPredmetKeepBestScore(broad);
            if (broad.isEmpty()) {
                broad = lexical;
            }
        }

        if (explicit.isEmpty()) {
            return pruneMatchesByRelativeScore(broad);
        }
        Predmet p = explicit.get();
        VectorSearchClient.VectorMatch anchor = buildSyntheticMatchFromPredmet(p);
        List<VectorSearchClient.VectorMatch> rest = broad.stream()
                .filter(m -> m.predmetId() != p.getId())
                .toList();
        rest = pruneMatchesByRelativeScore(rest);
        List<VectorSearchClient.VectorMatch> out = new ArrayList<>();
        out.add(anchor);
        out.addAll(rest);
        return out;
    }

    private AssistantResponse trySubjectPresenceResponse(
            String question,
            String fq,
            Long programId,
            List<Long> allowed,
            Long korisnikId
    ) {
        if (!isSubjectPresenceQuestion(fq)) {
            return null;
        }
        Optional<Predmet> p = resolvePredmetForPresenceQuestion(question, programId, allowed);
        if (p.isEmpty()) {
            return new AssistantResponse(
                    "U tvom kurikulumu nisam pronašao predmet koji se poklapa sa tim nazivom.",
                    List.of(),
                    SRC_RELATIONAL);
        }
        Predmet predmet = p.get();
        if (korisnikId != null && predmet.getId() != null) {
            lastReferencedPredmetByKorisnikId.put(korisnikId, predmet.getId());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Da, imate predmet ")
                .append(predmet.getNaziv())
                .append(", šifra ")
                .append(predmet.getSifra())
                .append(", ")
                .append(predmet.getEspb())
                .append(" ESPB.");
        if (predmet.getKratakOpis() != null && !predmet.getKratakOpis().isBlank()) {
            sb.append(" ").append(predmet.getKratakOpis().trim());
        }
        return new AssistantResponse(
                sb.toString().trim(),
                List.of(String.format("ID %d · %s (%s) · %d ESPB",
                        predmet.getId(), predmet.getNaziv(), predmet.getSifra(), predmet.getEspb())),
                SRC_RELATIONAL
        );
    }

    private void rememberExplicitSubjectMentionFromQuestion(String question, Long programId, Long korisnikId) {
        if (korisnikId == null || programId == null || question == null || question.isBlank()) {
            return;
        }
        Optional<Predmet> explicit = findPredmetExplicitlyNamedInQuestion(question, programId);
        if (explicit.isEmpty()) {
            return;
        }
        Long predmetId = explicit.get().getId();
        if (predmetId != null) {
            lastReferencedPredmetByKorisnikId.put(korisnikId, predmetId);
        }
    }

    private Optional<Predmet> resolvePredmetForPresenceQuestion(
            String question,
            Long programId,
            List<Long> allowed
    ) {
        Optional<Predmet> named = findPredmetExplicitlyNamedInQuestion(question, programId);
        if (named.isPresent()) {
            return named;
        }
        List<VectorSearchClient.VectorMatch> lexical = dedupeByPredmetKeepBestScore(
                fallbackLexicalCurriculumHits(question, programId, allowed));
        if (!lexical.isEmpty() && lexical.get(0).score() >= 0.47) {
            return predmetRepository.findById(lexical.get(0).predmetId());
        }
        return resolvePredmetByTitleTypo(question, programId);
    }

    private Optional<Predmet> resolvePredmetByTitleTypo(String question, Long programId) {
        List<String> qTokens = significantQuestionTokens(question);
        if (qTokens.isEmpty()) {
            return Optional.empty();
        }
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        Predmet best = null;
        double bestScore = 0.0;
        for (Predmet p : all) {
            String title = foldSerbian(p.getNaziv());
            String[] words = title.split("\\s+");
            double total = 0.0;
            int matchedWords = 0;
            for (String w : words) {
                if (w.length() < 5 || STOPWORDS.contains(w)) {
                    continue;
                }
                double localBest = 0.0;
                for (String qt : qTokens) {
                    double sim = tokenSimilarity(qt, w);
                    if (sim > localBest) {
                        localBest = sim;
                    }
                }
                if (localBest >= 0.72) {
                    matchedWords++;
                    total += localBest;
                }
            }
            if (matchedWords == 0) {
                continue;
            }
            double score = total / matchedWords;
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }
        if (best != null && bestScore >= 0.78) {
            return Optional.of(best);
        }
        return Optional.empty();
    }

    private static List<String> significantQuestionTokens(String question) {
        String[] toks = foldSerbian(question).split("[^a-z0-9]+");
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String t : toks) {
            if (t.length() < 5) {
                continue;
            }
            if (STOPWORDS.contains(t)) {
                continue;
            }
            if ("predmet".equals(t) || "predmeta".equals(t) || "kursa".equals(t) || "kurs".equals(t)) {
                continue;
            }
            out.add(t);
        }
        return new ArrayList<>(out);
    }

    private static double tokenSimilarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        int dist = levenshteinDistance(a, b);
        int max = Math.max(a.length(), b.length());
        if (max == 0) {
            return 0.0;
        }
        return 1.0 - ((double) dist / max);
    }

    private static int levenshteinDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(
                        Math.min(cur[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[m];
    }

    private static boolean isSubjectPresenceQuestion(String f) {
        boolean ask = f.contains("da li imam")
                || f.contains("imam li")
                || f.contains("jel imam")
                || f.contains("je l imam");
        if (!ask) {
            return false;
        }
        if (!(f.contains("predmet") || f.contains("kurs") || f.contains("kolegij"))) {
            return false;
        }
        // Ovo su upiti o sadržaju/temi predmeta, ne pitanje "da li predmet postoji u mom planu".
        if (f.contains("spominj")
                || f.contains("pominj")
                || f.contains("sadrz")
                || f.contains("obuhvat")
                || f.contains("bavi")
                || f.contains("gde se")
                || f.contains("gdje se")) {
            return false;
        }
        if (f.contains("ocen") || f.contains("prosek") || f.contains("rok") || f.contains("poloz")) {
            return false;
        }
        return true;
    }

    private void rememberContextSubjectFromMatches(
            Long korisnikId,
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        if (korisnikId == null || matches == null || matches.isEmpty()) {
            return;
        }
        VectorSearchClient.VectorMatch main = resolvePrimaryForNarrative(question, matches);
        long predmetId = main.predmetId();
        if (predmetId > 0) {
            lastReferencedPredmetByKorisnikId.put(korisnikId, predmetId);
        }
    }

    private static VectorSearchClient.VectorMatch buildSyntheticMatchFromPredmet(Predmet p) {
        String ko = p.getKratakOpis() == null ? "" : p.getKratakOpis().trim();
        if (!ko.isBlank()) {
            return new VectorSearchClient.VectorMatch(
                    p.getId(),
                    p.getSifra(),
                    p.getNaziv(),
                    p.getEspb(),
                    "",
                    "",
                    "",
                    ko,
                    "",
                    ko,
                    "sql_kratak_opis",
                    0.95
            );
        }
        return new VectorSearchClient.VectorMatch(
                p.getId(),
                p.getSifra(),
                p.getNaziv(),
                p.getEspb(),
                "",
                "",
                "",
                "",
                "",
                "",
                "sql_samo_naziv",
                0.9
        );
    }

    /**
     * Dovoljno jak lokalni pogodak (naziv / kratak opis u PostgreSQL-u) da se preskoči semantička pretraga.
     */
    private static boolean lexicalStrongConfidence(List<VectorSearchClient.VectorMatch> lexical) {
        if (lexical.isEmpty()) {
            return false;
        }
        VectorSearchClient.VectorMatch top = lexical.get(0);
        if (!"pg_naziv_opis_fallback".equals(top.fragmentType()) || top.score() < 0.52) {
            return false;
        }
        if (lexical.size() >= 2 && lexical.get(1).score() >= top.score() - 0.04) {
            return false;
        }
        return true;
    }

    private static boolean matchesInvolveQdrant(List<VectorSearchClient.VectorMatch> matches) {
        for (VectorSearchClient.VectorMatch m : matches) {
            String t = m.fragmentType() == null ? "" : m.fragmentType().trim();
            if ("pg_naziv_opis_fallback".equals(t) || t.startsWith("sql_")) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Kada vektorski servis / Qdrant ne vrati ništa (prazna kolekcija, mreža, nepoklapanje id-jeva),
     * ipak pokušaj naziv + kratak opis predmeta na programu — npr. „engleski jezik“ / „English“.
     */
    private List<VectorSearchClient.VectorMatch> fallbackLexicalCurriculumHits(
            String question,
            Long programId,
            List<Long> allowed
    ) {
        LinkedHashSet<String> needles = new LinkedHashSet<>(extractQuestionStems(question));
        needles.addAll(looseCurriculumTokens(question));
        addLanguageCourseAliasNeedles(foldSerbian(question), needles);
        needles.removeIf(n -> n.length() < 3 || LEXICAL_FALLBACK_SKIP.contains(n));
        if (needles.isEmpty()) {
            return List.of();
        }
        Set<Long> allow = new HashSet<>(allowed);
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        record Hit(Predmet p, int score, int titleMatches) {
        }
        List<Hit> hits = new ArrayList<>();
        for (Predmet p : all) {
            if (!allow.contains(p.getId())) {
                continue;
            }
            String title = foldSerbian(p.getNaziv());
            String desc = p.getKratakOpis() == null ? "" : foldSerbian(p.getKratakOpis());
            int score = 0;
            int titleMatches = 0;
            for (String n : needles) {
                if (title.contains(n)) {
                    int w = Math.min(n.length(), 10);
                    score += 4 + w;
                    titleMatches++;
                } else if (desc.contains(n)) {
                    score += 2 + Math.min(n.length(), 8);
                }
            }
            if (score > 0) {
                hits.add(new Hit(p, score, titleMatches));
            }
        }
        if (hits.isEmpty()) {
            return List.of();
        }
        hits.sort(Comparator.<Hit>comparingInt(Hit::score).reversed()
                .thenComparingInt(Hit::titleMatches).reversed());
        int top = hits.get(0).score;
        int floor = Math.max(6, top - 4);
        List<VectorSearchClient.VectorMatch> out = new ArrayList<>();
        for (Hit h : hits) {
            if (h.score < floor) {
                break;
            }
            if (out.size() >= 8) {
                break;
            }
            Predmet p = h.p;
            String ko = p.getKratakOpis() == null ? "" : p.getKratakOpis().trim();
            String text = ko.isBlank() ? p.getNaziv() : p.getNaziv() + " — " + ko;
            double sim = 0.42 + Math.min(h.score, 24) * 0.015;
            out.add(new VectorSearchClient.VectorMatch(
                    p.getId(),
                    p.getSifra(),
                    p.getNaziv(),
                    p.getEspb(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    text,
                    "pg_naziv_opis_fallback",
                    sim
            ));
        }
        return out;
    }

    private static void addLanguageCourseAliasNeedles(String folded, LinkedHashSet<String> out) {
        if (folded.contains("english") || folded.contains("englisch")) {
            out.add("engleski");
        }
        if (folded.contains("german") || folded.contains("deutsch") || folded.contains("nemac")
                || folded.contains("nemač")) {
            out.add("nemack");
        }
        if (folded.contains("french") || folded.contains("francais") || folded.contains("francus")) {
            out.add("francusk");
        }
        if (folded.contains("italian") || folded.contains("italijan")) {
            out.add("italij");
        }
        if (folded.contains("spanish") || folded.contains("espanol") || folded.contains("špan")
                || folded.contains("span")) {
            out.add("spansk");
        }
        if (folded.contains("russian") || folded.contains("ruski") || folded.contains("rusij")) {
            out.add("rusk");
        }
    }

    private Optional<Predmet> findPredmetExplicitlyNamedInQuestion(String question, Long programId) {
        String fq = foldSerbian(question);
        if (fq.length() < 6) {
            return Optional.empty();
        }
        fq = fq.replaceAll("\\s+", " ").trim();
        List<Predmet> all = new ArrayList<>(predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId));
        all.sort(Comparator.comparingInt((Predmet pr) -> pr.getNaziv().length()).reversed());
        for (Predmet pr : all) {
            String tn = foldSerbian(pr.getNaziv()).replaceAll("\\s+", " ").trim();
            if (tn.length() < 6) {
                continue;
            }
            if (fq.contains(tn)) {
                return Optional.of(pr);
            }
        }
        // Dovoljno je da bar dve značajne reči iz naziva budu u pitanju (npr. „engleski“ + „jezik“ za
        // „Engleski jezik za inženjere“) — ranije je zahtevano da SVA značajna slova budu u pitanju,
        // pa je „da li imam engleski jezik“ padalo na reči „inženjere“.
        for (Predmet pr : all) {
            String tn = foldSerbian(pr.getNaziv());
            String[] words = tn.split("\\s+");
            int sig = 0;
            for (String w : words) {
                if (w.length() < 4) {
                    continue;
                }
                if (w.chars().allMatch(Character::isDigit)) {
                    continue;
                }
                if (fq.contains(w)) {
                    sig++;
                }
            }
            if (sig >= 2) {
                return Optional.of(pr);
            }
        }
        return Optional.empty();
    }

    private AssistantResponse buildCurriculumListResponse(Student student, Long programId, String question) {
        List<Predmet> predmeti = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        String programNaziv = student.getStudijskiProgram().getNaziv();
        var sb = new StringBuilder();
        sb.append("Studijski program: „").append(programNaziv).append("”\n");
        sb.append("Ukupno predmeta na kurikulumu: ").append(predmeti.size()).append("\n\n");
        for (Predmet p : predmeti) {
            sb.append("- ")
                    .append(p.getNaziv())
                    .append(" (šifra ")
                    .append(p.getSifra())
                    .append("), ")
                    .append(p.getEspb())
                    .append(" ESPB\n");
        }
        List<String> sources = predmeti.stream()
                .map(p -> String.format(
                        "ID %d · %s (%s) · %d ESPB",
                        p.getId(), p.getNaziv(), p.getSifra(), p.getEspb()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString(), sources, SRC_RELATIONAL);
    }

    private AssistantResponse buildUnpassedSubjectsResponse(Long korisnikId, Long programId, String question) {
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        var grades = academicQueryService.subjectsAndGrades(korisnikId);
        Map<String, Integer> bestBySifra = new HashMap<>();
        for (var sg : grades) {
            bestBySifra.merge(sg.predmetSifra(), sg.ocena(), Math::max);
        }
        Set<String> passedSifre = bestBySifra.entrySet().stream()
                .filter(e -> e.getValue() >= 6)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        List<Predmet> pending = all.stream()
                .filter(p -> !passedSifre.contains(p.getSifra()))
                .toList();
        if (pending.isEmpty()) {
            return new AssistantResponse(
                    "Po podacima u bazi imaš ocenu ≥ 6 za sve predmete sa kurikuluma (nema nepoloženih po ovom pravilu).",
                    List.of("Kurikulum + ocene · PostgreSQL"),
                    SRC_RELATIONAL);
        }
        var sb = new StringBuilder();
        sb.append("Predmeti bez položene ocene ≥ 6 (ili bez upisanih ispita):\n\n");
        for (Predmet p : pending) {
            Integer g = bestBySifra.get(p.getSifra());
            if (g != null) {
                sb.append("- ")
                        .append(p.getNaziv())
                        .append(" (")
                        .append(p.getSifra())
                        .append(") — najbolji pokušaj u evidenciji: ")
                        .append(g)
                        .append('\n');
            } else {
                sb.append("- ")
                        .append(p.getNaziv())
                        .append(" (")
                        .append(p.getSifra())
                        .append(") — nema upisanih ispita\n");
            }
        }
        List<String> sources = pending.stream()
                .map(p -> String.format("ID %d · %s (%s)", p.getId(), p.getNaziv(), p.getSifra()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString().trim(), sources, SRC_RELATIONAL);
    }

    private static boolean wantsCurriculumRemainder(String f) {
        if (f.contains("sta mi je ostalo") || f.contains("šta mi je ostalo") || f.contains("sto mi je ostalo")) {
            return true;
        }
        if ((f.contains("ostalo") || f.contains("preostaje")) && (f.contains(" mi ") || f.contains(" meni"))) {
            return true;
        }
        if (f.contains("sta fali") || f.contains("šta fali") || f.contains("sto fali")) {
            return true;
        }
        return (f.contains("koj") || f.contains("koji") || f.contains("koje"))
                && f.contains("predmet")
                && (f.contains("nisam poloz") || f.contains("nemam polozen") || f.contains("nemam poloz"));
    }

    private static boolean wantsTotalExamAttempts(String f) {
        boolean aboutCount = f.contains("koliko puta")
                || f.contains("puta sam")
                || f.contains("broj izlazaka")
                || f.contains("koliko izlazaka")
                || (f.contains("puta") && (f.contains("izas") || f.contains("isao") || f.contains("išao")));
        if (!aboutCount) {
            return false;
        }
        return f.contains("ispit") || f.contains("izlazak");
    }

    private static boolean wantsPassRokSummary(String f) {
        if (f.contains("nisam poloz") || f.contains("nisam polož") || f.contains("nismo poloz")) {
            return false;
        }
        if (!f.contains("poloz") && !f.contains("polož")) {
            return false;
        }
        return f.contains("rok")
                || f.contains("ispitni rok")
                || f.contains("kad sam")
                || f.contains("kada sam")
                || f.contains("koj ispitni")
                || (f.contains("u kom") && f.contains("rok"));
    }

    private AssistantResponse buildCurriculumRemainderResponse(Long korisnikId, String question) {
        AcademicQueryService.CurriculumProgressDto cp = academicQueryService.curriculumProgress(korisnikId);
        var sb = new StringBuilder();
        sb.append(String.format(
                "Program: „%s“ (%s). Ukupno predmeta na kurikulumu: %d.\n"
                        + "Položeno (ocena ≥ 6): %d. Nepoloženo (aktivno): %d. Bez izlaska u ovoj fazi: %d. Kasnije u planu: %d.\n\n",
                cp.studijskiProgramNaziv(),
                cp.studijskiProgramSifra(),
                cp.ukupnoPredmetaNaProgramu(),
                cp.brojPolozenih(),
                cp.brojNepolozenih(),
                cp.brojBezIzlaska(),
                cp.brojPredmetaKasnije()));
        sb.append("Predmeti bez položene ocene ≥ 6:\n\n");
        int lines = 0;
        final int maxLines = 40;
        for (AcademicQueryService.CurriculumSubjectDto row : cp.predmeti()) {
            if ("POLOZENO".equals(row.status())) {
                continue;
            }
            if (lines >= maxLines) {
                sb.append("\n… (skraćeno; ostatak u portfelju)\n");
                break;
            }
            sb.append("- ")
                    .append(row.naziv())
                    .append(" (")
                    .append(row.sifra())
                    .append(", ")
                    .append(row.espb())
                    .append(" ESPB) — status: ")
                    .append(humanCurriculumStatus(row))
                    .append('\n');
            lines++;
        }
        if (lines == 0) {
            sb.append("(Nema nepoloženih po ovom pregledu.)\n");
        }
        return new AssistantResponse(
                sb.toString().trim(),
                List.of("Kurikulum i napredovanje · PostgreSQL"),
                SRC_RELATIONAL);
    }

    private static String humanCurriculumStatus(AcademicQueryService.CurriculumSubjectDto row) {
        return switch (row.status()) {
            case "PALI" -> {
                Integer n = row.najboljaOcena();
                yield n != null ? "najbolji pokušaj zasad: " + n : "nemaš još položenu ocenu";
            }
            case "BEZ_IZLAZAKA" -> "nemaš još izlazak na ispit u ovoj fazi";
            case "KASNIJE" -> "u kasnijoj godini kurikuluma (prema pravilu napredovanja)";
            default -> row.status();
        };
    }

    private AssistantResponse buildExamAttemptsSummaryResponse(Long korisnikId, String question) {
        AcademicQueryService.GpaDto g = academicQueryService.gpa(korisnikId);
        List<AcademicQueryService.SubjectGradeDto> list = academicQueryService.subjectsAndGrades(korisnikId);
        var sb = new StringBuilder();
        sb.append(String.format(
                "Ukupan broj evidentiranih izlazaka na ispite (svi predmeti): %d.\n",
                g.ukupnoIspita()));
        if (list.isEmpty()) {
            sb.append("Nema pojedinačnih unosa ocena u sistemu po predmetima.\n");
            return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"), SRC_RELATIONAL);
        }
        Map<String, Long> countBySifra = list.stream()
                .collect(Collectors.groupingBy(AcademicQueryService.SubjectGradeDto::predmetSifra, Collectors.counting()));
        sb.append("Broj izlazaka po predmetu (broj upisanih termina u evidenciji):\n\n");
        countBySifra.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(25)
                .forEach(e -> {
                    String naziv = list.stream()
                            .filter(x -> x.predmetSifra().equals(e.getKey()))
                            .map(AcademicQueryService.SubjectGradeDto::predmetNaziv)
                            .findFirst()
                            .orElse(e.getKey());
                    sb.append("- ")
                            .append(naziv)
                            .append(" (")
                            .append(e.getKey())
                            .append("): ")
                            .append(e.getValue())
                            .append(" izlazaka\n");
                });
        if (countBySifra.size() > 25) {
            sb.append("\n… (samo prvih 25 po broju izlazaka)");
        }
        return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"), SRC_RELATIONAL);
    }

    private AssistantResponse buildPassRokSummaryResponse(Long korisnikId, String question, Long programId) {
        List<AcademicQueryService.SubjectGradeDto> list = academicQueryService.subjectsAndGrades(korisnikId);
        List<AcademicQueryService.SubjectGradeDto> passed = list.stream()
                .filter(sg -> sg.ocena() >= 6)
                .toList();
        if (passed.isEmpty()) {
            return new AssistantResponse(
                    "U evidenciji nema položenih predmeta (ocena ≥ 6) — nema podatka o roku prvog položenog ispita.",
                    List.of("Ocene · PostgreSQL"),
                    SRC_RELATIONAL);
        }
        Optional<String> sifraFilter = findPredmetSifraHintInQuestion(question, programId);
        Map<String, List<AcademicQueryService.SubjectGradeDto>> bySifra = passed.stream()
                .filter(sg -> sifraFilter.isEmpty() || sifraFilter.get().equalsIgnoreCase(sg.predmetSifra()))
                .collect(Collectors.groupingBy(AcademicQueryService.SubjectGradeDto::predmetSifra));
        if (bySifra.isEmpty()) {
            return new AssistantResponse(
                    "Nema položenog ispita u evidenciji za predmet koji odgovara filtru iz pitanja.",
                    List.of("Ocene · PostgreSQL"),
                    SRC_RELATIONAL);
        }
        var sb = new StringBuilder();
        sb.append("Prvi položeni pokušaj (ocena ≥ 6) po predmetu:\n\n");
        for (var e : bySifra.entrySet()) {
            List<AcademicQueryService.SubjectGradeDto> rows = e.getValue().stream()
                    .sorted(Comparator.comparing(AcademicQueryService.SubjectGradeDto::datumIspita))
                    .toList();
            AcademicQueryService.SubjectGradeDto firstPass = rows.get(0);
            sb.append("- ")
                    .append(firstPass.predmetNaziv())
                    .append(" (")
                    .append(firstPass.predmetSifra())
                    .append(") — rok: ")
                    .append(firstPass.ispitniRok())
                    .append(", datum ")
                    .append(shortDate(firstPass.datumIspita()))
                    .append(", ocena ")
                    .append(firstPass.ocena())
                    .append('\n');
        }
        return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"), SRC_RELATIONAL);
    }

    private Optional<String> findPredmetSifraHintInQuestion(String question, Long programId) {
        String fq = foldSerbian(question);
        if (fq.length() < 4) {
            return Optional.empty();
        }
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        all.sort(Comparator.comparingInt((Predmet pr) -> pr.getNaziv().length()).reversed());
        for (Predmet p : all) {
            String tn = foldSerbian(p.getNaziv());
            if (tn.length() >= 8 && fq.contains(tn)) {
                return Optional.of(p.getSifra());
            }
        }
        return Optional.empty();
    }

    private AssistantResponse trySubjectSyllabusDetailResponse(
            String question,
            String f,
            Long programId,
            List<Long> allowed,
            Long korisnikId
    ) {
        boolean phraseIntent = wantsFullSubjectSyllabus(f);
        SubjectFieldFocus fieldFocus = detectSubjectFieldFocus(f);
        // Fokusirana pitanja tipa "daj ishod", "daj plan/sadržaj", "kako se polaže"
        // moraju uvek ići kroz tok za jedan predmet (uz kontekst ako nema eksplicitnog naziva).
        if (fieldFocus != SubjectFieldFocus.FULL) {
            phraseIntent = true;
        }
        Optional<Predmet> named = findPredmetExplicitlyNamedInQuestion(question, programId);
        boolean bareTitle = !phraseIntent && named.isPresent() && looksLikeBareCourseTitleQuery(f);
        if (!phraseIntent && !bareTitle) {
            return null;
        }
        Optional<Predmet> pred = named;
        boolean pronounFollowup = phraseIntent && isSubjectPronounFollowup(f);
        if (pred.isEmpty() && pronounFollowup) {
            pred = resolveSubjectFromConversationContext(korisnikId, programId);
        }
        // Za fokusirana pitanja ("daj mi ishod/teme/cilj/metode") koristi poslednji predmet iz konteksta,
        // i kada korisnik ne napiše eksplicitno "tog predmeta".
        if (pred.isEmpty() && phraseIntent && fieldFocus != SubjectFieldFocus.FULL) {
            pred = resolveSubjectFromConversationContext(korisnikId, programId);
        }
        if (pred.isEmpty() && pronounFollowup) {
            return new AssistantResponse(
                    "Nije jasno na koji prethodno pomenut predmet misliš. Napiši naziv predmeta (ili šifru) i daću tačan sadržaj.",
                    List.of(),
                    SRC_RELATIONAL);
        }
        if (pred.isEmpty() && phraseIntent) {
            pred = resolvePredmetForSyllabusViaLexical(question, programId, allowed);
        }
        if (pred.isEmpty()) {
            return new AssistantResponse(
                    "Predmet iz pitanja nije pronađen u kurikulumu studijskog programa studenta.",
                    List.of(),
                    SRC_RELATIONAL);
        }
        Predmet p = pred.get();
        Long predmetId = p.getId();
        if (predmetId == null) {
            return new AssistantResponse(
                    "Predmet iz pitanja nema validan identifikator u bazi.",
                    List.of(),
                    SRC_RELATIONAL);
        }
        if (korisnikId != null) {
            lastReferencedPredmetByKorisnikId.put(korisnikId, predmetId);
        }
        if (!p.getStudijskiProgram().getId().equals(programId)) {
            return new AssistantResponse(
                    "Predmet iz pitanja nije pronađen u kurikulumu studijskog programa studenta.",
                    List.of(),
                    SRC_RELATIONAL);
        }
        SadrzajPredmeta sqlRow = sadrzajPredmetaRepository.findById(predmetId).orElse(null);
        List<VectorSearchClient.VectorMatch> chunks = List.of();
        boolean shouldUseQdrant = syllabusNeedsQdrantChunks(sqlRow)
                || (fieldFocus != SubjectFieldFocus.FULL && naisProperties.getAssistant().isQdrantEnabled());
        if (shouldUseQdrant) {
            chunks = vectorSearchClient.search(p.getNaziv(), 32, List.of(predmetId));
            if (chunks.isEmpty()) {
                chunks = vectorSearchClient.search(question, 32, List.of(predmetId));
            }
        }
        if (fieldFocus != SubjectFieldFocus.FULL) {
            AssistantResponse focused = buildFocusedSubjectFieldResponse(question, p, sqlRow, chunks, fieldFocus);
            if (focused != null) {
                return focused;
            }
        }
        String evidence = formatSubjectSyllabusFromSources(p, chunks, sqlRow);
        List<String> sources = buildSyllabusSourceLines(p, chunks);
        String answerSrc = chunks.isEmpty() ? SRC_SYLLABUS_PG : SRC_SYLLABUS;
        return new AssistantResponse(evidence, sources, answerSrc);
    }

    private AssistantResponse buildFocusedSubjectFieldResponse(
            String question,
            Predmet predmet,
            SadrzajPredmeta sqlRow,
            List<VectorSearchClient.VectorMatch> chunks,
            SubjectFieldFocus focus
    ) {
        String fieldLabel = switch (focus) {
            case ISHODI -> "Ishodi učenja";
            case CILJ -> "Cilj predmeta";
            case TEME -> "Sadržaj / teme kursa";
            case METODE -> "Metode nastave";
            case FULL -> "";
        };
        String vectorOnly = switch (focus) {
            case ISHODI -> mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::ishodiUcenja).trim();
            case CILJ -> mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::cilj).trim();
            case TEME -> mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::temeKursa).trim();
            case METODE -> mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::metodeNastave).trim();
            case FULL -> "";
        };
        String sqlOnly = switch (focus) {
            case ISHODI -> sqlRow == null ? "" : safeTrim(sqlRow.getIshodiUcenja());
            case CILJ -> sqlRow == null ? "" : safeTrim(sqlRow.getCilj());
            case TEME -> sqlRow == null ? "" : safeTrim(sqlRow.getTemeKursa());
            case METODE -> sqlRow == null ? "" : safeTrim(sqlRow.getMetodeNastave());
            case FULL -> "";
        };
        // Vector-first za fokusirana pitanja; SQL je samo rezerva kada vektor ništa ne vrati.
        String fieldValue = !vectorOnly.isBlank() ? vectorOnly : sqlOnly;
        boolean genericOnly = isGenericSyllabusText(fieldValue);

        List<String> sources = buildSyllabusSourceLines(predmet, chunks);
        String answerSource = chunks.isEmpty() ? SRC_SYLLABUS_PG : SRC_SYLLABUS;

        // LLM first za fokusirano pitanje (npr. "koji je ishod ..."), sa uskim kontekstom predmeta.
        // Važi i kada je sadržaj generički — tada LLM treba da vrati lep, ali iskren odgovor (bez izmišljanja).
        String apiKey = effectiveLlmApiKey();
        if (!apiKey.isBlank()) {
            String vectorFieldContext = buildSubjectFieldVectorContext(predmet, chunks, focus, fieldValue);
            LlmOrFallback gen = callOpenAiForFocusedSubjectField(question, vectorFieldContext, apiKey);
            if (gen.usedLlm()) {
                return new AssistantResponse(gen.text(), sources,
                        "Odgovor generisao LLM (Groq/OpenAI kompatibilan API) · kontekst: Qdrant fragmenti predmeta.");
            }
        }

        if (fieldValue == null || fieldValue.isBlank() || genericOnly) {
            return new AssistantResponse(
                    "Za predmet „" + predmet.getNaziv() + "” nisu uneti konkretni podaci za traženo polje („" + fieldLabel + "”).",
                    sources,
                    answerSource
            );
        }

        String concise = fieldLabel + " za predmet „" + predmet.getNaziv() + "” (" + predmet.getSifra() + "): " + fieldValue.trim();
        return new AssistantResponse(concise, sources, answerSource);
    }

    private static String buildSubjectFieldVectorContext(
            Predmet predmet,
            List<VectorSearchClient.VectorMatch> chunks,
            SubjectFieldFocus focus,
            String sqlFallback
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("PITANJE je fokusirano na jedan element predmeta. Odgovori SAMO na to što je traženo, bez drugih sekcija.\n")
                .append("Predmet: ")
                .append(predmet.getNaziv())
                .append(" (")
                .append(predmet.getSifra())
                .append(")\n")
                .append("Traženo polje: ")
                .append(switch (focus) {
                    case ISHODI -> "Ishodi učenja";
                    case CILJ -> "Cilj predmeta";
                    case TEME -> "Sadržaj / teme kursa";
                    case METODE -> "Metode nastave";
                    case FULL -> "Kompletan sadržaj";
                })
                .append("\n\nQDRANT EVIDENCIJA:\n");
        int n = Math.min(chunks.size(), 8);
        for (int i = 0; i < n; i++) {
            VectorSearchClient.VectorMatch m = chunks.get(i);
            String v = switch (focus) {
                case ISHODI -> m.ishodiUcenja();
                case CILJ -> m.cilj();
                case TEME -> m.temeKursa();
                case METODE -> m.metodeNastave();
                case FULL -> m.text();
            };
            if (v == null || v.isBlank()) {
                continue;
            }
            sb.append("- ").append(v.trim()).append('\n');
        }
        if (sqlFallback != null && !sqlFallback.isBlank()) {
            sb.append("\nSQL rezerva (ako nedostaje u chunkovima): ").append(sqlFallback.trim()).append('\n');
        }
        sb.append("\nINSTRUKCIJA: Vrati 1-3 rečenice, samo za traženo polje. Ne navodi druge sekcije.");
        sb.append("\nNe dodaj procene kvaliteta teksta, komentare tipa 'generički/opšti opis' niti meta-objašnjenja.");
        sb.append("\nAko nema konkretnih podataka, vrati kratko samo činjenicu da konkretni podaci nisu dostupni.");
        return sb.toString();
    }

    private LlmOrFallback callOpenAiForFocusedSubjectField(String question, String context, String apiKey) {
        try {
            String qLine = question == null ? "" : question.replace("\r\n", "\n").trim();
            String userBlock = "PITANJE STUDENTA:\n„"
                    + qLine.replace("\n", " ")
                    + "“\n\n"
                    + "Odgovori ISKLJUCIVO traženim poljem za JEDAN predmet.\n"
                    + "- Nema uvoda tipa \"Da, ...\"\n"
                    + "- Nema liste drugih predmeta\n"
                    + "- Nema sekcija koje nisu tražene (npr. cilj/ishodi/metode ako je tražen sadržaj)\n"
                    + "- 1 do 3 rečenice, prirodno i jasno\n"
                    + "- Ne dodaj evaluacije tipa \"ovo je generički/opšti opis\" i slične meta-komentare.\n"
                    + "- Ako nema konkretnih podataka, reci samo kratko da nisu dostupni.\n\n"
                    + context;

            var body = new LinkedHashMap<String, Object>();
            body.put("model", naisProperties.getLlm().getOpenaiModel());
            body.put(
                    "messages",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", userBlock)
                    ));
            body.put("temperature", naisProperties.getLlm().getOpenaiTemperature());
            body.put("max_tokens", naisProperties.getLlm().getOpenaiMaxTokens());

            String jsonBody = MAPPER.writeValueAsString(body);
            String baseUrl = naisProperties.getLlm().getOpenaiBaseUrl().trim();
            while (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            if (baseUrl.isEmpty()) {
                baseUrl = "https://api.openai.com/v1";
            }
            RestClient client = restClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            String raw = client.post()
                    .uri("/chat/completions")
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (content.isEmpty() || "Nije moguće generisati odgovor.".equals(content)) {
                return new LlmOrFallback(MSG_VECTOR_LLM_EMPTY, false);
            }
            return new LlmOrFallback(content, true);
        } catch (Exception e) {
            return new LlmOrFallback(llmFailureMessageForUser(e), false);
        }
    }

    private enum SubjectFieldFocus {
        FULL,
        ISHODI,
        CILJ,
        TEME,
        METODE
    }

    private static SubjectFieldFocus detectSubjectFieldFocus(String f) {
        if (f == null || f.isBlank()) {
            return SubjectFieldFocus.FULL;
        }
        if (f.contains("ishod")
                || looksLikeApproxWord(f, "ishod", 1)
                || looksLikeApproxWord(f, "ishodi", 1)
                || (f.contains("ish") && (f.contains("ucen") || f.contains("učen")))) {
            return SubjectFieldFocus.ISHODI;
        }
        if (f.contains("cilj") || looksLikeApproxWord(f, "cilj", 1)) {
            return SubjectFieldFocus.CILJ;
        }
        if (f.contains("metod")
                || looksLikeApproxWord(f, "metode", 1)
                || f.contains("kako se polaz")
                || f.contains("nacin polag")) {
            return SubjectFieldFocus.METODE;
        }
        if (f.contains("teme") || f.contains("sadrzaj") || f.contains("plan")
                || f.contains("sta se uci") || f.contains("šta se uči")) {
            return SubjectFieldFocus.TEME;
        }
        return SubjectFieldFocus.FULL;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean looksLikeApproxWord(String foldedQuestion, String target, int maxDistance) {
        String[] toks = foldedQuestion.split("[^a-z0-9]+");
        for (String tok : toks) {
            if (tok.length() < 3) {
                continue;
            }
            if (Math.abs(tok.length() - target.length()) > maxDistance) {
                continue;
            }
            if (levenshteinDistance(tok, target) <= maxDistance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Qdrant fragmenti samo kad je u SQL-u sve generički/prazno — inače je sadržaj predmeta već u bazi.
     */
    private boolean syllabusNeedsQdrantChunks(SadrzajPredmeta sqlRow) {
        if (!naisProperties.getAssistant().isQdrantEnabled()) {
            return false;
        }
        if (sqlRow == null) {
            return true;
        }
        return isGenericSyllabusText(sqlRow.getCilj())
                && isGenericSyllabusText(sqlRow.getTemeKursa())
                && isGenericSyllabusText(sqlRow.getIshodiUcenja())
                && isGenericSyllabusText(sqlRow.getMetodeNastave());
    }

    private Optional<Predmet> resolvePredmetForSyllabusViaLexical(
            String question,
            Long programId,
            List<Long> allowed
    ) {
        List<VectorSearchClient.VectorMatch> hits = dedupeByPredmetKeepBestScore(
                fallbackLexicalCurriculumHits(question, programId, allowed));
        if (hits.isEmpty() || !lexicalStrongConfidence(hits)) {
            return Optional.empty();
        }
        return predmetRepository.findById(hits.get(0).predmetId());
    }

    private Optional<Predmet> resolveSubjectFromConversationContext(Long korisnikId, Long programId) {
        if (korisnikId == null) {
            return Optional.empty();
        }
        Long predmetId = lastReferencedPredmetByKorisnikId.get(korisnikId);
        if (predmetId == null) {
            return Optional.empty();
        }
        Optional<Predmet> p = predmetRepository.findById(predmetId);
        if (p.isEmpty() || !p.get().getStudijskiProgram().getId().equals(programId)) {
            lastReferencedPredmetByKorisnikId.remove(korisnikId);
            return Optional.empty();
        }
        return p;
    }

    private static boolean isSubjectPronounFollowup(String f) {
        return f.contains("tog predmeta")
                || f.contains("taj predmet")
                || f.contains("tom predmetu")
                || f.contains("tog kursa")
                || f.contains("taj kurs")
                || f.contains("njegov sadrzaj")
                || f.contains("njegov sadržaj")
                || f.contains("sadrzaj tog")
                || f.contains("sadržaj tog")
                || f.contains("o tom predmetu");
    }

    private static boolean wantsFullSubjectSyllabus(String f) {
        if (f.contains("koji predmeti") || f.contains("kojim predmetima") || f.contains("svi predmet")) {
            return false;
        }
        if (f.contains("sadrzaj predmet") || f.contains("sadržaj predmet")
                || f.contains("sadrzaj kursa") || f.contains("sadržaj kursa")
                || f.contains("sadrzaj kolegij") || f.contains("sadržaj kolegij")) {
            return true;
        }
        if ((f.contains("sadrzaj") || f.contains("sadržaj"))
                && (f.contains("o predmetu") || f.contains("o predmeta") || f.contains("za predmet")
                || f.contains("predmetu ") || f.contains("predmeta "))) {
            return true;
        }
        if ((f.contains("napisi") || f.contains("napiši")) && (f.contains("sadrzaj") || f.contains("sadržaj"))
                && (f.contains("predmet") || f.contains("kurs"))) {
            return true;
        }
        if ((f.contains("sta obuhvata") || f.contains("šta obuhvata") || f.contains("sto obuhvata"))
                && (f.contains("predmet") || f.contains("kurs"))) {
            return true;
        }
        if ((f.contains("sta se uci") || f.contains("šta se uči") || f.contains("sto se uci"))
                && (f.contains("predmet") || f.contains("kurs") || f.contains("ovom predmet"))) {
            return true;
        }
        if ((f.contains("sta radimo") || f.contains("šta radimo") || f.contains("sto radimo"))
                && (f.contains("predmet") || f.contains("kurs"))) {
            return true;
        }
        if ((f.contains("cilj") || f.contains("ciljev")) && f.contains("predmet")) {
            return true;
        }
        if (f.contains("ishod") && (f.contains("ucenja") || f.contains("učenja") || f.contains("predmet"))) {
            return true;
        }
        if (f.contains("teme") && (f.contains("predmet") || f.contains("kurs"))) {
            return true;
        }
        if ((f.contains("daj") || f.contains("pokazi") || f.contains("pokaži") || f.contains("zelim")
                || f.contains("želim") || f.contains("hocu") || f.contains("hoću"))
                && (f.contains("sadrzaj") || f.contains("sadržaj") || f.contains("pregled"))
                && (f.contains("predmet") || f.contains("kurs"))) {
            return true;
        }
        return f.contains("koji je sadrzaj") || f.contains("koji je sadržaj")
                || f.contains("kompletan opis predmet") || f.contains("informacije o predmetu")
                || (f.contains("o kom predmetu") && f.contains("radi"));
    }

    private static boolean looksLikeBareCourseTitleQuery(String f) {
        if (f.length() > 120) {
            return false;
        }
        if (f.contains("da li ") || f.contains("dal li")) {
            return false;
        }
        if (f.contains("koliko ") || f.contains("zasto") || f.contains("zašto") || f.contains("kako ")) {
            return false;
        }
        if (f.contains("ocen") || f.contains("polož") || f.contains("poloz") || f.contains("ispit")) {
            return false;
        }
        if (f.contains("prosek") || f.contains("proseč") || f.contains("prosec")) {
            return false;
        }
        if (f.contains("indeks") || f.contains("email")) {
            return false;
        }
        if (f.contains("koji predmeti") || f.contains("kojim predmetima")) {
            return false;
        }
        if (f.contains("lista") && f.contains("predmet")) {
            return false;
        }
        if (wantsUnpassedSubjects(f) || wantsCurriculumRemainder(f) || wantsTotalExamAttempts(f)) {
            return false;
        }
        String[] toks = f.split("[^a-zđščćž0-9]+");
        int n = 0;
        for (String t : toks) {
            if (!t.isEmpty()) {
                n++;
            }
        }
        return n <= 14;
    }

    private static String formatSubjectSyllabusFromSources(
            Predmet p,
            List<VectorSearchClient.VectorMatch> chunks,
            SadrzajPredmeta sqlRow
    ) {
        var sb = new StringBuilder();
        sb.append("Predmet „")
                .append(p.getNaziv())
                .append("” (šifra ")
                .append(p.getSifra())
                .append("), ")
                .append(p.getEspb())
                .append(" ESPB.\n\n");
        appendIfPresent(sb, "Opis predmeta: ", p.getKratakOpis());
        String cilj = pickRicherSyllabusLine(sqlRow == null ? null : sqlRow.getCilj(), chunks, VectorSearchClient.VectorMatch::cilj);
        String teme = pickRicherSyllabusLine(sqlRow == null ? null : sqlRow.getTemeKursa(), chunks, VectorSearchClient.VectorMatch::temeKursa);
        String ishodi = pickRicherSyllabusLine(sqlRow == null ? null : sqlRow.getIshodiUcenja(), chunks, VectorSearchClient.VectorMatch::ishodiUcenja);
        String metode = pickRicherSyllabusLine(sqlRow == null ? null : sqlRow.getMetodeNastave(), chunks, VectorSearchClient.VectorMatch::metodeNastave);
        if (isGenericSyllabusText(cilj) && isGenericSyllabusText(teme)
                && isGenericSyllabusText(ishodi) && isGenericSyllabusText(metode)) {
            SubjectFallbackNarrative fallback = synthesizeSubjectNarrativeFromTitle(p.getNaziv());
            if (fallback != null) {
                cilj = fallback.cilj();
                teme = fallback.teme();
                ishodi = fallback.ishodi();
                metode = fallback.metode();
            }
        }
        appendIfPresent(sb, "Cilj: ", cilj);
        appendIfPresent(sb, "Sadržaj / teme kursa: ", teme);
        appendIfPresent(sb, "Ishodi učenja: ", ishodi);
        appendIfPresent(sb, "Metode nastave: ", metode);
        String extra = mergeDistinctSyllabusTextFragments(chunks);
        appendIfPresent(sb, "Dodatni tekst za pretragu / indeks: ", extra);
        return sb.toString().trim();
    }

    private record SubjectFallbackNarrative(String cilj, String teme, String ishodi, String metode) {
    }

    private static SubjectFallbackNarrative synthesizeSubjectNarrativeFromTitle(String nazivPredmeta) {
        String t = foldSerbian(nazivPredmeta == null ? "" : nazivPredmeta);
        if (t.contains("digitalna obrada signala")) {
            return new SubjectFallbackNarrative(
                    "Uvod u diskretnu obradu signala i razumevanje osnovnih matematičkih alata za analizu signala i sistema.",
                    "Diskretni signali i sistemi; uzorkovanje i kvantizacija; diskretna konvolucija; Z-transformacija; DFT/FFT; osnove digitalnog filtriranja.",
                    "Student ume da analizira diskretne signale u vremenskom i frekvencijskom domenu, kao i da primeni osnovne transformacije i algoritme u jednostavnim zadacima.",
                    "Predavanja i računarske vežbe sa zadacima iz analize signala i osnova digitalnih filtera, uz proveru znanja kroz kolokvijume i završni ispit."
            );
        }
        return null;
    }

    /**
     * Biramo duži / konkretniji tekst: službena tabela sadrzaj_predmeta + Qdrant; šablon iz SQL-a
     * (npr. „u skladu sa NPP“) zamenjuje se bogatijim fragmentom iz vektora ako postoji.
     */
    private static String pickRicherSyllabusLine(
            String sqlLine,
            List<VectorSearchClient.VectorMatch> chunks,
            Function<VectorSearchClient.VectorMatch, String> field
    ) {
        String mergedVectors = mergeUniqueNonBlankChunks(chunks, field).trim();
        String sql = sqlLine == null ? "" : sqlLine.trim();
        if (sql.isEmpty()) {
            return mergedVectors;
        }
        if (mergedVectors.isEmpty()) {
            return sql;
        }
        boolean sqlGen = isGenericSyllabusText(sql);
        boolean vecGen = isGenericSyllabusText(mergedVectors);
        if (sqlGen && !vecGen) {
            return mergedVectors;
        }
        if (!sqlGen && vecGen) {
            return sql;
        }
        if (!sqlGen && sql.length() >= mergedVectors.length()) {
            return sql;
        }
        if (!vecGen) {
            return mergedVectors;
        }
        return sql.length() >= mergedVectors.length() ? sql : mergedVectors;
    }

    private static boolean isGenericSyllabusText(String t) {
        if (t == null || t.isBlank()) {
            return true;
        }
        String n = foldSerbian(t).replaceAll("\\s+", "");
        return n.contains("uskladusanastavnimplanomiprogramom")
                || n.contains("ishodiponastavnomplanu")
                || n.contains("ciljmodulaostvarivanjedefinisanihishodastudijskogprograma")
                || (n.contains("predavanja") && n.contains("vezbe") && n.contains("ispit") && n.length() < 80);
    }

    private AssistantResponse tryStatisticsRankingsResponse(
            Student student,
            Long programId,
            String fq,
            String questionRaw
    ) {
        boolean wantHardest = fq.contains("najtez")
                || fq.contains("najgori")
                || fq.contains("najgor")
                || (fq.contains("najniz") && (fq.contains("prolaz") || fq.contains("stopa")))
                || (fq.contains("tezak") && fq.contains("predmet") && fq.contains("koj"));
        boolean wantEasiest = fq.contains("najlak")
                || (fq.contains("najvis") && (fq.contains("prolaz") || fq.contains("stopa")));
        if (!wantHardest && !wantEasiest && fq.contains("rang") && (fq.contains("prolaz") || fq.contains("polaganje"))) {
            wantHardest = true;
            wantEasiest = true;
        }
        if (!wantHardest && !wantEasiest) {
            return null;
        }

        Integer kurikulumGodina = parseKurikulumGodinaFromQuestion(foldSerbian(questionRaw));
        StatisticsQueryParams params = new StatisticsQueryParams(
                student.getGodinaUpisa(),
                null,
                kurikulumGodina,
                null,
                null,
                false);
        ProgramStatisticsResponse full = programSubjectAnalyticsService.computeForProgram(programId, params);
        RankingsBundle r = full.rankings();
        String context = buildStatisticsRankingContext(full, student.getGodinaUpisa(), kurikulumGodina, wantHardest, wantEasiest);
        String apiKey = effectiveLlmApiKey();
        if (!apiKey.isBlank()) {
            LlmOrFallback gen = callOpenAi(questionRaw, context, apiKey);
            if (gen.usedLlm()) {
                return new AssistantResponse(
                        gen.text(),
                        List.of("Statistika programa · PostgreSQL"),
                        "Odgovor generisao LLM (Groq/OpenAI kompatibilan API) · kontekst: statistika programa iz baze."
                );
            }
        }

        var sb = new StringBuilder();
        if (wantHardest) {
            sb.append("Najteži predmeti (najniža prolaznost):\n");
            appendRankingBlock(sb, r.hardestByPassRate(), 6);
            sb.append('\n');
        }
        if (wantEasiest) {
            sb.append("Najlakši predmeti (najviša prolaznost):\n");
            appendRankingBlock(sb, r.easiestByPassRate(), 6);
        }
        String body = sb.toString().trim();
        if (body.length() > 12000) {
            body = body.substring(0, 11900) + "\n… (skraćeno)";
        }
        return new AssistantResponse(
                body,
                List.of("Statistika programa · PostgreSQL"),
                SRC_PROGRAM_STATS);
    }

    private AssistantResponse trySubjectPassRateResponse(
            Student student,
            Long programId,
            String fq,
            String question,
            List<Long> allowed
    ) {
        boolean passRateIntent = fq.contains("prolaznost")
                || fq.contains("verovatnoc") || fq.contains("vjerovatnoc")
                || ((fq.contains("prolaz") || fq.contains("proci") || fq.contains("prođe"))
                && (fq.contains("kolika") || fq.contains("koliko") || fq.contains("kolka")));
        if (!passRateIntent) {
            return null;
        }

        Optional<Predmet> pred = findPredmetExplicitlyNamedInQuestion(question, programId);
        if (pred.isEmpty()) {
            pred = resolvePredmetForSyllabusViaLexical(question, programId, allowed);
        }
        if (pred.isEmpty()) {
            return new AssistantResponse(
                    "Nisam uspeo da prepoznam za koji predmet želiš prolaznost. Napiši pun naziv ili šifru predmeta.",
                    List.of(),
                    SRC_PROGRAM_STATS
            );
        }
        Predmet p = pred.get();
        if (p.getId() == null) {
            return new AssistantResponse("Predmet nema validan identifikator u bazi.", List.of(), SRC_PROGRAM_STATS);
        }

        StatisticsQueryParams params = new StatisticsQueryParams(
                student.getGodinaUpisa(),
                null,
                null,
                null,
                p.getId(),
                false
        );
        SubjectStatisticsRow row = programSubjectAnalyticsService.subjectDetail(programId, p.getId(), params);
        if (row == null || row.totalStudentsWhoTook() <= 0 || row.passRate() == null) {
            return new AssistantResponse(
                    "Za predmet „" + p.getNaziv() + "” trenutno nema dovoljno statističkih podataka o prolaznosti u izabranom uzorku.",
                    List.of("Statistika programa · PostgreSQL"),
                    SRC_PROGRAM_STATS
            );
        }

        String answer = "Prolaznost za predmet „" + row.subjectName() + "” (" + row.subjectCode() + ") je "
                + formatPercent(row.passRate()) + "% (polagalo " + row.totalStudentsWhoTook()
                + ", položilo " + row.totalStudentsWhoPassed() + ").";
        return new AssistantResponse(
                answer,
                List.of("Statistika programa · PostgreSQL"),
                SRC_PROGRAM_STATS
        );
    }

    private static String buildStatisticsRankingContext(
            ProgramStatisticsResponse full,
            Integer godinaUpisa,
            Integer kurikulumGodina,
            boolean wantHardest,
            boolean wantEasiest
    ) {
        var sb = new StringBuilder();
        sb.append("STATISTIKA PROGRAMA\n")
                .append("Program: ").append(full.program().naziv())
                .append(" (").append(full.program().sifra()).append(")\n");
        if (kurikulumGodina != null) {
            sb.append("Filter: kurikulum godina = ").append(kurikulumGodina).append('\n');
        } else {
            sb.append("Filter: generacija upisa = ").append(godinaUpisa).append('\n');
        }
        sb.append("Metodologija: ").append(full.aggregationNote()).append("\n\n");

        if (wantHardest) {
            sb.append("HARD TEST (najniža prolaznost):\n");
            appendRankingRowsForContext(sb, full.rankings().hardestByPassRate(), 8);
            sb.append('\n');
        }
        if (wantEasiest) {
            sb.append("EASY TEST (najviša prolaznost):\n");
            appendRankingRowsForContext(sb, full.rankings().easiestByPassRate(), 8);
        }
        sb.append("\nINSTRUKCIJA: Odgovori prirodno kao osoba, kratko i jasno. Ne izmišljaj podatke.");
        return sb.toString().trim();
    }

    private static void appendRankingRowsForContext(StringBuilder sb, List<SubjectStatisticsRow> rows, int max) {
        if (rows == null || rows.isEmpty()) {
            sb.append("- Nema dovoljno podataka.\n");
            return;
        }
        int n = Math.min(max, rows.size());
        for (int i = 0; i < n; i++) {
            SubjectStatisticsRow s = rows.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(s.subjectName())
                    .append(" (").append(s.subjectCode()).append(")")
                    .append(" | passRate=")
                    .append(s.passRate() == null ? "NA" : formatPercent(s.passRate()))
                    .append("% | took=").append(s.totalStudentsWhoTook())
                    .append(" | passed=").append(s.totalStudentsWhoPassed())
                    .append(" | year=").append(s.kurikulumGodina())
                    .append(" | sem=").append(s.semestar())
                    .append('\n');
        }
    }

    private static void appendRankingBlock(StringBuilder sb, List<SubjectStatisticsRow> rows, int max) {
        if (rows == null || rows.isEmpty()) {
            sb.append("— Nema dovoljno podataka u uzorku (nema predmeta sa izlascima ili stopom).\n");
            return;
        }
        int n = Math.min(max, rows.size());
        for (int i = 0; i < n; i++) {
            SubjectStatisticsRow s = rows.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(s.subjectName())
                    .append(" (")
                    .append(s.subjectCode())
                    .append("): ");
            if (s.passRate() != null) {
                sb.append("prolaznost je ").append(formatPercent(s.passRate())).append("%");
            } else {
                sb.append("prolaznost nije dostupna");
            }
            sb.append(", polagalo je ")
                    .append(s.totalStudentsWhoTook())
                    .append(", položilo ")
                    .append(s.totalStudentsWhoPassed())
                    .append(", a padalo ")
                    .append(Math.max(0, s.totalStudentsWhoTook() - s.totalStudentsWhoPassed()))
                    .append(". (")
                    .append(s.kurikulumGodina())
                    .append(". godina, semestar ")
                    .append(s.semestar())
                    .append(")")
                    .append('\n');
        }
    }

    private static String formatPercent(Double val) {
        return String.format(Locale.ROOT, "%.2f", val);
    }

    /** Prepoznaje „prva godina“, „druga godina“, „kurikulum godina 2“, … */
    private static Integer parseKurikulumGodinaFromQuestion(String f) {
        if (f.contains("prv") && f.contains("godin")) {
            return 1;
        }
        if (f.contains("drug") && f.contains("godin")) {
            return 2;
        }
        if (f.contains("trec") && f.contains("godin")) {
            return 3;
        }
        if (f.contains("cetvrt") && f.contains("godin")) {
            return 4;
        }
        Matcher m1 = Pattern.compile("kurikul(?:um)?\\s*godin[a-z]*\\s*(\\d)").matcher(f);
        if (m1.find()) {
            int v = Integer.parseInt(m1.group(1));
            if (v >= 1 && v <= 4) {
                return v;
            }
        }
        Matcher m2 = Pattern.compile("(?:^|\\D)([1234])\\s*\\.?\\s*godin").matcher(f);
        if (m2.find()) {
            return Integer.parseInt(m2.group(1));
        }
        return null;
    }

    private static String mergeUniqueNonBlankChunks(
            List<VectorSearchClient.VectorMatch> rows,
            Function<VectorSearchClient.VectorMatch, String> field
    ) {
        LinkedHashSet<String> seenNorm = new LinkedHashSet<>();
        List<String> ordered = new ArrayList<>();
        for (VectorSearchClient.VectorMatch m : rows) {
            String v = field.apply(m);
            if (v == null || v.isBlank()) {
                continue;
            }
            String t = v.trim();
            String norm = t.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            if (seenNorm.add(norm)) {
                ordered.add(t);
            }
        }
        return String.join("\n\n", ordered);
    }

    private static String mergeDistinctSyllabusTextFragments(List<VectorSearchClient.VectorMatch> chunks) {
        LinkedHashSet<String> seenNorm = new LinkedHashSet<>();
        var sb = new StringBuilder();
        for (VectorSearchClient.VectorMatch m : chunks) {
            if (m.text() == null || m.text().isBlank()) {
                continue;
            }
            if (textRedundantWithFields(m)) {
                continue;
            }
            String t = m.text().trim();
            String norm = t.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            if (norm.length() < 24 || !seenNorm.add(norm)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(t.length() > 1400 ? t.substring(0, 1400).trim() + "…" : t);
        }
        return sb.toString();
    }

    private static List<String> buildSyllabusSourceLines(Predmet p, List<VectorSearchClient.VectorMatch> chunks) {
        List<String> out = new ArrayList<>();
        out.add(String.format(
                "PostgreSQL · ID %d · %s (%s) · %d ESPB",
                p.getId(), p.getNaziv(), p.getSifra(), p.getEspb()));
        int i = 0;
        for (VectorSearchClient.VectorMatch m : chunks) {
            if (i++ >= 8) {
                break;
            }
            String tip = m.fragmentType() == null || m.fragmentType().isBlank() ? "fragment" : m.fragmentType();
            out.add(String.format("Qdrant · %s · %.2f", tip, m.score()));
        }
        return out;
    }

    private static boolean wantsUnpassedSubjects(String f) {
        if (!f.contains("poloz") && !f.contains("polož") && !f.contains("polozen") && !f.contains("položen")) {
            return false;
        }
        if (f.contains("nisam") || f.contains("nismo") || f.contains("niste")) {
            return f.contains("predmet") || f.contains("kurs") || f.contains("sta") || f.contains("koje")
                    || f.contains("koji") || f.contains("šta") || f.contains("sta");
        }
        if (f.contains("nepolozen") || f.contains("ne polozen") || f.contains("nepoložen")) {
            return true;
        }
        return f.contains("koje nisam") || f.contains("sta nisam");
    }

    private AssistantResponse buildStudentDataResponse(
            Long korisnikId,
            boolean includeProfile,
            boolean includeGrades,
            boolean includeGpa,
            String question
    ) {
        var sb = new StringBuilder();
        List<String> sources = new ArrayList<>();
        if (includeProfile) {
            var d = academicQueryService.studentProfile(korisnikId);
            sb.append(String.format(
                    "Zoveš se %s %s, indeks %s. Na programu si %s (%s), katedra %s, godina upisa %d. Email: %s.\n\n",
                    d.ime(),
                    d.prezime(),
                    d.brojIndeksa(),
                    d.studijskiProgramNaziv(),
                    d.studijskiProgramSifra(),
                    d.katedraNaziv(),
                    d.godinaUpisa(),
                    d.email()));
            sources.add("Profil · PostgreSQL");
        }
        if (includeGpa) {
            var g = academicQueryService.gpa(korisnikId);
            if (g.prosekNaEspb() != null) {
                sb.append(String.format(
                        "Prosek na položenim predmetima (ocena ≥ 6): %.2f (ESPB položenih: %d, "
                                + "upisano ukupno %d ispitnih ocena, položeno predmeta: %d od %d na programu).\n\n",
                        g.prosekNaEspb(),
                        g.zbirEspbPolozenih(),
                        g.ukupnoIspita(),
                        g.brojPolozenihPredmeta(),
                        g.ukupnoPredmetaNaProgramu()));
            } else {
                sb.append(String.format(
                        "Još nemaš predmeta sa ocenom ≥ 6 za prosek. Na programu: %d predmeta; "
                                + "upisano imaš %d ispitnih ocena.\n\n",
                        g.ukupnoPredmetaNaProgramu(),
                        g.ukupnoIspita()));
            }
            sources.add("Prosek · PostgreSQL");
        }
        if (includeGrades) {
            var list = academicQueryService.subjectsAndGrades(korisnikId);
            if (list.isEmpty()) {
                sb.append("U sistemu još nemaš upisanih ocena.");
            } else {
                sb.append("Tvoje ocene:\n\n");
                for (var sg : list) {
                    String datum = shortDate(sg.datumIspita());
                    sb.append(String.format(
                            "• %s (%s) — ocena %d · %d ESPB · %s · %s\n",
                            sg.predmetNaziv(),
                            sg.predmetSifra(),
                            sg.ocena(),
                            sg.espb(),
                            sg.ispitniRok(),
                            datum));
                }
            }
            sources.add("Ocene · PostgreSQL");
        }
        return new AssistantResponse(sb.toString().trim(), sources, SRC_RELATIONAL);
    }

    private static String shortDate(String iso) {
        if (iso == null || iso.length() < 10) {
            return iso == null ? "" : iso;
        }
        return iso.substring(0, 10);
    }

    private static boolean asksEverythingAboutStudent(String f) {
        return f.contains("sve o meni")
                || f.contains("sve sto znas o meni")
                || f.contains("sve što znaš o meni")
                || f.contains("kompletan profil")
                || f.contains("ceo izvestaj")
                || f.contains("ceo izveštaj")
                || f.contains("sta znas o meni")
                || f.contains("šta znaš o meni");
    }

    private static boolean wantsProfileSection(String question, String f) {
        if (asksForFullCurriculumList(question)) {
            return false;
        }
        if (wantsGradesSection(f) && !f.contains("profil") && !f.contains("indeks") && !f.contains("program")) {
            return false;
        }
        if (wantsGpaSection(f) && !f.contains("profil") && !f.contains("indeks") && !f.contains("program")) {
            return false;
        }
        if (f.contains("moj profil") || f.contains("moji podaci")) {
            return true;
        }
        if (f.contains("podaci o meni") || wordBound(f, "o meni") || f.contains("o sebi")) {
            return true;
        }
        if (f.contains("ko sam ja") || wordBound(f, "ko sam")) {
            return true;
        }
        if (f.contains("broj indeks") || (wordBound(f, "indeks") && (f.contains("koji") || f.contains("moj")))) {
            return true;
        }
        if (f.contains("na kom programu")
                || f.contains("koji program studiram")
                || f.contains("koj studijski program")) {
            return true;
        }
        return (f.contains("moj email") || (f.contains("email") && f.contains("moj")));
    }

    private static boolean wantsGradesSection(String f) {
        if (f.contains("ocene") || f.contains("ocena")) {
            return true;
        }
        if (f.contains("ocen")
                && (f.contains("moj")
                || f.contains("imam")
                || f.contains("koje")
                || f.contains("lista")
                || wordBound(f, "sve"))) {
            return true;
        }
        return (f.contains("ispit") || f.contains("ispite"))
                && (f.contains("poloz") || f.contains("polož") || f.contains("rezultat"));
    }

    private static boolean wantsGpaSection(String f) {
        return f.contains("prosek") || f.contains("gpa") || f.contains("ponderisan")
                || f.contains("ponderi") || (f.contains("espb") && f.contains("prosek"));
    }

    private static boolean asksForFullCurriculumList(String question) {
        String f = foldSerbian(question);
        boolean aboutSubjects = f.contains("predmet") || f.contains("kurse") || f.contains("kurs");
        if (!aboutSubjects) {
            return false;
        }
        // Upiti tipa "koji predmet spominje X" nisu zahtev za punu listu kurikuluma.
        if (f.contains("spominj")
                || f.contains("pominj")
                || f.contains("sadrz")
                || f.contains("obuhvat")
                || f.contains("bavi")
                || f.contains("gde se")
                || f.contains("gdje se")) {
            return false;
        }
        if (f.contains("spisak") || f.contains("lista") || f.contains("nabro") || f.contains("navedi")) {
            return true;
        }
        if (f.contains("svi predmet") || f.contains("sve predmet") || f.contains("svi kurse") || f.contains("sve kurse")) {
            return true;
        }
        if (wordBound(f, "imam") || wordBound(f, "imate") || wordBound(f, "imamo")) {
            return f.contains("koje predmete")
                    || f.contains("koje sve predmete")
                    || f.contains("sve predmete")
                    || f.contains("koliko predmeta")
                    || f.contains("sta sve")
                    || wordBound(f, "spisak")
                    || wordBound(f, "lista");
        }
        return wordBound(f, "sve") && (f.contains("koje") || f.contains("koja"));
    }

    private static boolean wordBound(String hay, String needle) {
        if (needle.isEmpty() || hay.length() < needle.length()) {
            return false;
        }
        int i = hay.indexOf(needle);
        while (i >= 0) {
            boolean leftOk = i == 0 || !Character.isLetterOrDigit(hay.charAt(i - 1));
            int after = i + needle.length();
            boolean rightOk = after >= hay.length() || !Character.isLetterOrDigit(hay.charAt(after));
            if (leftOk && rightOk) {
                return true;
            }
            i = hay.indexOf(needle, i + 1);
        }
        return false;
    }

    private static List<VectorSearchClient.VectorMatch> orderMatchesForLlm(
            String question,
            List<VectorSearchClient.VectorMatch> matches,
            int max
    ) {
        if (matches.isEmpty()) {
            return matches;
        }
        VectorSearchClient.VectorMatch primary = resolvePrimaryForNarrative(question, matches);
        List<VectorSearchClient.VectorMatch> rest = matches.stream()
                .filter(m -> m.predmetId() != primary.predmetId())
                .toList();
        List<VectorSearchClient.VectorMatch> out = new ArrayList<>(max);
        out.add(primary);
        for (VectorSearchClient.VectorMatch m : rest) {
            if (out.size() >= max) {
                break;
            }
            out.add(m);
        }
        return out;
    }

    private static List<VectorSearchClient.VectorMatch> orderMatchesForLlm(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        return orderMatchesForLlm(question, matches, 6);
    }

    /** Kratak odgovor iz top Qdrant pogodaka kada LLM nije dostupan. */
    private static String vectorFallbackAnswerFromMatches(List<VectorSearchClient.VectorMatch> ordered) {
        if (ordered.isEmpty()) {
            return "Nema dovoljno podataka za odgovor iz pretrage.";
        }
        VectorSearchClient.VectorMatch top = ordered.get(0);
        var sb = new StringBuilder();
        sb.append("Prema semantičkoj pretrazi sadržaja kurikuluma, kao najbliži tvom pitanju izdvajam predmet „")
                .append(top.predmetNaziv())
                .append("” (šifra ")
                .append(top.predmetSifra())
                .append(", ")
                .append(top.espb())
                .append(" ESPB)");
        String snippet = firstCurriculumSnippet(top);
        if (snippet != null && !snippet.isBlank()) {
            sb.append(":\n\n").append(snippet);
        } else {
            sb.append(".");
        }
        if (ordered.size() > 1) {
            sb.append("\n\nDrugi slični predmeti na programu: ");
            for (int i = 1; i < Math.min(ordered.size(), 4); i++) {
                if (i > 1) {
                    sb.append("; ");
                }
                VectorSearchClient.VectorMatch m = ordered.get(i);
                sb.append("„").append(m.predmetNaziv()).append("” (").append(m.predmetSifra()).append(")");
            }
            sb.append(".");
        }
        return sb.toString();
    }

    private static String firstCurriculumSnippet(VectorSearchClient.VectorMatch m) {
        if (m.cilj() != null && !m.cilj().isBlank()) {
            return truncate(m.cilj().trim(), 520);
        }
        if (m.temeKursa() != null && !m.temeKursa().isBlank()) {
            return truncate(m.temeKursa().trim(), 520);
        }
        if (m.ishodiUcenja() != null && !m.ishodiUcenja().isBlank()) {
            return truncate(m.ishodiUcenja().trim(), 520);
        }
        if (m.metodeNastave() != null && !m.metodeNastave().isBlank()) {
            return truncate(m.metodeNastave().trim(), 520);
        }
        if (m.text() != null && !m.text().isBlank()) {
            return truncate(m.text().trim(), 520);
        }
        return null;
    }

    private static String buildStructuredContext(List<VectorSearchClient.VectorMatch> matches) {
        var sb = new StringBuilder();
        sb.append("EVIDENCIJA (samo predmeti sa studijskog programa studenta). ")
                .append("Koristi ove podatke doslovno za činjenice; ne izmišljaj druge predmete.\n");
        int i = 1;
        for (VectorSearchClient.VectorMatch m : matches) {
            sb.append("\n[")
                    .append(i++)
                    .append("] ")
                    .append(m.predmetNaziv())
                    .append(" — šifra ")
                    .append(m.predmetSifra())
                    .append(", ")
                    .append(m.espb())
                    .append(" ESPB");
            if (!m.profesor().isBlank()) {
                sb.append(", nastavnik: ").append(m.profesor());
            }
            sb.append('\n');
            appendIfPresent(sb, "    Cilj: ", m.cilj());
            appendIfPresent(sb, "    Ishodi učenja: ", m.ishodiUcenja());
            appendIfPresent(sb, "    Metode nastave: ", m.metodeNastave());
            appendIfPresent(sb, "    Teme / sadržaj kursa: ", m.temeKursa());
            if (m.text() != null && !m.text().isBlank() && !textRedundantWithFields(m)) {
                appendIfPresent(sb, "    Skraćeni tekst za pretragu: ", truncate(m.text(), 900));
            }
        }
        return sb.toString().trim();
    }

    private static boolean textRedundantWithFields(VectorSearchClient.VectorMatch m) {
        String t = m.text() == null ? "" : m.text().trim();
        if (t.length() < 120) {
            return false;
        }
        String fused = (m.cilj() + " " + m.ishodiUcenja() + " " + m.temeKursa() + " " + m.metodeNastave())
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
        if (fused.length() < 80) {
            return false;
        }
        String tn = t.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        String sample = tn.substring(0, Math.min(100, tn.length()));
        return fused.contains(sample.substring(0, Math.min(50, sample.length())));
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sb.append(label).append(value.trim()).append('\n');
    }

    /**
     * Groq prvo (Docker / .env), zatim opcioni OpenAI ključ za lokalni dev, pa konfiguracija.
     */
    private String effectiveLlmApiKey() {
        String groqEnv = System.getenv("GROQ_API_KEY");
        if (groqEnv != null && !groqEnv.isBlank()) {
            return groqEnv.trim();
        }
        String openaiEnv = System.getenv("OPENAI_API_KEY");
        if (openaiEnv != null && !openaiEnv.isBlank()) {
            return openaiEnv.trim();
        }
        String fromProps = naisProperties.getLlm().getOpenaiApiKey();
        return (fromProps != null && !fromProps.isBlank()) ? fromProps.trim() : "";
    }

    private LlmOrFallback callOpenAi(String question, String context, String apiKey) {
        try {
            String qLine = question == null ? "" : question.replace("\r\n", "\n").trim();
            String userBlock = "PITANJE STUDENTA:\n„"
                    + qLine.replace("\n", " ")
                    + "“\n\n"
                    + "Prvo odgovori DIREKTNO na pitanje (npr. koji predmet, da/ne, šta obuhvata). "
                    + "Zatim u 1–3 rečenice dodaj konkretne detalje iz konteksta ispod (naziv predmeta, šifra, teme). "
                    + "Ako kontekst ima više predmeta, reci koji je glavni za pitanje i kratko spomeni ostale. "
                    + "Ne koristi naslove tipa „Odgovor:“; piši kao u četu. "
                    + "Ako podaci u kontekstu ne pokrivaju pitanje, reci to jasno.\n\n"
                    + context;
            var body = new LinkedHashMap<String, Object>();
            body.put("model", naisProperties.getLlm().getOpenaiModel());
            body.put(
                    "messages",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", userBlock)
                    ));
            body.put("temperature", naisProperties.getLlm().getOpenaiTemperature());
            body.put("max_tokens", naisProperties.getLlm().getOpenaiMaxTokens());
            String jsonBody = MAPPER.writeValueAsString(body);
            String baseUrl = naisProperties.getLlm().getOpenaiBaseUrl().trim();
            while (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            if (baseUrl.isEmpty()) {
                baseUrl = "https://api.openai.com/v1";
            }
            RestClient client = restClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            String raw = client.post()
                    .uri("/chat/completions")
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (content.isEmpty() || "Nije moguće generisati odgovor.".equals(content)) {
                log.warn("LLM returned empty assistant content");
                return new LlmOrFallback(MSG_VECTOR_LLM_EMPTY, false);
            }
            return new LlmOrFallback(content, true);
        } catch (Exception e) {
            log.warn("LLM chat completion failed: {}", e.getMessage());
            return new LlmOrFallback(llmFailureMessageForUser(e), false);
        }
    }

    private static String llmFailureMessageForUser(Exception e) {
        String d = e.getMessage();
        if (d == null || d.isBlank()) {
            return "Poziv jezičkom modelu nije uspeo. Proveri GROQ_API_KEY, OPENAI_BASE_URL (Groq), OPENAI_MODEL i mrežu iz kontejnera.";
        }
        if (d.length() > 220) {
            d = d.substring(0, 217) + "...";
        }
        return "Poziv jezičkom modelu nije uspeo: " + d;
    }

    private static String systemPrompt() {
        return String.join(
                "\n",
                "Ti si asistent za univerzitetski informacioni sistem.",
                "Odgovaraš na pitanja korisnika isključivo na osnovu dostupnih podataka (kontekst ispod = evidencija kurikuluma za njegov/njen program).",
                "",
                "Prvo prepoznaj tip pitanja, pa odgovori po pravilima za taj tip.",
                "",
                "TIPOVI PITANJA",
                "",
                "1) Predmeti / kurikulum (najvažnije)",
                "Primeri: „Da li predmet sadrži mašinsko učenje?“, „Gde se spominje Unix?“, „Koji je sadržaj predmeta?“",
                "Ako student traži kompletan sadržaj jednog predmeta (naziv, šifra, opis, cilj, teme, ishodi), koristi doslovno sve što je u kontekstu za taj predmet; prazna polja preskoči — ne izmišljaj.",
                "Pravila:",
                "- Pretraži SVE predmete u kontekstu: naziv, opis, ciljeve, ishode učenja, teme (i ostala polja).",
                "- Koristi semantičko značenje, ne samo identične reči (npr. „Unix“ → „Unix/Linux“ …).",
                "- Odgovor: jasno DA ili NE, zatim naziv predmeta i šifra predmeta iz konteksta, kratko šta obuhvata.",
                "- NE SMEŠ da izmišljaš predmete koji nisu u kontekstu.",
                "- Ako je više predmeta relevantno: navedi najviše 3, najrelevantniji prvi.",
                "",
                "1b) Statistika programa (najteži / najlakši predmeti po prolaznosti)",
                "Ako u kontekstu postoji tekst o rangiranju predmeta (prolaznost, najteži, najlakši), koristi te podatke doslovno — ne izmišljaj druge rang liste ni druge procente.",
                "",
                "2) Pitanja van kurikuluma",
                "Kontekst ispod su samo opisi predmeta. Ako korisnik pita za lične podatke, prosek ili kompletnu listu ocena, a ti vidiš samo ovaj kurikulum:",
                "reci da to nije u ovom tekstu i da za to koristi odeljak profila/ocena u aplikaciji — bez izmišljanja imena, indeksa ili ocena.",
                "",
                "3) Nejasna pitanja",
                "Ako nije jasno šta traži, postavi jedno kratko pojašnjavajuće pitanje.",
                "",
                "OPŠTA PRAVILA",
                "- Uvek prvo direktan odgovor, pa kratko objašnjenje.",
                "- Budi kratak i jasan.",
                "- Ako koncept nije u kontekstu predmeta: jasno reci da NIJE pronađen u dostupnom kurikulumu."
        );
    }

    private static String roughStemSr(String token) {
        if (token.length() <= 4) {
            return token;
        }
        for (String s : SR_STEM_SUFFIXES) {
            if (token.length() > s.length() + 2 && token.endsWith(s)) {
                return token.substring(0, token.length() - s.length());
            }
        }
        return token;
    }

    private static List<String> extractQuestionStems(String question) {
        String folded = foldSerbian(question);
        String[] tokens = folded.split("[^a-zđščćž0-9]+");
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String tok : tokens) {
            if (tok.length() < 3) {
                continue;
            }
            if (STOPWORDS.contains(tok)) {
                continue;
            }
            out.add(tok);
            String st = roughStemSr(tok);
            if (st.length() >= 3 && !STOPWORDS.contains(st) && !st.equals(tok)) {
                out.add(st);
            }
        }
        return new ArrayList<>(out);
    }

    private static List<String> looseCurriculumTokens(String question) {
        String f = foldSerbian(question);
        LinkedHashSet<String> out = new LinkedHashSet<>();
        String[] tech = {
                "unix", "linux", "nosql", "mongodb", "docker", "kubernetes", "oop", "sql", "git", "agil", "scrum",
                "devops", "jenkins", "gitlab", "ansible", "terraform", "pipeline", "cicd",
        };
        for (String t : tech) {
            if (f.contains(t)) {
                out.add(t);
            }
        }
        for (String segment : f.split("[-_/]+")) {
            if (segment.length() < 3 || STOPWORDS.contains(segment)) {
                continue;
            }
            out.add(segment);
            String st = roughStemSr(segment);
            if (st.length() >= 3 && !STOPWORDS.contains(st)) {
                out.add(st);
            }
        }
        return new ArrayList<>(out);
    }

    private static int keywordFitScore(VectorSearchClient.VectorMatch m, List<String> stems) {
        if (stems.isEmpty()) {
            return 0;
        }
        String title = foldSerbian(m.predmetNaziv());
        String hay = title + " "
                + foldSerbian(m.cilj()) + " "
                + foldSerbian(m.ishodiUcenja()) + " "
                + foldSerbian(m.temeKursa()) + " "
                + foldSerbian(m.text());
        int s = 0;
        for (String stem : stems) {
            if (stem.length() < 3) {
                continue;
            }
            if (title.contains(stem)) {
                s += 8;
            } else if (hay.contains(stem)) {
                s += 4;
            }
        }
        return s;
    }

    private static VectorSearchClient.VectorMatch resolvePrimaryForNarrative(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        VectorSearchClient.VectorMatch vectorTop = matches.get(0);
        List<String> stems = extractQuestionStems(question);
        if (stems.isEmpty()) {
            return vectorTop;
        }
        int topScore = keywordFitScore(vectorTop, stems);
        VectorSearchClient.VectorMatch best = vectorTop;
        int bestScore = topScore;
        int n = Math.min(matches.size(), 12);
        for (int i = 1; i < n; i++) {
            VectorSearchClient.VectorMatch m = matches.get(i);
            int sc = keywordFitScore(m, stems);
            if (sc > bestScore) {
                bestScore = sc;
                best = m;
            }
        }
        if (bestScore >= 8 || (bestScore >= 4 && bestScore > topScore)) {
            return best;
        }
        return vectorTop;
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
