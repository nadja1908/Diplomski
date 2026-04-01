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

    public StatisticsAggregatesResponse computeAggregates() {
        List<Map<String, Object>> overallRows = jdbcTemplate.queryForList("""
                SELECT p.id AS predmet_id,
                       p.naziv AS naziv_predmeta,
                       COUNT(o.id) AS ukupno_polaganja,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene >= 6 THEN 1 ELSE 0 END), 0) AS polozeno,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene < 6 THEN 1 ELSE 0 END), 0) AS pali,
                       COALESCE(SUM(o.vrednost_ocene), 0) AS zbir_ocena,
                       COUNT(o.id) AS broj_ocena
                FROM ocena o
                JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                JOIN predmet p ON it.predmet_id = p.id
                GROUP BY p.id, p.naziv
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

        List<Map<String, Object>> monthlyRows = jdbcTemplate.queryForList("""
                SELECT p.id AS predmet_id,
                       TO_CHAR(it.datum_vreme AT TIME ZONE 'UTC', 'YYYY-MM') AS mesec,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene >= 6 THEN 1 ELSE 0 END), 0) AS polozeno,
                       COALESCE(SUM(CASE WHEN o.vrednost_ocene < 6 THEN 1 ELSE 0 END), 0) AS pali,
                       CASE WHEN COUNT(o.id) = 0 THEN 0
                            ELSE ROUND(AVG(o.vrednost_ocene)::numeric, 2)::double precision END AS prosecna_ocena
                FROM ocena o
                JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
                JOIN predmet p ON it.predmet_id = p.id
                GROUP BY p.id, TO_CHAR(it.datum_vreme AT TIME ZONE 'UTC', 'YYYY-MM')
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
