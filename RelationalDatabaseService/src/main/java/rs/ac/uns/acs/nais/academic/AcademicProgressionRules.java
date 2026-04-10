package rs.ac.uns.acs.nais.academic;

import rs.ac.uns.acs.nais.domain.Ocena;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.Student;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;

public final class AcademicProgressionRules {

    public static final int ZADNJA_GODINA_UPISA_BEZ_STROG_NAPREDOVANJA = 2021;

    private static final ZoneId ZONA = ZoneId.of("Europe/Belgrade");

    private AcademicProgressionRules() {
    }

    public static LocalDate datumIspita(Ocena o) {
        Instant instant = o.getIspitniTermin().getDatumVreme();
        return LocalDate.ofInstant(instant, ZONA);
    }

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

    public static int procenjenaGodinaStudijaAkademska(int godinaUpisa, LocalDate d) {
        int cy = d.getYear();
        int cm = d.getMonthValue();
        int pocetakTekuceSkolske = cm >= Month.OCTOBER.getValue() ? cy : cy - 1;
        int g = pocetakTekuceSkolske - godinaUpisa + 1;
        return Math.max(1, Math.min(6, g));
    }

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
