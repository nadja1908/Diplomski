package rs.ac.uns.acs.nais.academic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsQueryParams;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class LinearAcademicTimeline {

    private final int referenceIntakeYear;

    public LinearAcademicTimeline(
            @Value("${nais.academic.reference-intake-year:2025}") int referenceIntakeYear
    ) {
        this.referenceIntakeYear = referenceIntakeYear;
    }

    public int referenceIntakeYear() {
        return referenceIntakeYear;
    }

    public int studyYearFromGodinaUpisa(int godinaUpisa) {
        int y = referenceIntakeYear - godinaUpisa + 1;
        return Math.max(1, Math.min(4, y));
    }

    public boolean predmetJeURasporedu(int godinaUpisaStudenta, int kurikulumGodina, int semestar) {
        int sy = studyYearFromGodinaUpisa(godinaUpisaStudenta);
        if (kurikulumGodina < sy) {
            return true;
        }
        if (kurikulumGodina > sy) {
            return false;
        }
        return semestar == 1;
    }

    public int ocekivanaGodinaKurikulumaZaGeneraciju(int godinaUpisa) {
        return studyYearFromGodinaUpisa(godinaUpisa);
    }

    public List<String> dozvoljeneSkolskeGodineZaGeneraciju(int godinaUpisa) {
        List<String> out = new ArrayList<>();
        int end = Math.min(referenceIntakeYear, godinaUpisa + 3);
        for (int y = godinaUpisa; y <= end; y++) {
            out.add(formatSkolskaGodinaLabel(y));
        }
        return out;
    }

    public String formatSkolskaGodinaLabel(int prvaGodinaSkolske) {
        int druga = (prvaGodinaSkolske + 1) % 100;
        return prvaGodinaSkolske + "/" + (druga < 10 ? "0" + druga : String.valueOf(druga));
    }

    public boolean skolskaGodinaJeDozvoljenaZaGeneraciju(String skolskaGodina, int godinaUpisa) {
        if (skolskaGodina == null || skolskaGodina.isBlank()) {
            return true;
        }
        Integer start = parseSkolskaGodinaStart(skolskaGodina.trim());
        if (start == null) {
            return false;
        }
        int end = Math.min(referenceIntakeYear, godinaUpisa + 3);
        return start >= godinaUpisa && start <= end;
    }

    private Integer parseSkolskaGodinaStart(String sg) {
        int slash = sg.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        try {
            return Integer.parseInt(sg.substring(0, slash).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean koristiStrogiLinearniModel(Integer godinaUpisa) {
        return godinaUpisa != null && godinaUpisa > 2021;
    }

    public StatisticsQueryParams normalizeStatisticsParams(StatisticsQueryParams params) {
        Integer gu = params.godinaUpisa();
        if (!koristiStrogiLinearniModel(gu)) {
            return params;
        }
        int expectedK = ocekivanaGodinaKurikulumaZaGeneraciju(gu);
        return new StatisticsQueryParams(
                gu,
                null,
                expectedK,
                1,
                params.predmetId(),
                params.includeGenerationBreakdown());
    }

    public java.util.Set<Long> predmetIdsDostupniZaGeneraciju(
            int godinaUpisa,
            java.util.Map<Long, PredmetKurikulumSlot> kurikulumPoPredmetId
    ) {
        Set<Long> ids = new LinkedHashSet<>();
        for (var e : kurikulumPoPredmetId.entrySet()) {
            PredmetKurikulumSlot sl = e.getValue();
            if (predmetJeURasporedu(godinaUpisa, sl.kurikulumGodina(), sl.semestar())) {
                ids.add(e.getKey());
            }
        }
        return ids;
    }

    public record PredmetKurikulumSlot(int kurikulumGodina, int semestar) {
    }
}
