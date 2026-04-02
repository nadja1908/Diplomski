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
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.Student;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

    /** Gruba sklanjanja radi poklapanja „bazama“ → „baz“ sa „Baze podataka“. */
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

        if (asksForFullCurriculumList(question)) {
            return buildCurriculumListResponse(student, programId);
        }

        String fq = foldSerbian(question);
        if (wantsUnpassedSubjects(fq)) {
            return buildUnpassedSubjectsResponse(korisnikId, programId);
        }
        AssistantResponse topicList = tryCurriculumTopicListing(question, fq, programId);
        if (topicList != null) {
            return topicList;
        }

        if (asksEverythingAboutStudent(fq)) {
            return buildStudentDataResponse(korisnikId, true, true, true);
        }
        boolean wantProf = wantsProfileSection(question, fq);
        boolean wantGr = wantsGradesSection(fq);
        boolean wantGpa = wantsGpaSection(fq);
        if (wantProf || wantGr || wantGpa) {
            return buildStudentDataResponse(korisnikId, wantProf, wantGr, wantGpa);
        }

        var matches = vectorSearchClient.search(question, 24, allowed);
        matches = dedupeByPredmetKeepBestScore(matches);
        matches = pruneMatchesByRelativeScore(matches);
        if (matches.isEmpty()) {
            return new AssistantResponse(
                    "Nisam našao predmet koji bi pouzdano odgovarao tom pitanju. Probaj drugačije reči ili pitaj za konkretan naziv kursa.",
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
            answer = callOpenAi(question, context, apiKey, matches);
        } else {
            answer = synthesizeNaturalFallbackAnswer(question, matches);
        }
        return new AssistantResponse(answer, sources);
    }

    /** Pitanja tipa „koje predmete imam“ — lista iz PostgreSQL-a, ne Qdrant. */
    private AssistantResponse buildCurriculumListResponse(Student student, Long programId) {
        List<Predmet> predmeti = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        String programNaziv = student.getStudijskiProgram().getNaziv();
        var sb = new StringBuilder();
        sb.append("Na programu „")
                .append(programNaziv)
                .append("” imaš ")
                .append(predmeti.size())
                .append(" predmeta:\n\n");
        for (Predmet p : predmeti) {
            sb.append("• ")
                    .append(p.getNaziv())
                    .append(" (")
                    .append(p.getSifra())
                    .append("), ")
                    .append(p.getEspb())
                    .append(" ESPB\n");
        }
        sb.append("\nZa šta se radi na pojedinom kursu, pitaj u jednoj rečenici (npr. „šta je u Bazama podataka”).");
        List<String> sources = predmeti.stream()
                .map(p -> String.format(
                        "ID %d · %s (%s) · %d ESPB",
                        p.getId(), p.getNaziv(), p.getSifra(), p.getEspb()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString(), sources);
    }

    /** „Koje nisam položio/la“ — kurikulum minus položeni (ocena ≥ 6). */
    private AssistantResponse buildUnpassedSubjectsResponse(Long korisnikId, Long programId) {
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
                    "Po podacima u sistemu izgleda da si položio/la sve predmete sa kurikuluma. Ako nešto fali u evidenciji, proveri kod studentske.",
                    List.of("Kurikulum + ocene · PostgreSQL"));
        }
        var sb = new StringBuilder();
        sb.append("Još nemaš položeno (nema ocene ≥ 6) ovo sa programa:\n\n");
        for (Predmet p : pending) {
            Integer g = bestBySifra.get(p.getSifra());
            if (g != null) {
                sb.append("• ")
                        .append(p.getNaziv())
                        .append(" (")
                        .append(p.getSifra())
                        .append(") — najbolji pokušaj zasad: ")
                        .append(g)
                        .append('\n');
            } else {
                sb.append("• ")
                        .append(p.getNaziv())
                        .append(" (")
                        .append(p.getSifra())
                        .append(") — nema upisanih ispita\n");
            }
        }
        List<String> sources = pending.stream()
                .map(p -> String.format("ID %d · %s (%s)", p.getId(), p.getNaziv(), p.getSifra()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString().trim(), sources);
    }

    /**
     * „Predmeti vezani za programiranje“ itd. — pretraga naziva i kratkog opisa u PostgreSQL-u.
     * Ako nema pogodaka, vraća null pa ide vektorska pretraga.
     */
    private AssistantResponse tryCurriculumTopicListing(String question, String f, Long programId) {
        if (!asksBroadCurriculumTopicSearch(f)) {
            return null;
        }
        List<String> terms = expandCurriculumSearchTerms(question);
        if (terms.isEmpty()) {
            return null;
        }
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        List<Predmet> hits = all.stream()
                .filter(p -> predmetMatchesCurriculumTerms(p, terms))
                .toList();
        if (hits.isEmpty()) {
            return null;
        }
        String intro = curriculumTopicIntroLine(f, terms);
        var sb = new StringBuilder();
        sb.append(intro).append("\n\n");
        for (Predmet p : hits) {
            sb.append("• ")
                    .append(p.getNaziv())
                    .append(" (")
                    .append(p.getSifra())
                    .append("), ")
                    .append(p.getEspb())
                    .append(" ESPB");
            if (p.getKratakOpis() != null && !p.getKratakOpis().isBlank()) {
                sb.append(" — ").append(p.getKratakOpis().trim());
            }
            sb.append('\n');
        }
        sb.append("\nZa detaljnije teme pojedinog kursa pitaj konkretno po nazivu predmeta.");
        List<String> sources = hits.stream()
                .map(p -> String.format(
                        "ID %d · %s (%s) · %d ESPB",
                        p.getId(), p.getNaziv(), p.getSifra(), p.getEspb()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString().trim(), sources);
    }

    private static String curriculumTopicIntroLine(String f, List<String> terms) {
        if (f.contains("programiranje") || f.contains("programira") || f.contains("kodiranje")
                || f.contains("objektno") || f.contains("oop")) {
            return "Evo predmeta na tvom studijskom programu gde se u nazivu ili kratkom opisu eksplicitno radi o programiranju, jezicima ili objektno orijentisanom kodu:";
        }
        if (f.contains("nosql") || f.contains("mongo")) {
            return "NoSQL (dokument-model, MongoDB ili slično, CAP, izbor baze…) u NAIS je uglavnom opisan uz Baze podataka; evo predmeta gde se to pojavljuje u kratkom opisu:";
        }
        if (f.contains("sql") || f.contains("baze") || f.contains("baza")) {
            return "Predmeti gde se u opisu spominju baze podataka, SQL ili srodne teme (po podacima u NAIS):";
        }
        if (f.contains("matematik")) {
            return "Predmeti sa matematikom u nazivu ili opisu:";
        }
        if (f.contains("mrez") || f.contains("mrež")) {
            return "Predmeti vezani za mreže:";
        }
        if (f.contains("bezbednost") || f.contains("bezbednos") || f.contains("sigurnost")) {
            return "Predmeti vezani za bezbednost / sigurnost:";
        }
        return "Predmeti koji odgovaraju tvom upitu (pretraga naziva i kratkog opisa u bazi):";
    }

    private static boolean asksBroadCurriculumTopicSearch(String f) {
        if (f.contains("poloz") || f.contains("polož") || f.contains("nisam")) {
            return false;
        }
        boolean programmingTopic = f.contains("programiranje") || f.contains("programira") || f.contains("kodiranje")
                || f.contains("objektno") || f.contains("oop");
        if (programmingTopic
                && (f.contains("gde") || f.contains("koji") || f.contains("koje") || f.contains("kojim")
                || wordBound(f, "svi") || f.contains("navedi") || f.contains("ispisi") || f.contains("spisak")
                || f.contains("povezan") || f.contains("vezan") || f.contains("veza ") || f.contains("veze "))) {
            return true;
        }
        boolean nosqlAsk = f.contains("nosql") || f.contains("mongo");
        if (nosqlAsk && (f.contains("predmet") || f.contains("kurs") || f.contains("koji") || f.contains("gde"))) {
            return true;
        }
        if (!f.contains("predmet") && !f.contains("kurse") && !f.contains("kurs")) {
            return false;
        }
        if (f.contains("povezan") || f.contains("vezan") || f.contains("veza sa") || f.contains("veze sa")) {
            return true;
        }
        if (f.contains("da li imam predmet") || f.contains("imam li predmet") || f.contains("postoji li predmet")) {
            return true;
        }
        if (f.contains("koji predmeti") || f.contains("kojim predmetima")) {
            return true;
        }
        if (wordBound(f, "svi") && f.contains("predmet")) {
            return true;
        }
        return f.contains("gde se program") || f.contains("u kojim predmetima");
    }

    private static List<String> expandCurriculumSearchTerms(String question) {
        LinkedHashSet<String> t = new LinkedHashSet<>(extractQuestionStems(question));
        String f = foldSerbian(question);
        if (f.contains("programiranje") || f.contains("programira") || f.contains("kodiranje")) {
            t.add("program");
            t.add("programiranje");
            t.add("oop");
            t.add("objektno");
        }
        if (f.contains("nosql") || f.contains("mongo")) {
            t.add("nosql");
            t.add("sql");
        }
        if (f.contains("sql") && !f.contains("nosql")) {
            t.add("sql");
        }
        if (f.contains("matematik")) {
            t.add("matematik");
            t.add("matematika");
        }
        if (f.contains("mrez") || f.contains("mrež")) {
            t.add("mrez");
            t.add("mreze");
        }
        if (f.contains("bezbednost") || f.contains("bezbednos") || f.contains("sigurnost")) {
            t.add("sigurnost");
            t.add("bezbednost");
        }
        return t.stream()
                .filter(s -> s.length() >= 3)
                .toList();
    }

    private static boolean predmetMatchesCurriculumTerms(Predmet p, List<String> terms) {
        String hay = foldSerbian(p.getNaziv() + " "
                + (p.getKratakOpis() == null ? "" : p.getKratakOpis()));
        for (String term : terms) {
            if (hay.contains(term)) {
                return true;
            }
        }
        return false;
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

    /** Profil, ocene, proseka — direktno iz PostgreSQL-a, prirodan ton. */
    private AssistantResponse buildStudentDataResponse(
            Long korisnikId,
            boolean includeProfile,
            boolean includeGrades,
            boolean includeGpa
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
        return new AssistantResponse(sb.toString().trim(), sources);
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
        if (f.contains("spisak") || f.contains("lista") || f.contains("nabro") || f.contains("navedi")) {
            return true;
        }
        if (f.contains("svi predmet") || f.contains("sve predmet") || f.contains("svi kurse") || f.contains("sve kurse")) {
            return true;
        }
        if (f.contains("studijski program") || f.contains("na programu") || f.contains("mom programu")) {
            return true;
        }
        if (wordBound(f, "imam") || wordBound(f, "imate") || wordBound(f, "imamo")) {
            return f.contains("koje") || f.contains("koji") || f.contains("koja") || f.contains("koliko")
                    || f.contains("sta") || wordBound(f, "sve");
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

    private String callOpenAi(
            String question,
            String context,
            String apiKey,
            List<VectorSearchClient.VectorMatch> matchesForFallback
    ) {
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
            return synthesizeNaturalFallbackAnswer(question, matchesForFallback);
        }
    }

    private static String systemPrompt() {
        return String.join("\n",
                "Ti si asistent u sistemu NAIS za studente.",
                "Odgovaraj na srpskom, prirodno i kratko, kao u četu.",
                "Koristi samo podatke iz konteksta; ne izmišljaj predmete.",
                "Kontekst su predmeti sa studijskog programa studenta.",
                "Ako pitanje ne pije vodu ni na jedan predmet iz konteksta, reci to u jednoj rečenici."
        );
    }

    /**
     * Prirodan odgovor bez OpenAI: jedan ili dva pasusa iz najboljih pogodaka.
     * Lista predmeta je u {@code sources} u JSON-u (čet ne duplira bullet liste).
     */
    private static String synthesizeNaturalFallbackAnswer(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        if (matches == null || matches.isEmpty()) {
            return "Trenutno nema pogodaka u vektorskoj bazi. Proveri Qdrant ili probaj druge reči.";
        }
        VectorSearchClient.VectorMatch vectorTop = matches.get(0);
        VectorSearchClient.VectorMatch primary = resolvePrimaryForNarrative(question, matches);
        List<String> stems = extractQuestionStems(question);
        ContentPick chosen = pickBestCourseContent(matches, primary);
        VectorSearchClient.VectorMatch pickMatch = matches.stream()
                .filter(m -> m.predmetId() == chosen.predmetId())
                .findFirst()
                .orElse(primary);
        ContentPick pick = chosen.predmetId() != primary.predmetId()
                && keywordFitScore(primary, stems) >= keywordFitScore(pickMatch, stems)
                ? contentPickFrom(primary)
                : chosen;

        var sb = new StringBuilder();
        sb.append("To je predmet „")
                .append(primary.predmetNaziv())
                .append("” (")
                .append(primary.predmetSifra())
                .append(", ")
                .append(primary.espb())
                .append(" ESPB");
        if (!primary.profesor().isBlank()) {
            sb.append(", ").append(primary.profesor());
        }
        sb.append(").\n\n");

        if (primary.predmetId() != vectorTop.predmetId()) {
            sb.append("(U listi ispod prvi po „sličnosti“ je još „")
                    .append(vectorTop.predmetNaziv())
                    .append("”.)\n\n");
        }

        String body = truncate(pick.temeText(), 560);
        if (body.isBlank()) {
            body = truncate(pick.extraText(), 400);
        }
        boolean richTeme = !isGenericBoilerplateTeme(pick.temeText());
        if (!body.isBlank()) {
            if (richTeme && pick.predmetId() == primary.predmetId()) {
                sb.append("Na tom kursu se između ostalog radi o ovome: ");
            } else if (richTeme) {
                sb.append("Detaljniji opis u bazi je vezan za „").append(pick.naziv()).append("”: ");
            } else if (pick.predmetId() == primary.predmetId()) {
                sb.append("U sistemu za sada stoji kratak, generički opis (uvod, vežbe…). Zvaničan nastavni plan dobijaš na katedri. Tekst iz baze: ");
            } else {
                sb.append("Dodatni tekst u bazi: ");
            }
            sb.append(body).append("\n\n");
        } else {
            sb.append("Za ovaj predmet nemam proširen opis u bazi.\n\n");
        }

        VectorSearchClient.VectorMatch other = null;
        for (VectorSearchClient.VectorMatch m : matches) {
            if (m.predmetId() != primary.predmetId()) {
                other = m;
                break;
            }
        }
        if (other != null) {
            sb.append("Još su blizu temi tvog pitanja i „")
                    .append(other.predmetNaziv())
                    .append("” — vidi izvore ispod.");
        }

        if (!questionMatchesAnyCourseTitle(question, matches) && other == null) {
            sb.append("\n\nAko misliš na nešto drugo, probaj drugačije reči.");
        }
        return sb.toString().trim();
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

    private static int keywordFitScore(VectorSearchClient.VectorMatch m, List<String> stems) {
        if (stems.isEmpty()) {
            return 0;
        }
        String title = foldSerbian(m.predmetNaziv());
        String hay = title + " " + foldSerbian(m.temeKursa()) + " " + foldSerbian(m.text());
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

    /** Kada korisnik piše „bazama“, „sql“ itd., prednost ima predmet čiji naziv/teme to sadrže, ne slepi vektor-vrh. */
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

    private static ContentPick contentPickFrom(VectorSearchClient.VectorMatch m) {
        return new ContentPick(
                m.predmetId(),
                m.predmetNaziv(),
                m.predmetSifra(),
                m.temeKursa(),
                m.text()
        );
    }

    private static boolean isGenericBoilerplateTeme(String teme) {
        if (teme == null || teme.length() < 60) {
            return true;
        }
        String f = foldSerbian(teme);
        return f.contains("osnovne teme iz") && f.contains("pregled literature")
                && f.contains("ispitne prakse");
    }

    private record ContentPick(long predmetId, String naziv, String sifra, String temeText, String extraText) {
    }

    /** Najbolji ne-generički opis teme; polazi od predmeta koji koristimo u narativu. */
    private static ContentPick pickBestCourseContent(
            List<VectorSearchClient.VectorMatch> matches,
            VectorSearchClient.VectorMatch narrativePrimary
    ) {
        if (!isGenericBoilerplateTeme(narrativePrimary.temeKursa())) {
            return contentPickFrom(narrativePrimary);
        }
        int n = Math.min(matches.size(), 8);
        for (int i = 0; i < n; i++) {
            VectorSearchClient.VectorMatch m = matches.get(i);
            if (!isGenericBoilerplateTeme(m.temeKursa())) {
                return contentPickFrom(m);
            }
        }
        return contentPickFrom(narrativePrimary);
    }

    private static boolean questionMatchesAnyCourseTitle(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        List<String> stems = extractQuestionStems(question);
        if (stems.isEmpty()) {
            return false;
        }
        return matches.stream().anyMatch(m -> {
            String t = foldSerbian(m.predmetNaziv());
            return stems.stream().anyMatch(s -> s.length() >= 3 && t.contains(s));
        });
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
