package rs.ac.uns.acs.nais.academic;

import rs.ac.uns.acs.nais.domain.Ocena;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.Student;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;

/**
 * Napredovanje po akademskoj godini (oktobar–septembar) i semestrima u okviru godine studija.
 * Ishod_ispita mora da padne u trenutku kad je student završio dovoljno semestara da bi smeo taj predmet
 * ({@code kurikulum_godina} / {@code kurikulum_semestar} u nastavnom planu).
 */
public final class AcademicProgressionRules {

    /**
     * Generacije iz originalnog {@code 02_data.sql} (upis do 2021): datumi ispita nisu modelovani strogo po semestrima.
     * Za njih ne primenjujemo filtriranje po napredovanju u prikazu i agregatima.
     */
    public static final int ZADNJA_GODINA_UPISA_BEZ_STROG_NAPREDOVANJA = 2021;

    private static final ZoneId ZONA = ZoneId.of("Europe/Belgrade");

    private AcademicProgressionRules() {
    }

    public static LocalDate datumIspita(Ocena o) {
        Instant instant = o.getIspitniTermin().getDatumVreme();
        return LocalDate.ofInstant(instant, ZONA);
    }

    /**
     * Da li evidencija ocene sme postojati: na datum ispita student je morao imati pravo na taj predmet.
     */
    public static boolean ocenaJeDozvoljenaEvidencija(Student student, Ocena ocena) {
        if (student.getGodinaUpisa() <= ZADNJA_GODINA_UPISA_BEZ_STROG_NAPREDOVANJA) {
            return true;
        }
        Predmet p = ocena.getIspitniTermin().getPredmet();
        return studentMozePolagatiPredmetNaDatum(
                student.getGodinaUpisa(),
                datumIspita(ocena),
                p.getKurikulumGodina(),
                p.getKurikulumSemestar());
    }

    public static boolean studentMozePolagatiPredmetNaDatum(
            int godinaUpisa,
            LocalDate datumIspita,
            int kurikulumGodina,
            int kurikulumSemestar
    ) {
        int potrebnoSemestara = (kurikulumGodina - 1) * 2 + kurikulumSemestar;
        return ukupnoZavrsenihSemestara(godinaUpisa, datumIspita) >= potrebnoSemestara;
    }

    /**
     * Godina studija 1..6; školska godina počinje oktobrom (npr. upis 2025 → 1. godina do septembra 2026).
     */
    public static int procenjenaGodinaStudijaAkademska(int godinaUpisa, LocalDate d) {
        int cy = d.getYear();
        int cm = d.getMonthValue();
        int pocetakTekuceSkolske = cm >= Month.OCTOBER.getValue() ? cy : cy - 1;
        int g = pocetakTekuceSkolske - godinaUpisa + 1;
        return Math.max(1, Math.min(6, g));
    }

    /**
     * Završeni semestri u tekućoj godini studija: okt–jan = 0, feb–jun = 1, jul–sep = 2.
     */
    public static int zavrseniSemestriUTekucojGodiniStudija(Month month) {
        int m = month.getValue();
        if (m >= 10 || m == 1) {
            return 0;
        }
        if (m >= 2 && m <= 6) {
            return 1;
        }
        return 2;
    }

    public static int ukupnoZavrsenihSemestara(int godinaUpisa, LocalDate d) {
        int gs = procenjenaGodinaStudijaAkademska(godinaUpisa, d);
        int uGodini = zavrseniSemestriUTekucojGodiniStudija(d.getMonth());
        return 2 * (gs - 1) + uGodini;
    }
}
