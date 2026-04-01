package rs.ac.uns.acs.nais.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatisticsAggregatesResponse(
        List<OverallStatRow> overall,
        List<MonthlyStatRow> monthly
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MonthlyStatRow(
            long predmetId,
            String mesec,
            long polozeno,
            long pali,
            double prosecnaOcena
    ) {
    }
}
