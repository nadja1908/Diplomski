package rs.ac.uns.acs.nais.internal.dto;

import java.util.List;

public record StatisticsAggregatesResponse(
        List<OverallStatRow> overall,
        List<MonthlyStatRow> monthly
) {
    public record OverallStatRow(
            long predmetId,
            String nazivPredmeta,
            long ukupnoPolaganja,
            long polozeno,
            long pali,
            long zbirOcena,
            long brojOcena
    ) {
    }

    public record MonthlyStatRow(
            long predmetId,
            String mesec,
            long polozeno,
            long pali,
            double prosecnaOcena
    ) {
    }
}
