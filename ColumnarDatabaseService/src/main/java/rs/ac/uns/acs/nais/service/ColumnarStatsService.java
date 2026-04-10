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
import rs.ac.uns.acs.nais.internal.dto.StudentProgramPredmetMin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ColumnarStatsService {

    private static final Pattern TREND_ROK_KEY = Pattern.compile("^(\\d{4})-R(\\d{2})$");
    private static final Pattern TREND_CAL_KEY = Pattern.compile("(\\d{4})-(\\d{2})");

    private final SubjectStatisticsRepository subjectStatisticsRepository;
    private final SubjectMonthlyTrendRepository subjectMonthlyTrendRepository;
    private final RelationalInternalClient relationalInternalClient;

    public List<SubjectStatisticsDto> studentCassandraStats(Long korisnikId) {
        List<StudentProgramPredmetMin> predmeti = relationalInternalClient.studentProgramPredmeti(korisnikId);
        if (predmeti == null || predmeti.isEmpty()) {
            return List.of();
        }
        List<SubjectStatisticsDto> out = new ArrayList<>();
        for (StudentProgramPredmetMin pm : predmeti) {
            Optional<SubjectStatistics> opt = subjectStatisticsRepository.findById(pm.id());
            if (opt.isPresent()) {
                SubjectStatisticsDto base = toDto(opt.get());
                out.add(new SubjectStatisticsDto(
                        base.predmetId(),
                        base.nazivPredmeta(),
                        base.ukupnoPolaganja(),
                        base.polozeno(),
                        base.pali(),
                        base.prosecnaOcena(),
                        base.brojOcena(),
                        pm.sifra()));
            } else {
                out.add(new SubjectStatisticsDto(
                        pm.id(),
                        pm.naziv(),
                        0L,
                        0L,
                        0L,
                        null,
                        0L,
                        pm.sifra()));
            }
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

    public PassFailTrendDto headPassFailTrends(Long korisnikId, Integer godina) {
        Long katedraId = relationalInternalClient.katedraForSef(korisnikId);
        if (katedraId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niste šef katedre");
        }
        Set<Long> ids = new HashSet<>(relationalInternalClient.predmetIdsForKatedra(katedraId));
        Map<String, MonthAgg> byMonth = new TreeMap<>();
        TreeSet<Integer> years = new TreeSet<>(Comparator.reverseOrder());
        for (Long pid : ids) {
            for (SubjectMonthlyTrend t : subjectMonthlyTrendRepository.findByKeyPredmetId(pid)) {
                String canonical = normalizeTrendPeriodKey(t.getKey().getMesec());
                rokGodinaIzKljuča(canonical).ifPresent(years::add);
                MonthAgg agg = byMonth.computeIfAbsent(canonical, x -> new MonthAgg());
                agg.polozeno += t.getPolozeno();
                agg.pali += t.getPali();
            }
        }
        List<Integer> dostupneGodine = new ArrayList<>(years);
        if (years.isEmpty()) {
            return new PassFailTrendDto(List.of(), dostupneGodine, null);
        }
        int izabranaGodina = godina != null ? godina : years.first();
        List<PassFailTrendDto.MonthPoint> points = new ArrayList<>();
        for (int rokIdx = 1; rokIdx <= 7; rokIdx++) {
            String k = String.format("%d-R%02d", izabranaGodina, rokIdx);
            MonthAgg a = byMonth.get(k);
            int p = a == null ? 0 : a.polozeno;
            int f = a == null ? 0 : a.pali;
            points.add(new PassFailTrendDto.MonthPoint(k, p, f));
        }
        return new PassFailTrendDto(points, dostupneGodine, izabranaGodina);
    }

    private static OptionalInt rokGodinaIzKljuča(String canonicalKey) {
        Matcher m = TREND_ROK_KEY.matcher(canonicalKey);
        if (!m.matches()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(m.group(1)));
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
                n,
                null
        );
    }

    static String normalizeTrendPeriodKey(String mesec) {
        if (mesec == null || mesec.isBlank()) {
            return mesec;
        }
        if (TREND_ROK_KEY.matcher(mesec).matches()) {
            return mesec;
        }
        Matcher cal = TREND_CAL_KEY.matcher(mesec);
        if (cal.matches()) {
            int y = Integer.parseInt(cal.group(1));
            int month = Integer.parseInt(cal.group(2));
            int[] yr = calendarMonthToRokIndex(y, month);
            return String.format("%d-R%02d", yr[0], yr[1]);
        }
        return mesec;
    }

    private static int[] calendarMonthToRokIndex(int year, int month) {
        return switch (month) {
            case 12 -> new int[] { year + 1, 1 };
            case 1 -> new int[] { year, 1 };
            case 2 -> new int[] { year, 2 };
            case 3, 4 -> new int[] { year, 3 };
            case 5, 6 -> new int[] { year, 4 };
            case 7 -> new int[] { year, 5 };
            case 8 -> new int[] { year, 6 };
            case 9, 10, 11 -> new int[] { year, 7 };
            default -> new int[] { year, 1 };
        };
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
            long brojOcena,
            String sifra
    ) {
    }

    public record PassFailTrendDto(
            List<MonthPoint> meseci,
            List<Integer> dostupneGodine,
            Integer izabranaGodina
    ) {
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
