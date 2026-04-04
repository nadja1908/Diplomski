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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Nakon deduplikacije po predmetu: zadrži samo pogotke čiji je skor blizu najboljeg.
     * Smanjuje „šum“ kad mnogi predmeti dele isti generički opis kursa u embedding prostoru.
     */
    private static final double VECTOR_SCORE_GAP_FROM_BEST = 0.14;
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
        if (wantsCurriculumRemainder(fq)) {
            return buildCurriculumRemainderResponse(korisnikId);
        }
        if (wantsTotalExamAttempts(fq)) {
            return buildExamAttemptsSummaryResponse(korisnikId);
        }
        AssistantResponse topicList = tryCurriculumTopicListing(question, fq, programId);
        if (topicList != null) {
            return topicList;
        }

        AssistantResponse syllabus = trySubjectSyllabusDetailResponse(question, fq, programId, allowed);
        if (syllabus != null) {
            return syllabus;
        }

        AssistantResponse sqlByWords = trySqlCurriculumKeywordAnswer(question, fq, programId);
        if (sqlByWords != null) {
            return sqlByWords;
        }

        if (asksEverythingAboutStudent(fq)) {
            return buildStudentDataResponse(korisnikId, true, true, true);
        }
        if (wantsPassRokSummary(fq)) {
            return buildPassRokSummaryResponse(korisnikId, question, programId);
        }
        boolean wantProf = wantsProfileSection(question, fq);
        boolean wantGr = wantsGradesSection(fq);
        boolean wantGpa = wantsGpaSection(fq);
        if (wantProf || wantGr || wantGpa) {
            return buildStudentDataResponse(korisnikId, wantProf, wantGr, wantGpa);
        }

        List<VectorSearchClient.VectorMatch> matches = searchVectorWithExplicitCourseBoost(question, allowed, programId);
        if (matches.isEmpty()) {
            return new AssistantResponse(
                    "Ne bih da nagađam — među predmetima na tvom programu nisam našao nešto što pouzdano odgovara tom pitanju. "
                            + "Probaj drugačije reči ili napiši tačan naziv kursa.",
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

        List<VectorSearchClient.VectorMatch> forLlm = orderMatchesForLlm(question, matches);
        String context = buildStructuredContext(forLlm);

        String answer;
        String apiKey = naisProperties.getLlm().getOpenaiApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            answer = callOpenAi(question, context, apiKey, matches);
        } else {
            answer = synthesizeNaturalFallbackAnswer(question, matches);
        }
        return new AssistantResponse(answer, sources);
    }

    /**
     * Ako student u pitanju navede pun naziv predmeta sa programa (npr. „Kompjuterska arhitektura“),
     * taj predmet se uzima kao primarni — ne slepi vrh vektorske pretrage koji može biti pogrešan kurs.
     */
    private List<VectorSearchClient.VectorMatch> searchVectorWithExplicitCourseBoost(
            String question,
            List<Long> allowed,
            Long programId
    ) {
        List<VectorSearchClient.VectorMatch> broad = vectorSearchClient.search(question, 32, allowed);
        broad = dedupeByPredmetKeepBestScore(broad);
        broad = mergeLexicalKurikulumHits(question, programId, broad);
        broad = dedupeByPredmetKeepBestScore(broad);
        Optional<Predmet> explicit = findPredmetExplicitlyNamedInQuestion(question, programId);
        if (explicit.isEmpty()) {
            return pruneMatchesByRelativeScore(broad);
        }
        Predmet p = explicit.get();
        List<VectorSearchClient.VectorMatch> focused =
                vectorSearchClient.search(p.getNaziv(), 14, List.of(p.getId()));
        focused = dedupeByPredmetKeepBestScore(focused);
        VectorSearchClient.VectorMatch anchor = focused.isEmpty() ? null : focused.get(0);
        List<VectorSearchClient.VectorMatch> rest = broad.stream()
                .filter(m -> m.predmetId() != p.getId())
                .toList();
        rest = pruneMatchesByRelativeScore(rest);
        List<VectorSearchClient.VectorMatch> out = new ArrayList<>();
        if (anchor != null) {
            out.add(anchor);
        } else {
            String ko = p.getKratakOpis() == null ? "" : p.getKratakOpis().trim();
            if (!ko.isBlank()) {
                out.add(new VectorSearchClient.VectorMatch(
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
                ));
            } else {
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
                        "",
                        "sql_samo_naziv",
                        0.9
                ));
            }
        }
        out.addAll(rest);
        return out;
    }

    /**
     * Vektorski indeks (chunks.jsonl → Qdrant) često nije ažuran kao PostgreSQL kurikulum. Zato za reči iz
     * pitanja koje se pojave u nazivu ili kratkom opisu predmeta u bazi umećemo pouzdan pogodak (npr. „etika“
     * → „Etika i privatnost podataka“ iako u Qdrantu nema chunk-a za taj predmet).
     */
    private List<VectorSearchClient.VectorMatch> mergeLexicalKurikulumHits(
            String question,
            Long programId,
            List<VectorSearchClient.VectorMatch> vectorMatches
    ) {
        LinkedHashSet<String> stemSet = new LinkedHashSet<>(extractQuestionStems(question));
        stemSet.addAll(looseCurriculumTokens(question));
        List<String> stems = new ArrayList<>(stemSet);
        if (stems.isEmpty()) {
            return vectorMatches;
        }
        Map<Long, Double> scoreByPredmet = new LinkedHashMap<>();
        Map<Long, Predmet> predmetById = new LinkedHashMap<>();
        List<Predmet> all = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        for (Predmet p : all) {
            String title = foldSerbian(p.getNaziv());
            String desc = p.getKratakOpis() == null ? "" : foldSerbian(p.getKratakOpis());
            double best = 0;
            for (String stem : stems) {
                if (stem.length() < 3) {
                    continue;
                }
                if (title.contains(stem)) {
                    best = Math.max(best, stem.length() >= 4 ? 0.86 : 0.8);
                } else if (stem.length() >= 3 && desc.contains(stem)) {
                    best = Math.max(best, stem.length() >= 4 ? 0.72 : 0.66);
                }
            }
            if (best > 0) {
                scoreByPredmet.merge(p.getId(), best, Math::max);
                predmetById.putIfAbsent(p.getId(), p);
            }
        }
        if (scoreByPredmet.isEmpty()) {
            return vectorMatches;
        }
        List<VectorSearchClient.VectorMatch> lexicalRows = new ArrayList<>();
        for (var e : scoreByPredmet.entrySet()) {
            lexicalRows.add(vectorMatchFromPredmetLexical(predmetById.get(e.getKey()), e.getValue()));
        }
        lexicalRows.sort(Comparator.comparingDouble(VectorSearchClient.VectorMatch::score).reversed());
        List<VectorSearchClient.VectorMatch> merged = new ArrayList<>(lexicalRows);
        merged.addAll(vectorMatches);
        return merged;
    }

    private static VectorSearchClient.VectorMatch vectorMatchFromPredmetLexical(Predmet p, double score) {
        String ko = p.getKratakOpis() == null ? "" : p.getKratakOpis().trim();
        String text = ko.isBlank() ? p.getNaziv() : p.getNaziv() + " — " + ko;
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
                text,
                "sql_naziv_opis_lex",
                score
        );
    }

    /**
     * Pronalazi predmet čiji se pun naziv (ili bar dve značajne reči naziva) pojavljuje u pitanju.
     * Duži nazivi prvi — da „Računarska i informaciona tehnologija“ pobedi „Računarske mreže“ kad je ceo naziv u tekstu.
     */
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
                if (!fq.contains(w)) {
                    sig = -1;
                    break;
                }
                sig++;
            }
            if (sig >= 2) {
                return Optional.of(pr);
            }
        }
        return Optional.empty();
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

    /** Šta još nije položeno + status po kurikulumu (iz napredovanja). */
    private AssistantResponse buildCurriculumRemainderResponse(Long korisnikId) {
        AcademicQueryService.CurriculumProgressDto cp = academicQueryService.curriculumProgress(korisnikId);
        var sb = new StringBuilder();
        sb.append("Evo pregleda na osnovu evidencije u sistemu.\n\n");
        sb.append(String.format(
                "Program: „%s“ (%s). Ukupno predmeta na kurikulumu: %d.\n"
                        + "Položeno (ocena ≥ 6): %d. Aktivno nepoloženo (pokušaji ispod 6 ili u toku): %d. "
                        + "Bez izlaska u ovoj fazi: %d. Kasnije u nastavnom planu: %d.\n\n",
                cp.studijskiProgramNaziv(),
                cp.studijskiProgramSifra(),
                cp.ukupnoPredmetaNaProgramu(),
                cp.brojPolozenih(),
                cp.brojNepolozenih(),
                cp.brojBezIzlaska(),
                cp.brojPredmetaKasnije()));
        sb.append("Predmeti koje još nemaš položeno (nema ocene ≥ 6):\n\n");
        int lines = 0;
        final int maxLines = 40;
        for (AcademicQueryService.CurriculumSubjectDto row : cp.predmeti()) {
            if ("POLOZENO".equals(row.status())) {
                continue;
            }
            if (lines >= maxLines) {
                sb.append("\n… lista je skraćena; ostatak vidi u portfelju / evidenciji ocena.\n");
                break;
            }
            sb.append("• ")
                    .append(row.naziv())
                    .append(" (")
                    .append(row.sifra())
                    .append(", ")
                    .append(row.espb())
                    .append(" ESPB) — ")
                    .append(humanCurriculumStatus(row))
                    .append('\n');
            lines++;
        }
        if (lines == 0) {
            sb.append("— Nemaš otvorenih nepoloženih predmeta po ovom pregledu.\n");
        }
        return new AssistantResponse(sb.toString().trim(), List.of("Kurikulum i napredovanje · PostgreSQL"));
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

    private AssistantResponse buildExamAttemptsSummaryResponse(Long korisnikId) {
        AcademicQueryService.GpaDto g = academicQueryService.gpa(korisnikId);
        List<AcademicQueryService.SubjectGradeDto> list = academicQueryService.subjectsAndGrades(korisnikId);
        var sb = new StringBuilder();
        sb.append(String.format(
                "Ukupan broj evidentiranih izlazaka na ispite (svi predmeti): %d.\n",
                g.ukupnoIspita()));
        if (list.isEmpty()) {
            sb.append("Još nemaš pojedinačnih unosa ocena u sistemu — ova informacija nije dostupna po predmetima.");
            return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"));
        }
        Map<String, Long> countBySifra = list.stream()
                .collect(Collectors.groupingBy(AcademicQueryService.SubjectGradeDto::predmetSifra, Collectors.counting()));
        sb.append("Po predmetima (broj izlazaka = broj upisanih termina u evidenciji):\n\n");
        countBySifra.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(25)
                .forEach(e -> {
                    String naziv = list.stream()
                            .filter(x -> x.predmetSifra().equals(e.getKey()))
                            .map(AcademicQueryService.SubjectGradeDto::predmetNaziv)
                            .findFirst()
                            .orElse(e.getKey());
                    sb.append("• ")
                            .append(naziv)
                            .append(" (")
                            .append(e.getKey())
                            .append("): ")
                            .append(e.getValue())
                            .append("×\n");
                });
        if (countBySifra.size() > 25) {
            sb.append("\n… prikazano 25 predmeta sa najviše izlazaka.");
        }
        return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"));
    }

    private AssistantResponse buildPassRokSummaryResponse(Long korisnikId, String question, Long programId) {
        List<AcademicQueryService.SubjectGradeDto> list = academicQueryService.subjectsAndGrades(korisnikId);
        List<AcademicQueryService.SubjectGradeDto> passed = list.stream()
                .filter(sg -> sg.ocena() >= 6)
                .toList();
        if (passed.isEmpty()) {
            return new AssistantResponse(
                    "Nemaš još položenih predmeta (ocena ≥ 6) u evidenciji — nema podatka o roku položenog ispita.",
                    List.of("Ocene · PostgreSQL"));
        }
        Optional<String> sifraFilter = findPredmetSifraHintInQuestion(question, programId);
        Map<String, List<AcademicQueryService.SubjectGradeDto>> bySifra = passed.stream()
                .filter(sg -> sifraFilter.isEmpty() || sifraFilter.get().equalsIgnoreCase(sg.predmetSifra()))
                .collect(Collectors.groupingBy(AcademicQueryService.SubjectGradeDto::predmetSifra));
        if (bySifra.isEmpty()) {
            return new AssistantResponse(
                    "Nisam našao položen ispit za predmet koji liči na tvoje pitanje. "
                            + "Probaj tačan naziv predmeta ili pogledaj listu svih ocena.",
                    List.of("Ocene · PostgreSQL"));
        }
        var sb = new StringBuilder();
        sb.append("Po evidenciji, položio/la si (prvi put sa ocenom ≥ 6) ovako:\n\n");
        for (var e : bySifra.entrySet()) {
            List<AcademicQueryService.SubjectGradeDto> rows = e.getValue().stream()
                    .sorted(Comparator.comparing(AcademicQueryService.SubjectGradeDto::datumIspita))
                    .toList();
            AcademicQueryService.SubjectGradeDto firstPass = rows.get(0);
            sb.append("• „")
                    .append(firstPass.predmetNaziv())
                    .append("” (")
                    .append(firstPass.predmetSifra())
                    .append(") — prvi položeni pokušaj: ")
                    .append(firstPass.ispitniRok())
                    .append(", datum ")
                    .append(shortDate(firstPass.datumIspita()))
                    .append(", ocena ")
                    .append(firstPass.ocena())
                    .append('\n');
        }
        return new AssistantResponse(sb.toString().trim(), List.of("Ocene · PostgreSQL"));
    }

    /**
     * Ako u pitanju deluje da je naveden naziv predmeta sa programa, vrati njegovu šifru za filtriranje.
     */
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

    /**
     * Pitanja tipa „da li predmet sadrži X“ / „gde se spominje Y“ — SQL pretraga naziva i kratkog opisa kada široka
     * lista ({@link #tryCurriculumTopicListing}) ne pokrije oblik pitanja. Vraća null ako nema pogodaka ili nije taj namer.
     */
    private AssistantResponse trySqlCurriculumKeywordAnswer(String question, String f, Long programId) {
        if (!sqlCurriculumKeywordQuestion(f)) {
            return null;
        }
        LinkedHashSet<String> termSet = new LinkedHashSet<>(extractQuestionStems(question));
        termSet.addAll(looseCurriculumTokens(question));
        termSet.removeIf(t -> "predmet".equals(t) || "kurse".equals(t) || "kurs".equals(t) || "koji".equals(t)
                || "koje".equals(t) || "kojim".equals(t) || "kojih".equals(t) || "studij".equals(t)
                || "program".equals(t) || "nesto".equals(t) || "nešto".equals(t));
        List<String> terms = termSet.stream().filter(t -> t.length() >= 3).toList();
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
        var sb = new StringBuilder();
        sb.append("Po nazivu i kratkom opisu u bazi, ovo na tvom studijskom programu odgovara traženim rečima:\n\n");
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
        sb.append("\nZa detaljnije teme pitaj konkretno po nazivu predmeta.");
        List<String> sources = hits.stream()
                .map(p -> String.format(
                        "ID %d · %s (%s) · %d ESPB",
                        p.getId(), p.getNaziv(), p.getSifra(), p.getEspb()))
                .collect(Collectors.toList());
        return new AssistantResponse(sb.toString().trim(), sources);
    }

    private static boolean sqlCurriculumKeywordQuestion(String f) {
        if (wantsFullSubjectSyllabus(f)) {
            return false;
        }
        if (f.contains("koliko puta") || f.contains("broj izlazaka")) {
            return false;
        }
        if (wantsCurriculumRemainder(f) || wantsTotalExamAttempts(f) || wantsPassRokSummary(f)) {
            return false;
        }
        if (wantsUnpassedSubjects(f)) {
            return false;
        }
        if ((f.contains("ocen") || f.contains("prosek") || f.contains("prose"))
                && !f.contains("sadrzi") && !f.contains("sadrži") && !f.contains("spominje")
                && !f.contains("pominje")) {
            return false;
        }
        boolean hasPredmetOrKurs = f.contains("predmet") || f.contains("kurse") || f.contains("kurs");
        if (f.contains("sadrzi") || f.contains("sadrži") || (f.contains("sadrzaj") && hasPredmetOrKurs)) {
            return hasPredmetOrKurs || f.contains("kurikulum") || f.contains("studij");
        }
        if ((f.contains("spominje") || f.contains("pominje") || f.contains("pominju"))
                && (hasPredmetOrKurs || f.contains("gde") || f.contains("negde"))) {
            return true;
        }
        return f.contains("da li") && hasPredmetOrKurs
                && (f.contains("unix") || f.contains("linux") || f.contains("masin") || f.contains("ucenje")
                || f.contains("ucim") || f.contains("algebr"));
    }

    /**
     * Kompletan prikaz sadržaja jednog predmeta (PostgreSQL + svi relevantni fragmenti iz vektorskog indeksa), bez LLM-a.
     */
    private AssistantResponse trySubjectSyllabusDetailResponse(
            String question,
            String f,
            Long programId,
            List<Long> allowed
    ) {
        boolean phraseIntent = wantsFullSubjectSyllabus(f);
        Optional<Predmet> named = findPredmetExplicitlyNamedInQuestion(question, programId);
        boolean bareTitle = !phraseIntent && named.isPresent() && looksLikeBareCourseTitleQuery(f);
        if (!phraseIntent && !bareTitle) {
            return null;
        }
        Optional<Predmet> pred = named;
        if (pred.isEmpty() && phraseIntent) {
            pred = resolvePredmetForSyllabusViaSearch(question, allowed);
        }
        if (pred.isEmpty()) {
            return new AssistantResponse("Predmet nije pronađen u dostupnom kurikulumu.", List.of());
        }
        Predmet p = pred.get();
        if (!p.getStudijskiProgram().getId().equals(programId)) {
            return new AssistantResponse("Predmet nije pronađen u dostupnom kurikulumu.", List.of());
        }
        List<VectorSearchClient.VectorMatch> chunks = vectorSearchClient.search(p.getNaziv(), 32, List.of(p.getId()));
        if (chunks.isEmpty()) {
            chunks = vectorSearchClient.search(question, 32, List.of(p.getId()));
        }
        String body = formatSubjectSyllabusFromSources(p, chunks);
        List<String> sources = buildSyllabusSourceLines(p, chunks);
        return new AssistantResponse(body, sources);
    }

    private Optional<Predmet> resolvePredmetForSyllabusViaSearch(String question, List<Long> allowed) {
        List<VectorSearchClient.VectorMatch> hits = vectorSearchClient.search(question, 14, allowed);
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        hits = dedupeByPredmetKeepBestScore(hits);
        VectorSearchClient.VectorMatch top = hits.get(0);
        if (top.score() < 0.16) {
            return Optional.empty();
        }
        return predmetRepository.findById(top.predmetId());
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

    private static String formatSubjectSyllabusFromSources(Predmet p, List<VectorSearchClient.VectorMatch> chunks) {
        var sb = new StringBuilder();
        sb.append("Predmet „")
                .append(p.getNaziv())
                .append("” (šifra ")
                .append(p.getSifra())
                .append("), ")
                .append(p.getEspb())
                .append(" ESPB.\n\n");
        appendIfPresent(sb, "Opis: ", p.getKratakOpis());
        appendIfPresent(sb, "Cilj: ", mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::cilj));
        appendIfPresent(sb, "Sadržaj / teme kursa: ", mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::temeKursa));
        appendIfPresent(sb, "Ishodi učenja: ", mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::ishodiUcenja));
        appendIfPresent(sb, "Metode nastave: ", mergeUniqueNonBlankChunks(chunks, VectorSearchClient.VectorMatch::metodeNastave));
        String extra = mergeDistinctSyllabusTextFragments(chunks);
        appendIfPresent(sb, "Dodatni tekst iz indeksa: ", extra);
        return sb.toString().trim();
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

    /**
     * Najrelevantniji predmet prvi, zatim ostali (maks. ukupno {@code max}) da model ne gubi fokus u šumu.
     */
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

    /** Ne dupliraj u kontekstu ako je „text“ uglavnom isto što i strukturisana polja. */
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

    private String callOpenAi(
            String question,
            String context,
            String apiKey,
            List<VectorSearchClient.VectorMatch> matchesForFallback
    ) {
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

    /**
     * Prirodan odgovor bez OpenAI: jedan ili dva pasusa iz najboljih pogodaka.
     * Lista predmeta je u {@code sources} u JSON-u (čet ne duplira bullet liste).
     */
    private static String synthesizeNaturalFallbackAnswer(
            String question,
            List<VectorSearchClient.VectorMatch> matches
    ) {
        if (matches == null || matches.isEmpty()) {
            return "Iz ovih reči mi nije uspelo da nađem pouzdan pogodak među predmetima na tvom programu. "
                    + "Probaj drugačiju formulaciju ili konkretan naziv kursa.";
        }
        String fq = foldSerbian(question);
        if (asksIfTopicMentionedSomewhere(fq)) {
            List<String> stems = extractQuestionStems(question);
            if (!stems.isEmpty()) {
                List<VectorSearchClient.VectorMatch> mentionHits = new ArrayList<>();
                for (VectorSearchClient.VectorMatch m : matches) {
                    if (keywordFitScore(m, stems) > 0) {
                        mentionHits.add(m);
                    }
                }
                mentionHits.sort(Comparator.comparingInt((VectorSearchClient.VectorMatch m) -> keywordFitScore(m, stems))
                        .reversed());
                mentionHits = dedupeMentionList(mentionHits);
                if (!mentionHits.isEmpty()) {
                    return formatMentionFallbackAnswer(mentionHits);
                }
            }
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
        sb.append("Najbliže onome što pitaš deluje predmet ")
                .append("„")
                .append(primary.predmetNaziv())
                .append("” — ")
                .append(primary.espb())
                .append(" ESPB, šifra ")
                .append(primary.predmetSifra());
        if (!primary.profesor().isBlank()) {
            sb.append(", predmet vodi ").append(primary.profesor());
        }
        sb.append(".\n\n");

        if (primary.predmetId() != vectorTop.predmetId()) {
            sb.append("Uz to, pretraga je istakla i „")
                    .append(vectorTop.predmetNaziv())
                    .append("” jer mu je opis sličan temi — vredi baciti pogled i tamo.\n\n");
        }

        String body = truncate(pick.temeText(), 560);
        if (body.isBlank()) {
            body = truncate(pick.extraText(), 400);
        }
        boolean richTeme = !isGenericBoilerplateTeme(pick.temeText());
        if (!body.isBlank()) {
            if (richTeme && pick.predmetId() == primary.predmetId()) {
                sb.append("Po opisu kursa, između ostalog ulazi i ovo: ");
            } else if (richTeme) {
                sb.append("Detaljnije je opisano kod „").append(pick.naziv()).append("”: ");
            } else if (pick.predmetId() == primary.predmetId()) {
                sb.append(
                        "U bazi je za sada kratak, prilično opšti tekst; tačan plan obično dobiješ od katedre. "
                                + "Evo šta piše: ");
            } else {
                sb.append("Još jedan srodan opis u bazi: ");
            }
            sb.append(body).append("\n\n");
        } else {
            sb.append("Za ovaj predmet u bazi još nema dužeg opisa sadržaja.\n\n");
        }

        VectorSearchClient.VectorMatch other = null;
        for (VectorSearchClient.VectorMatch m : matches) {
            if (m.predmetId() != primary.predmetId()) {
                other = m;
                break;
            }
        }
        if (other != null) {
            sb.append("Blizu su ti i teme sa „")
                    .append(other.predmetNaziv())
                    .append("” — detalje vidi u izvorima ispod.");
        }

        if (!questionMatchesAnyCourseTitle(question, matches) && other == null) {
            sb.append("\n\nAko nije to što tražiš, probaj drugačije reči ili tačan naziv predmeta.");
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

    /**
     * Dodatni tokeni za leksičku pretragu kurikuluma (skraćenice, UNIX/Linux, crtice u složenim izrazima).
     */
    private static List<String> looseCurriculumTokens(String question) {
        String f = foldSerbian(question);
        LinkedHashSet<String> out = new LinkedHashSet<>();
        String[] tech = {
                "unix", "linux", "nosql", "mongodb", "docker", "kubernetes", "oop", "sql", "git", "agil", "scrum",
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

    private static boolean asksIfTopicMentionedSomewhere(String f) {
        return f.contains("spominje")
                || f.contains("pominje")
                || f.contains("pominju")
                || (f.contains("negde") && (f.contains("spominje") || f.contains("pominje") || f.contains("naveden")))
                || (f.contains("da li") && (f.contains("postoji") || f.contains("ima")))
                || (f.contains("gde") && (f.contains("ucim") || f.contains("radim")));
    }

    private static List<VectorSearchClient.VectorMatch> dedupeMentionList(List<VectorSearchClient.VectorMatch> in) {
        Map<Long, VectorSearchClient.VectorMatch> byId = new LinkedHashMap<>();
        for (VectorSearchClient.VectorMatch m : in) {
            byId.putIfAbsent(m.predmetId(), m);
        }
        return new ArrayList<>(byId.values());
    }

    private static String formatMentionFallbackAnswer(List<VectorSearchClient.VectorMatch> hits) {
        var sb = new StringBuilder();
        sb.append("Da — u opisima predmeta na tvom programu to se odnosi bar na:\n\n");
        int n = Math.min(hits.size(), 6);
        for (int i = 0; i < n; i++) {
            VectorSearchClient.VectorMatch m = hits.get(i);
            sb.append("• „")
                    .append(m.predmetNaziv())
                    .append("” (")
                    .append(m.predmetSifra())
                    .append(", ")
                    .append(m.espb())
                    .append(" ESPB)\n");
        }
        if (hits.size() > n) {
            sb.append("\nIma još srodnih pogodaka u listi izvora ispod.");
        }
        sb.append(
                "\n\nZa detaljan plan nastave najbolje je proveriti sa katedrom ili službenim nastavnim planom.");
        return sb.toString().trim();
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
