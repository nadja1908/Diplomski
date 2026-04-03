package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.academic.AcademicProgressionRules;
import rs.ac.uns.acs.nais.academic.LinearAcademicTimeline;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.StudijskiProgram;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.GenerationBreakdownRow;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.ProgramStatisticsResponse;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.ProgramSummary;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.RankingsBundle;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsFilterOptions;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsQueryParams;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.SubjectStatisticsRow;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.UnpassedSubjectPassRateDto;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;
import rs.ac.uns.acs.nais.repository.StudijskiProgramRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analitika predmeta po studijskom programu iz relacione baze.
 *
 * <p><strong>Studenti koji su polagali</strong> ({@code totalStudentsWhoTook}): broj različitih studenata
 * koji imaju bar jedan zapis u {@code ocena} za dati predmet (posle filtara na generaciju / školsku godinu).</p>
 *
 * <p><strong>Studenti koji su položili</strong> ({@code totalStudentsWhoPassed}): među tim studentima, oni čija je
 * <em>najbolja</em> ocena na tom predmetu &gt;= 6. Jedan student se broji jednom.</p>
 *
 * <p><strong>Stopa prolaznosti</strong>: {@code passed / took * 100}. „Najteži“ predmet = najniža stopa među
 * predmetima gde je {@code took &gt; 0}.</p>
 *
 * <p><strong>Prosečna ocena</strong>: aritmetička sredina svih <em>redova</em> {@code ocena} gde je
 * {@code vrednost_ocene &gt;= 6} (ne proseka generacija). Blagi studenti sa više položenih pokušaja ulaze više puta
 * u prosek — što odgovara zahtevu „iz stvarnih zapisa“, alternativa bi bila jedna ocena po studentu (npr. najbolja).</p>
 *
 * <p><strong>Medijana</strong>: {@code percentile_cont(0.5)} nad istim skupom položenih ocena (redovi sa ocenom &gt;= 6).</p>
 */
@Service
@RequiredArgsConstructor
public class ProgramSubjectAnalyticsService {

    private static final int PASSING_GRADE = 6;

    private final NamedParameterJdbcTemplate namedJdbc;
    private final PredmetRepository predmetRepository;
    private final StudijskiProgramRepository studijskiProgramRepository;
    private final StudentRepository studentRepository;
    private final LinearAcademicTimeline linearAcademicTimeline;

    /**
     * Nepoloženi predmeti iz tekuće i svih prethodnih godina kurikuluma (prema procenjenoj godini studija),
     * bez predmeta iz budućih godina kurikuluma; sa programskom stopom prolaznosti.
     * Redosled: godina kurikuluma, semestar, naziv predmeta.
     */
    @Transactional(readOnly = true)
    public List<UnpassedSubjectPassRateDto> unpassedSubjectsPassRatesSorted(long korisnikId) {
        var student = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalArgumentException("Student nije pronađen"));
        long programId = student.getStudijskiProgram().getId();
        long studentPk = student.getId();
        LocalDate today = LocalDate.now();
        int procenjenaGodinaStudija = AcademicProgressionRules.procenjenaGodinaStudijaAkademska(
                student.getGodinaUpisa(), today);
        int maxKurikulumGodina = Math.min(4, Math.max(1, procenjenaGodinaStudija));

        Map<Long, Integer> bestByPredmet = new HashMap<>();
        MapSqlParameterSource bestParams = new MapSqlParameterSource()
                .addValue("sid", studentPk)
                .addValue("gu", student.getGodinaUpisa());
        List<Map<String, Object>> bestRows = namedJdbc.queryForList(
                """
                        SELECT it.predmet_id AS pid, MAX(o.vrednost_ocene) AS best
                        FROM ocena o
                        JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                        JOIN predmet p ON it.predmet_id = p.id
                        WHERE o.student_id = :sid
                          AND (:gu <= 2021
                               OR nais_ocena_je_u_redu(:gu, it.datum_vreme, p.kurikulum_godina, p.kurikulum_semestar))
                        GROUP BY it.predmet_id
                        """,
                bestParams);
        for (Map<String, Object> row : bestRows) {
            bestByPredmet.put(
                    ((Number) row.get("pid")).longValue(),
                    ((Number) row.get("best")).intValue());
        }

        Set<Long> unpassedPredmetIds = new HashSet<>();
        List<Predmet> programPredmeti = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        Map<Long, Predmet> predmetEntityById = programPredmeti.stream()
                .collect(Collectors.toMap(Predmet::getId, Function.identity()));
        for (Predmet p : programPredmeti) {
            int kg = p.getKurikulumGodina();
            // Svi semestri u okviru dozvoljenih godina kurikuluma (npr. 1. godina → i 1. i 2. semestar).
            if (kg < 1 || kg > maxKurikulumGodina) {
                continue;
            }
            Integer best = bestByPredmet.get(p.getId());
            if (best == null || best < PASSING_GRADE) {
                unpassedPredmetIds.add(p.getId());
            }
        }

        ProgramStatisticsResponse full =
                computeForProgram(programId, StatisticsQueryParams.defaultAll());
        return full.subjects().stream()
                .filter(s -> unpassedPredmetIds.contains(s.subjectId()))
                .sorted(Comparator
                        .comparingInt(SubjectStatisticsRow::kurikulumGodina)
                        .thenComparingInt(SubjectStatisticsRow::semestar)
                        .thenComparing(SubjectStatisticsRow::subjectName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> {
                    Predmet pr = predmetEntityById.get(s.subjectId());
                    if (pr == null) {
                        throw new IllegalStateException("Predmet van mape programa: " + s.subjectId());
                    }
                    return new UnpassedSubjectPassRateDto(
                            pr.getSifra(),
                            pr.getNaziv(),
                            pr.getKurikulumGodina(),
                            pr.getKurikulumSemestar(),
                            s.passRate());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramStatisticsResponse computeForProgram(long programId, StatisticsQueryParams params) {
        StudijskiProgram prog = studijskiProgramRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Studijski program ne postoji: " + programId));
        List<Predmet> predmetiOrdered = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        Map<Long, CurriculumSlot> curriculumByPredmetId = buildCurriculumMap(predmetiOrdered);

        Set<Long> linearPredmetFilter = null;
        if (params.godinaUpisa() != null
                && params.godinaUpisa() != 2021
                && linearAcademicTimeline.koristiStrogiLinearniModel(params.godinaUpisa())) {
            linearPredmetFilter = linearAcademicTimeline.predmetIdsDostupniZaGeneraciju(
                    params.godinaUpisa(),
                    curriculumSlotsByPredmetId(predmetiOrdered));
        }

        MapSqlParameterSource baseParams = new MapSqlParameterSource("programId", programId);
        String filteredOcenaCte = buildFilteredOcenaCte(params, baseParams, linearPredmetFilter);

        String mainSql = """
                %s
                , per_student_predmet AS (
                    SELECT predmet_id,
                           student_id,
                           MAX(vrednost_ocene) AS best_grade,
                           COUNT(*) AS attempt_count
                    FROM filtered_ocene
                    GROUP BY predmet_id, student_id
                ),
                took_passed AS (
                    SELECT predmet_id,
                           COUNT(*) AS took,
                           SUM(CASE WHEN best_grade >= %d THEN 1 ELSE 0 END) AS passed
                    FROM per_student_predmet
                    GROUP BY predmet_id
                ),
                passing_grades AS (
                    SELECT predmet_id, vrednost_ocene
                    FROM filtered_ocene
                    WHERE vrednost_ocene >= %d
                ),
                grade_agg AS (
                    SELECT predmet_id,
                           AVG(vrednost_ocene::numeric) AS avg_passing,
                           MAX(vrednost_ocene) AS highest_grade,
                           MIN(vrednost_ocene) AS lowest_passing,
                           COUNT(*)::bigint AS passing_row_count
                    FROM passing_grades
                    GROUP BY predmet_id
                ),
                median_passing AS (
                    SELECT predmet_id,
                           percentile_cont(0.5) WITHIN GROUP (ORDER BY vrednost_ocene::double precision) AS median_val
                    FROM passing_grades
                    GROUP BY predmet_id
                ),
                attempts AS (
                    SELECT predmet_id, COUNT(*)::bigint AS total_attempts
                    FROM filtered_ocene
                    GROUP BY predmet_id
                )
                SELECT p.id AS predmet_id,
                       p.sifra,
                       p.naziv,
                       p.espb,
                       COALESCE(tp.took, 0)::bigint AS took,
                       COALESCE(tp.passed, 0)::bigint AS passed,
                       ga.avg_passing,
                       ga.highest_grade,
                       ga.lowest_passing,
                       mp.median_val,
                       COALESCE(a.total_attempts, 0)::bigint AS total_attempts,
                       COALESCE(ga.passing_row_count, 0)::bigint AS passing_row_count
                FROM predmet p
                LEFT JOIN took_passed tp ON tp.predmet_id = p.id
                LEFT JOIN grade_agg ga ON ga.predmet_id = p.id
                LEFT JOIN median_passing mp ON mp.predmet_id = p.id
                LEFT JOIN attempts a ON a.predmet_id = p.id
                WHERE p.studijski_program_id = :programId
                ORDER BY p.sifra
                """.formatted(filteredOcenaCte, PASSING_GRADE, PASSING_GRADE);

        List<SubjectStatisticsRow> subjects = namedJdbc.query(mainSql, baseParams, (rs, rowNum) -> {
            long pid = rs.getLong("predmet_id");
            CurriculumSlot slot = curriculumByPredmetId.getOrDefault(pid, new CurriculumSlot(1, 1));
            long took = rs.getLong("took");
            long passed = rs.getLong("passed");
            long failed = Math.max(0, took - passed);
            Double passRate = took == 0 ? null : round2(100.0 * passed / took);
            java.math.BigDecimal avgBd = rs.getBigDecimal("avg_passing");
            Double avgPassing = avgBd == null ? null : round2(avgBd.doubleValue());
            double medianRaw = rs.getDouble("median_val");
            Double median = rs.wasNull() ? null : round2(medianRaw);
            int hg = rs.getInt("highest_grade");
            if (rs.wasNull()) {
                hg = 0;
            }
            Integer highest = hg >= PASSING_GRADE ? hg : null;
            int lg = rs.getInt("lowest_passing");
            if (rs.wasNull()) {
                lg = 0;
            }
            Integer lowestPassing = lg >= PASSING_GRADE ? lg : null;
            long attempts = rs.getLong("total_attempts");

            return new SubjectStatisticsRow(
                    pid,
                    rs.getString("sifra"),
                    rs.getString("naziv"),
                    rs.getInt("espb"),
                    slot.godinaStudija(),
                    slot.semestar(),
                    took,
                    passed,
                    failed,
                    passRate,
                    avgPassing,
                    median,
                    highest,
                    lowestPassing,
                    attempts,
                    List.of()
            );
        });

        Map<Long, List<GenerationBreakdownRow>> breakdownMap = params.includeGenerationBreakdown()
                ? fetchGenerationBreakdown(programId, params, filteredOcenaCte, baseParams)
                : Map.of();

        subjects = subjects.stream()
                .map(s -> new SubjectStatisticsRow(
                        s.subjectId(),
                        s.subjectCode(),
                        s.subjectName(),
                        s.espb(),
                        s.kurikulumGodina(),
                        s.semestar(),
                        s.totalStudentsWhoTook(),
                        s.totalStudentsWhoPassed(),
                        s.totalStudentsWhoFailed(),
                        s.passRate(),
                        s.averagePassingGrade(),
                        s.medianPassingGrade(),
                        s.highestGrade(),
                        s.lowestPassingGrade(),
                        s.totalExamAttempts(),
                        breakdownMap.getOrDefault(s.subjectId(), List.of())
                ))
                .toList();

        subjects = applyCurriculumAndSubjectFilters(subjects, params);

        RankingsBundle rankings = buildRankings(subjects);
        ProgramSummary summary = new ProgramSummary(prog.getId(), prog.getSifra(), prog.getNaziv());
        String note = buildAggregationNote(params);
        return new ProgramStatisticsResponse(summary, subjects, rankings, note);
    }

    private Map<Long, List<GenerationBreakdownRow>> fetchGenerationBreakdown(
            long programId,
            StatisticsQueryParams params,
            String filteredOcenaCte,
            MapSqlParameterSource baseParams
    ) {
        String sql = """
                %s
                , per_student AS (
                    SELECT predmet_id, student_id, godina_upisa,
                           MAX(vrednost_ocene) AS best_grade
                    FROM filtered_ocene
                    GROUP BY predmet_id, student_id, godina_upisa
                ),
                gb AS (
                    SELECT predmet_id, godina_upisa,
                           COUNT(*)::bigint AS took,
                           SUM(CASE WHEN best_grade >= %d THEN 1 ELSE 0 END)::bigint AS passed
                    FROM per_student
                    GROUP BY predmet_id, godina_upisa
                ),
                pass_avg AS (
                    SELECT fo.predmet_id, fo.godina_upisa,
                           AVG(fo.vrednost_ocene::numeric) AS avg_pass
                    FROM filtered_ocene fo
                    WHERE fo.vrednost_ocene >= %d
                    GROUP BY fo.predmet_id, fo.godina_upisa
                )
                SELECT gb.predmet_id, gb.godina_upisa, gb.took, gb.passed,
                       pa.avg_pass
                FROM gb
                LEFT JOIN pass_avg pa ON pa.predmet_id = gb.predmet_id AND pa.godina_upisa = gb.godina_upisa
                ORDER BY gb.predmet_id, gb.godina_upisa
                """.formatted(filteredOcenaCte, PASSING_GRADE, PASSING_GRADE);

        Map<Long, List<GenerationBreakdownRow>> out = new HashMap<>();
        namedJdbc.query(sql, baseParams, rs -> {
            long pid = rs.getLong("predmet_id");
            int gy = rs.getInt("godina_upisa");
            long took = rs.getLong("took");
            long passed = rs.getLong("passed");
            long failed = Math.max(0, took - passed);
            Double pr = took == 0 ? null : round2(100.0 * passed / took);
            java.math.BigDecimal ap = rs.getBigDecimal("avg_pass");
            Double avgPass = ap == null ? null : round2(ap.doubleValue());
            out.computeIfAbsent(pid, k -> new ArrayList<>())
                    .add(new GenerationBreakdownRow(gy, took, passed, failed, pr, avgPass));
        });
        return out;
    }

    private static List<SubjectStatisticsRow> applyCurriculumAndSubjectFilters(
            List<SubjectStatisticsRow> rows,
            StatisticsQueryParams params
    ) {
        return rows.stream()
                .filter(s -> params.predmetId() == null || s.subjectId() == params.predmetId())
                .filter(s -> params.kurikulumGodina() == null || s.kurikulumGodina() == params.kurikulumGodina())
                .filter(s -> params.semestar() == null || s.semestar() == params.semestar())
                .toList();
    }

    /**
     * CTE {@code filtered_ocene}: svi relevantni izlasci sa metapodacima studenta i roka.
     * Predmeti su uvek ograničeni na {@code studijski_program_id = :programId}.
     */
    private Map<Long, LinearAcademicTimeline.PredmetKurikulumSlot> curriculumSlotsByPredmetId(List<Predmet> predmetiOrdered) {
        Map<Long, LinearAcademicTimeline.PredmetKurikulumSlot> m = new LinkedHashMap<>();
        for (Predmet p : predmetiOrdered) {
            m.put(p.getId(), new LinearAcademicTimeline.PredmetKurikulumSlot(
                    p.getKurikulumGodina(), p.getKurikulumSemestar()));
        }
        return m;
    }

    private String buildFilteredOcenaCte(
            StatisticsQueryParams params,
            MapSqlParameterSource baseParams,
            Set<Long> linearEligiblePredmetIdsOrNull
    ) {
        StringBuilder where = new StringBuilder("""
                SELECT o.id,
                       o.student_id,
                       o.vrednost_ocene,
                       it.predmet_id,
                       s.godina_upisa,
                       ir.skolska_godina
                FROM ocena o
                JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                JOIN predmet p ON it.predmet_id = p.id
                JOIN student s ON o.student_id = s.id
                JOIN ispitni_rok ir ON it.ispitni_rok_id = ir.id
                WHERE p.studijski_program_id = :programId
                  AND (s.godina_upisa <= 2021
                       OR nais_ocena_je_u_redu(s.godina_upisa, it.datum_vreme, p.kurikulum_godina, p.kurikulum_semestar))
                """);
        if (params.godinaUpisa() != null) {
            if (params.godinaUpisa() == 2021) {
                where.append(" AND s.godina_upisa <= 2021 ");
            } else {
                where.append(" AND s.godina_upisa = :filterGodinaUpisa ");
                baseParams.addValue("filterGodinaUpisa", params.godinaUpisa());
            }
        }
        if (params.skolskaGodina() != null && !params.skolskaGodina().isBlank()) {
            where.append(" AND ir.skolska_godina = :skolskaGodina ");
            baseParams.addValue("skolskaGodina", params.skolskaGodina().trim());
        }
        if (params.predmetId() != null) {
            where.append(" AND p.id = :filterPredmetId ");
            baseParams.addValue("filterPredmetId", params.predmetId());
        }
        if (linearEligiblePredmetIdsOrNull != null && !linearEligiblePredmetIdsOrNull.isEmpty()) {
            where.append(" AND p.id IN (:linearEligiblePredmetIds) ");
            baseParams.addValue("linearEligiblePredmetIds", linearEligiblePredmetIdsOrNull);
        }
        return "WITH filtered_ocene AS (" + where + ")";
    }

    private static Map<Long, CurriculumSlot> buildCurriculumMap(List<Predmet> predmetiOrdered) {
        Map<Long, CurriculumSlot> m = new HashMap<>();
        for (Predmet p : predmetiOrdered) {
            m.put(p.getId(), new CurriculumSlot(p.getKurikulumGodina(), p.getKurikulumSemestar()));
        }
        return m;
    }

    private record CurriculumSlot(int godinaStudija, int semestar) {
    }

    private static RankingsBundle buildRankings(List<SubjectStatisticsRow> subjects) {
        Comparator<SubjectStatisticsRow> byPassRateAsc = Comparator
                .comparing((SubjectStatisticsRow s) -> s.passRate() == null ? Double.NaN : s.passRate());
        Comparator<SubjectStatisticsRow> byPassRateDesc = byPassRateAsc.reversed();

        List<SubjectStatisticsRow> withTook = subjects.stream()
                .filter(s -> s.totalStudentsWhoTook() > 0 && s.passRate() != null)
                .toList();

        List<SubjectStatisticsRow> hardest = withTook.stream()
                .sorted(byPassRateAsc.thenComparingLong(SubjectStatisticsRow::totalStudentsWhoTook))
                .limit(10)
                .toList();

        List<SubjectStatisticsRow> easiest = withTook.stream()
                .sorted(byPassRateDesc.thenComparingLong(s -> -s.totalStudentsWhoTook()))
                .limit(10)
                .toList();

        List<SubjectStatisticsRow> withAvg = subjects.stream()
                .filter(s -> s.averagePassingGrade() != null)
                .toList();

        List<SubjectStatisticsRow> hiAvg = withAvg.stream()
                .sorted(Comparator.comparingDouble(SubjectStatisticsRow::averagePassingGrade).reversed())
                .limit(10)
                .toList();

        List<SubjectStatisticsRow> loAvg = withAvg.stream()
                .sorted(Comparator.comparingDouble(SubjectStatisticsRow::averagePassingGrade))
                .limit(10)
                .toList();

        return new RankingsBundle(hardest, easiest, hiAvg, loAvg);
    }

    private String buildAggregationNote(StatisticsQueryParams params) {
        StringBuilder b = new StringBuilder();
        b.append("Agregacija prema zapisima u tabeli ocena. ");
        b.append("Položio = najbolja ocena studenta na predmetu >= 6. ");
        b.append("Prosek i medijana racunaju se samo od ocena >= 6 (svi takvi redovi). ");
        if (params.godinaUpisa() == null) {
            b.append("Uzorak: sve generacije (godine upisa) na programu. ");
        } else if (params.godinaUpisa() == 2021) {
            b.append("Uzorak: studenti sa godinom upisa <= 2021. ");
        } else {
            b.append("Uzorak: samo generacija upisa ").append(params.godinaUpisa()).append(". ");
        }
        if (params.skolskaGodina() != null && !params.skolskaGodina().isBlank()) {
            b.append("Filtrirano na školsku godinu ispitnog roka: ").append(params.skolskaGodina().trim()).append(". ");
        }
        if (linearAcademicTimeline.koristiStrogiLinearniModel(params.godinaUpisa())) {
            b.append("Predispitni uzorak je ograničen linearnim rasporedom (bez ponavljanja godine / produženog studiranja). ");
            b.append("Školska godina ispitnog roka se ne filtrira (svi relevantni rokovi). ");
            b.append("Godina kurikuluma i semestar: automatski iz generacije upisa (tekuća godina, I semestar). ");
        } else {
            if (params.kurikulumGodina() != null) {
                b.append("Filtar: godina kurikuluma ").append(params.kurikulumGodina()).append(". ");
            }
            if (params.semestar() != null) {
                b.append("Filtar: semestar ").append(params.semestar()).append(". ");
            }
        }
        return b.toString().trim();
    }

    @Transactional(readOnly = true)
    public StatisticsFilterOptions filterOptions(long programId, Integer godinaUpisa) {
        List<Integer> generacije = namedJdbc.queryForList("""
                SELECT DISTINCT s.godina_upisa AS gy
                FROM student s
                WHERE s.studijski_program_id = :pid
                  AND s.godina_upisa >= 2022
                  AND s.godina_upisa <= :refYear
                ORDER BY gy DESC
                """, new MapSqlParameterSource("pid", programId)
                .addValue("refYear", linearAcademicTimeline.referenceIntakeYear()), Integer.class);
        int ref = linearAcademicTimeline.referenceIntakeYear();
        if (generacije.isEmpty()) {
            generacije = new java.util.ArrayList<>();
            for (int y = ref; y >= 2022; y--) {
                generacije.add(y);
            }
        }

        List<String> godineFromDb = namedJdbc.queryForList("""
                SELECT DISTINCT ir.skolska_godina AS sg
                FROM ocena o
                JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                JOIN predmet p ON it.predmet_id = p.id
                JOIN ispitni_rok ir ON it.ispitni_rok_id = ir.id
                WHERE p.studijski_program_id = :pid
                ORDER BY sg DESC
                """, new MapSqlParameterSource("pid", programId), String.class);

        List<String> godine;
        if (linearAcademicTimeline.koristiStrogiLinearniModel(godinaUpisa)) {
            godine = List.of();
        } else {
            godine = godineFromDb;
        }

        List<Predmet> predmeti = predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId);
        Map<Long, CurriculumSlot> slots = buildCurriculumMap(predmeti);
        final int strictGodinaKur =
                linearAcademicTimeline.koristiStrogiLinearniModel(godinaUpisa)
                        ? linearAcademicTimeline.ocekivanaGodinaKurikulumaZaGeneraciju(godinaUpisa)
                        : 0;
        List<StatisticsFilterOptions.SubjectPickerOption> picker = predmeti.stream()
                .map(pr -> {
                    CurriculumSlot sl = slots.get(pr.getId());
                    return new StatisticsFilterOptions.SubjectPickerOption(
                            pr.getId(),
                            pr.getSifra(),
                            pr.getNaziv(),
                            sl.godinaStudija(),
                            sl.semestar()
                    );
                })
                .filter(opt -> strictGodinaKur == 0
                        || (opt.kurikulumGodina() == strictGodinaKur && opt.semestar() == 1))
                .toList();

        return new StatisticsFilterOptions(generacije, godine, picker);
    }

    private static Double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /**
     * Jedan predmet po ID-u (isti izračun kao lista, uz validaciju pripadnosti programu).
     */
    @Transactional(readOnly = true)
    public SubjectStatisticsRow subjectDetail(long programId, long subjectId, StatisticsQueryParams params) {
        StatisticsQueryParams withPredmet = new StatisticsQueryParams(
                params.godinaUpisa(),
                params.skolskaGodina(),
                params.kurikulumGodina(),
                params.semestar(),
                subjectId,
                params.includeGenerationBreakdown()
        );
        var resp = computeForProgram(programId, withPredmet);
        return resp.subjects().stream()
                .filter(s -> s.subjectId() == subjectId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Predmet nije na programu ili nema podataka."));
    }
}
