package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.cassandra.SubjectMonthlyTrend;
import rs.ac.uns.acs.nais.cassandra.SubjectMonthlyTrendKey;
import rs.ac.uns.acs.nais.cassandra.SubjectMonthlyTrendRepository;
import rs.ac.uns.acs.nais.cassandra.SubjectStatistics;
import rs.ac.uns.acs.nais.cassandra.SubjectStatisticsRepository;
import rs.ac.uns.acs.nais.client.RelationalInternalClient;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse.MonthlyStatRow;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse.OverallStatRow;

@Service
@RequiredArgsConstructor
public class StatisticsSyncService {

    private final RelationalInternalClient relationalInternalClient;
    private final SubjectStatisticsRepository subjectStatisticsRepository;
    private final SubjectMonthlyTrendRepository subjectMonthlyTrendRepository;

    public void rebuildFromRelationalService() {
        StatisticsAggregatesResponse data = relationalInternalClient.fetchAggregates();
        if (data == null || data.overall() == null) {
            return;
        }
        subjectStatisticsRepository.deleteAll();
        subjectMonthlyTrendRepository.deleteAll();

        for (OverallStatRow row : data.overall()) {
            SubjectStatistics s = SubjectStatistics.builder()
                    .predmetId(row.predmetId())
                    .nazivPredmeta(row.nazivPredmeta())
                    .ukupnoPolaganja(row.ukupnoPolaganja())
                    .polozeno(row.polozeno())
                    .pali(row.pali())
                    .zbirOcena(row.zbirOcena())
                    .brojOcena(row.brojOcena())
                    .build();
            subjectStatisticsRepository.save(s);
        }

        if (data.monthly() != null) {
            for (MonthlyStatRow row : data.monthly()) {
                SubjectMonthlyTrendKey key = new SubjectMonthlyTrendKey(row.predmetId(), row.mesec());
                SubjectMonthlyTrend t = SubjectMonthlyTrend.builder()
                        .key(key)
                        .polozeno((int) row.polozeno())
                        .pali((int) row.pali())
                        .prosecnaOcena(row.prosecnaOcena())
                        .build();
                subjectMonthlyTrendRepository.save(t);
            }
        }
    }
}
