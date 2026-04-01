package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.cassandra.SubjectMonthlyTrend;
import rs.ac.uns.acs.nais.cassandra.SubjectMonthlyTrendRepository;
import rs.ac.uns.acs.nais.cassandra.SubjectStatistics;
import rs.ac.uns.acs.nais.cassandra.SubjectStatisticsRepository;
import rs.ac.uns.acs.nais.client.RelationalInternalClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ColumnarStatsService {

    private final SubjectStatisticsRepository subjectStatisticsRepository;
    private final SubjectMonthlyTrendRepository subjectMonthlyTrendRepository;
    private final RelationalInternalClient relationalInternalClient;

    public List<SubjectStatisticsDto> studentCassandraStats(Long korisnikId) {
        List<Long> predmetIds = relationalInternalClient.studentPredmetIds(korisnikId);
        List<SubjectStatisticsDto> out = new ArrayList<>();
        for (Long pid : new LinkedHashSet<>(predmetIds)) {
            subjectStatisticsRepository.findById(pid).ifPresent(stat -> out.add(toDto(stat)));
        }
        out.sort(Comparator.comparing(SubjectStatisticsDto::nazivPredmeta));
        return out;
    }

    public List<SubjectStatisticsDto> headSubjectAnalytics(Long korisnikId) {
        Long katedraId = relationalInternalClient.katedraForSef(korisnikId);
        if (katedraId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niste šef katedre");
        }
        Set<Long> ids = new HashSet<>(relationalInternalClient.predmetIdsForKatedra(katedraId));
        List<SubjectStatisticsDto> out = new ArrayList<>();
        subjectStatisticsRepository.findAll().forEach(stat -> {
            if (ids.contains(stat.getPredmetId())) {
                out.add(toDto(stat));
            }
        });
        out.sort(Comparator.comparing(SubjectStatisticsDto::nazivPredmeta));
        return out;
    }

    public PassFailTrendDto headPassFailTrends(Long korisnikId) {
        Long katedraId = relationalInternalClient.katedraForSef(korisnikId);
        if (katedraId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niste šef katedre");
        }
        Set<Long> ids = new HashSet<>(relationalInternalClient.predmetIdsForKatedra(katedraId));
        Map<String, MonthAgg> byMonth = new TreeMap<>();
        for (Long pid : ids) {
            for (SubjectMonthlyTrend t : subjectMonthlyTrendRepository.findByKeyPredmetId(pid)) {
                String m = t.getKey().getMesec();
                MonthAgg agg = byMonth.computeIfAbsent(m, x -> new MonthAgg());
                agg.polozeno += t.getPolozeno();
                agg.pali += t.getPali();
            }
        }
        List<PassFailTrendDto.MonthPoint> points = byMonth.entrySet().stream()
                .map(e -> new PassFailTrendDto.MonthPoint(e.getKey(), e.getValue().polozeno, e.getValue().pali))
                .toList();
        return new PassFailTrendDto(points);
    }

    public PerformanceOverviewDto headPerformanceOverview(Long korisnikId) {
        List<SubjectStatisticsDto> stats = headSubjectAnalytics(korisnikId);
        Optional<SubjectStatisticsDto> easiest = stats.stream()
                .filter(s -> s.brojOcena() > 0 && s.prosecnaOcena() != null)
                .max(Comparator.comparingDouble(SubjectStatisticsDto::prosecnaOcena));
        Optional<SubjectStatisticsDto> hardest = stats.stream()
                .filter(s -> s.brojOcena() > 0 && s.prosecnaOcena() != null)
                .min(Comparator.comparingDouble(SubjectStatisticsDto::prosecnaOcena));
        long totalPass = stats.stream().mapToLong(SubjectStatisticsDto::polozeno).sum();
        long totalFail = stats.stream().mapToLong(SubjectStatisticsDto::pali).sum();
        return new PerformanceOverviewDto(
                totalPass,
                totalFail,
                easiest.orElse(null),
                hardest.orElse(null),
                stats.size()
        );
    }

    public List<SubjectStatisticsDto> globalRankings() {
        List<SubjectStatisticsDto> all = new ArrayList<>();
        subjectStatisticsRepository.findAll().forEach(s -> all.add(toDto(s)));
        return all.stream()
                .sorted(Comparator.comparingDouble((SubjectStatisticsDto s) ->
                        s.prosecnaOcena() != null ? s.prosecnaOcena() : 0).reversed())
                .toList();
    }

    private SubjectStatisticsDto toDto(SubjectStatistics s) {
        long n = s.getBrojOcena() == null ? 0 : s.getBrojOcena();
        long z = s.getZbirOcena() == null ? 0 : s.getZbirOcena();
        Double avg = n == 0 ? null : Math.round((z * 100.0 / n)) / 100.0;
        return new SubjectStatisticsDto(
                s.getPredmetId(),
                s.getNazivPredmeta(),
                s.getUkupnoPolaganja(),
                s.getPolozeno(),
                s.getPali(),
                avg,
                n
        );
    }

    private static class MonthAgg {
        int polozeno;
        int pali;
    }

    public record SubjectStatisticsDto(
            Long predmetId,
            String nazivPredmeta,
            long ukupnoPolaganja,
            long polozeno,
            long pali,
            Double prosecnaOcena,
            long brojOcena
    ) {
    }

    public record PassFailTrendDto(List<MonthPoint> meseci) {
        public record MonthPoint(String mesec, int polozeno, int pali) {
        }
    }

    public record PerformanceOverviewDto(
            long ukupnoPolozeno,
            long ukupnoPali,
            SubjectStatisticsDto najlaksiPredmet,
            SubjectStatisticsDto najteziPredmet,
            int brojPredmetaUKatedri
    ) {
    }
}
