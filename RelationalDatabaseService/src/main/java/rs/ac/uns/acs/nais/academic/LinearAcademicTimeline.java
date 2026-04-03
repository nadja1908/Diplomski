package rs.ac.uns.acs.nais.academic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsQueryParams;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Striktan linearni model studija: nema ponavljanja godine, nema produženog studiranja.
 * Godina upisa {@code U} i referentna kalendarska godina upisa novog kruga {@code R} određuju
 * tekuću godinu studija {@code Y = R - U + 1} (ograničeno na [1, 4]).
 *
 * <p>Dostupnost predmeta: sve iz završenih godina kurikuluma ({@code ks &lt; Y}, oba semestra)
 * i samo I semestar tekuće godine ({@code ks == Y} i {@code sem == 1}).</p>
 */
@Component
public class LinearAcademicTimeline {

    /** Kalendarska godina u kojoj je generacija {@code R} u prvoj godini studija (npr. 2025 → “prvaci” = 1. godina). */
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

    /**
     * Da li je predmet na mestu ({@code kurikulumGodina}, {@code semestar}) u rasporedu za studenta sa godinom upisa {@code godinaUpisa}.
     */
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

    /** Jedina „tekuća“ godina kurikuluma za filtriranje po generaciji (bez višestrukog izbora). */
    public int ocekivanaGodinaKurikulumaZaGeneraciju(int godinaUpisa) {
        return studyYearFromGodinaUpisa(godinaUpisa);
    }

    /**
     * Školske godine ispitnog roka koje smeju da se pojave uz datu generaciju upisa
     * (od godine upisa do referentne godine, uključivo).
     */
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

    /** Legacy uzorak „2021 i ranije“ — bez stroge linearne validacije kršenja godina. */
    public boolean koristiStrogiLinearniModel(Integer godinaUpisa) {
        return godinaUpisa != null && godinaUpisa > 2021;
    }

    /**
     * Za generaciju upisa {@code > 2021}: fiksira tekuću godinu kurikuluma i I semestar; uklanja filter
     * „školska godina ispitnog roka” (agregacija po svim rokovima u okviru linearnog uzorka predmeta).
     */
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

    /**
     * Predmeti čiji se ispiti smeju brojati za uzorak generacije {@code godinaUpisa} u linearnom modelu
     * (završene godine — oba semestra; tekuća godina — samo I semestar).
     */
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
