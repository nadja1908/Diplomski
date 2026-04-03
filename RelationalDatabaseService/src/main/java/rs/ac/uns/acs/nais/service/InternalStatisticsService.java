package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse.MonthlyStatRow;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse.OverallStatRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InternalStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Agregacija za Cassandru: za <strong>svaki predmet u katalogu</strong>, životni zbirovi (
     * <strong>sve godine, svi ispitni termini</strong> u bazi) — bez ograničenja po akademskoj godini.
     */
    public StatisticsAggregatesResponse computeAggregates() {
        List<Map<String, Object>> overallRows = jdbcTemplate.queryForList("""
                SELECT p.id AS predmet_id,
                       p.naziv AS naziv_predmeta,
                       COALESCE(s.ukupno_polaganja, 0) AS ukupno_polaganja,
                       COALESCE(s.polozeno, 0) AS polozeno,
                       COALESCE(s.pali, 0) AS pali,
                       COALESCE(s.zbir_ocena, 0) AS zbir_ocena,
                       COALESCE(s.broj_ocena, 0) AS broj_ocena
                FROM predmet p
                LEFT JOIN (
                    SELECT it.predmet_id AS pid,
                           COUNT(o.id) AS ukupno_polaganja,
                           COALESCE(SUM(CASE WHEN o.vrednost_ocene >= 6 THEN 1 ELSE 0 END), 0) AS polozeno,
                           COALESCE(SUM(CASE WHEN o.vrednost_ocene < 6 THEN 1 ELSE 0 END), 0) AS pali,
                           COALESCE(SUM(o.vrednost_ocene), 0) AS zbir_ocena,
                           COUNT(o.id) AS broj_ocena
                    FROM ocena o
                    JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                    JOIN student st ON o.student_id = st.id
                    JOIN predmet pr ON it.predmet_id = pr.id
                    WHERE (st.godina_upisa <= 2021
                           OR nais_ocena_je_u_redu(st.godina_upisa, it.datum_vreme, pr.kurikulum_godina, pr.kurikulum_semestar))
                    GROUP BY it.predmet_id
                ) s ON s.pid = p.id
                ORDER BY p.id
                """);

        List<OverallStatRow> overall = new ArrayList<>();
        for (Map<String, Object> row : overallRows) {
            overall.add(new OverallStatRow(
                    ((Number) row.get("predmet_id")).longValue(),
                    (String) row.get("naziv_predmeta"),
                    ((Number) row.get("ukupno_polaganja")).longValue(),
                    ((Number) row.get("polozeno")).longValue(),
                    ((Number) row.get("pali")).longValue(),
                    ((Number) row.get("zbir_ocena")).longValue(),
                    ((Number) row.get("broj_ocena")).longValue()
            ));
        }

        /*
         * Tačke po ispitnom roku (ne po kalendarskom mesecu): ključ mesec = YYYY-R01..R06
         * (R01 Januarski … R06 Oktobarski). Meseci van „čistih“ rokova mapiraju se na najbliži rok
         * (npr. mart → Aprilski, jul → Junski, decembar → Januarski sledeće godine).
         */
        List<Map<String, Object>> monthlyRows = jdbcTemplate.queryForList("""
                SELECT p.id AS predmet_id,
                       (r.rok_godina::text || '-R' || LPAD(r.rok_indeks::text, 2, '0')) AS mesec,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene >= 6 THEN 1 ELSE 0 END), 0) AS polozeno,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene < 6 THEN 1 ELSE 0 END), 0) AS pali,
                       CASE WHEN COUNT(o.id) = 0 THEN 0
                            ELSE ROUND(AVG(o.vrednost_ocene)::numeric, 2)::double precision END AS prosecna_ocena
                FROM ocena o
                JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                JOIN predmet p ON it.predmet_id = p.id
                JOIN student st ON o.student_id = st.id
                CROSS JOIN LATERAL (
                    SELECT
                        CASE WHEN EXTRACT(MONTH FROM (it.datum_vreme AT TIME ZONE 'UTC'))::int = 12
                             THEN EXTRACT(YEAR FROM (it.datum_vreme AT TIME ZONE 'UTC'))::int + 1
                             ELSE EXTRACT(YEAR FROM (it.datum_vreme AT TIME ZONE 'UTC'))::int
                        END AS rok_godina,
                        CASE EXTRACT(MONTH FROM (it.datum_vreme AT TIME ZONE 'UTC'))::int
                            WHEN 1 THEN 1
                            WHEN 2 THEN 2
                            WHEN 3 THEN 3
                            WHEN 4 THEN 3
                            WHEN 5 THEN 4
                            WHEN 6 THEN 4
                            WHEN 7 THEN 4
                            WHEN 8 THEN 5
                            WHEN 9 THEN 6
                            WHEN 10 THEN 6
                            WHEN 11 THEN 6
                            WHEN 12 THEN 1
                            ELSE 1
                        END AS rok_indeks
                ) r
                WHERE (st.godina_upisa <= 2021
                       OR nais_ocena_je_u_redu(st.godina_upisa, it.datum_vreme, p.kurikulum_godina, p.kurikulum_semestar))
                GROUP BY p.id, r.rok_godina, r.rok_indeks
                """);

        List<MonthlyStatRow> monthly = new ArrayList<>();
        for (Map<String, Object> row : monthlyRows) {
            monthly.add(new MonthlyStatRow(
                    ((Number) row.get("predmet_id")).longValue(),
                    (String) row.get("mesec"),
                    ((Number) row.get("polozeno")).longValue(),
                    ((Number) row.get("pali")).longValue(),
                    ((Number) row.get("prosecna_ocena")).doubleValue()
            ));
        }

        return new StatisticsAggregatesResponse(overall, monthly);
    }
}
