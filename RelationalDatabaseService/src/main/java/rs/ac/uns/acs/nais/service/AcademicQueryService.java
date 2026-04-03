package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.academic.AcademicProgressionRules;
import rs.ac.uns.acs.nais.academic.LinearAcademicTimeline;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.domain.Korisnik;
import rs.ac.uns.acs.nais.domain.KorisnikUloga;
import rs.ac.uns.acs.nais.domain.Ocena;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.Student;
import rs.ac.uns.acs.nais.domain.StudijskiProgram;
import rs.ac.uns.acs.nais.repository.KorisnikRepository;
import rs.ac.uns.acs.nais.repository.OcenaRepository;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.SefKatedreRepository;
import rs.ac.uns.acs.nais.repository.StudijskiProgramRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AcademicQueryService {

    private final KorisnikRepository korisnikRepository;
    private final StudentRepository studentRepository;
    private final SefKatedreRepository sefKatedreRepository;
    private final StudijskiProgramRepository studijskiProgramRepository;
    private final OcenaRepository ocenaRepository;
    private final PredmetRepository predmetRepository;
    private final LinearAcademicTimeline linearAcademicTimeline;

    /**
     * Autorizacija po bazi (ne {@code @PreAuthorize} na kontroleru), da radi pouzdano sa JWT principalom tipa {@link Long}.
     */
    private void assertSefKatedre(Long korisnikId) {
        if (korisnikId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niste prijavljeni");
        }
        Korisnik k = korisnikRepository.findById(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Korisnik nije pronađen"));
        if (k.getUloga() != KorisnikUloga.SEF_KATEDRE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Potrebna je uloga šefa katedre");
        }
    }

    @Transactional(readOnly = true)
    public StudentProfileDto studentProfile(Long korisnikId) {
        Objects.requireNonNull(korisnikId, "korisnikId");
        Korisnik korisnik = korisnikRepository.findById(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Korisnik nije pronađen"));
        Student s = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Student nije pronađen"));
        var p = s.getStudijskiProgram();
        return new StudentProfileDto(
                korisnik.getIme(),
                korisnik.getPrezime(),
                korisnik.getEmail(),
                brojIndeksaSaSifromPrograma(s.getBrojIndeksa(), p.getSifra()),
                p.getNaziv(),
                p.getSifra(),
                p.getKatedra().getNaziv(),
                s.getGodinaUpisa()
        );
    }

    @Transactional(readOnly = true)
    public List<SubjectGradeDto> subjectsAndGrades(Long korisnikId) {
        Student s = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Student nije pronađen"));
        return ocenaRepository.findByStudentIdWithDetails(s.getId()).stream()
                .filter(o -> AcademicProgressionRules.ocenaJeDozvoljenaEvidencija(s, o))
                .map(this::toSubjectGrade)
                .toList();
    }

    @Transactional(readOnly = true)
    public GpaDto gpa(Long korisnikId) {
        Student s = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Student nije pronađen"));
        List<Ocena> ocene = ocenaRepository.findByStudentIdWithDetails(s.getId()).stream()
                .filter(o -> AcademicProgressionRules.ocenaJeDozvoljenaEvidencija(s, o))
                .toList();
        Map<String, Predmet> predmetPoSifri = new HashMap<>();
        Map<String, Integer> bestBySifra = new HashMap<>();
        for (Ocena o : ocene) {
            Predmet p = o.getIspitniTermin().getPredmet();
            String sifra = p.getSifra();
            int v = o.getVrednostOcene();
            predmetPoSifri.putIfAbsent(sifra, p);
            bestBySifra.merge(sifra, v, Math::max);
        }
        int espbSum = 0;
        double weighted = 0;
        int brojPolozenihPredmeta = 0;
        int zbirOcenaPolozenih = 0;
        for (Map.Entry<String, Integer> e : bestBySifra.entrySet()) {
            if (e.getValue() < 6) {
                continue;
            }
            brojPolozenihPredmeta++;
            zbirOcenaPolozenih += e.getValue();
            Predmet p = predmetPoSifri.get(e.getKey());
            int esp = p != null && p.getEspb() != null ? p.getEspb() : 0;
            espbSum += esp;
            weighted += e.getValue() * esp;
        }
        Double prosek = espbSum == 0 ? null : Math.round((weighted / espbSum) * 100.0) / 100.0;
        Double prosekAritmetickiPolozenih = brojPolozenihPredmeta == 0
                ? null
                : Math.round((zbirOcenaPolozenih * 100.0 / brojPolozenihPredmeta)) / 100.0;
        long programId = s.getStudijskiProgram().getId();
        int ukupnoPredmetaNaProgramu = (int) predmetRepository.countByStudijskiProgram_Id(programId);
        return new GpaDto(
                prosek,
                prosekAritmetickiPolozenih,
                espbSum,
                ocene.size(),
                brojPolozenihPredmeta,
                ukupnoPredmetaNaProgramu);
    }

    /**
     * Kurikulum studijskog programa studenta sa statusom po predmetu (najbolji pokušaj).
     * Godina i semestar predmeta dolaze iz kolona {@code kurikulum_godina} i {@code kurikulum_semestar} u bazi (nastavni plan).
     * Procenjena godina studenta: akademska godina počinje oktobrom (1–6).
     */
    @Transactional(readOnly = true)
    public CurriculumProgressDto curriculumProgress(Long korisnikId) {
        Student s = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Student nije pronađen"));
        int godinaUpisa = s.getGodinaUpisa();
        LocalDate today = LocalDate.now();
        final int procenjenaGodina = procenjenaGodinaStudijaAkademska(godinaUpisa, today);

        List<Ocena> sveOcene = ocenaRepository.findByStudentIdWithDetails(s.getId());
        List<Ocena> ocene = sveOcene.stream()
                .filter(o -> AcademicProgressionRules.ocenaJeDozvoljenaEvidencija(s, o))
                .toList();
        Map<String, Integer> bestBySifra = new HashMap<>();
        Map<String, List<Ocena>> izlasciPoSifri = new HashMap<>();
        for (Ocena o : ocene) {
            String sifra = o.getIspitniTermin().getPredmet().getSifra();
            bestBySifra.merge(sifra, o.getVrednostOcene(), Math::max);
            izlasciPoSifri.computeIfAbsent(sifra, k -> new ArrayList<>()).add(o);
        }
        for (List<Ocena> lst : izlasciPoSifri.values()) {
            lst.sort(Comparator.comparing((Ocena o) -> o.getIspitniTermin().getDatumVreme()).reversed());
        }

        long programId = s.getStudijskiProgram().getId();
        List<Predmet> predmeti = predmetRepository.findAllByStudijskiProgramIdOrderByKurikulumSifra(programId);
        List<CurriculumSubjectDto> redovi = new ArrayList<>();
        for (Predmet p : predmeti) {
            int gs = p.getKurikulumGodina();
            int sem = p.getKurikulumSemestar();
            Integer najbolja = bestBySifra.get(p.getSifra());
            String status;
            if (najbolja != null && najbolja >= 6) {
                status = "POLOZENO";
            } else if (najbolja != null) {
                status = "PALI";
            } else if (!predmetJeUSvojojFaziStudija(godinaUpisa, today, gs, sem)) {
                status = "KASNIJE";
            } else {
                status = "BEZ_IZLAZAKA";
            }
            List<CurriculumAttemptDto> izlasci = izlasciPoSifri.getOrDefault(p.getSifra(), List.of()).stream()
                    .map(this::toCurriculumAttempt)
                    .toList();
            redovi.add(new CurriculumSubjectDto(
                    p.getId(),
                    p.getSifra(),
                    p.getNaziv(),
                    p.getEspb() != null ? p.getEspb() : 0,
                    gs,
                    sem,
                    status,
                    najbolja,
                    izlasci
            ));
        }
        redovi.sort(Comparator.comparingInt(CurriculumSubjectDto::godinaStudija)
                .thenComparingInt(CurriculumSubjectDto::semestar)
                .thenComparing(CurriculumSubjectDto::sifra));

        int brojPolozenih = 0;
        int brojNepolozenih = 0;
        int brojBezIzlaska = 0;
        int brojKasnije = 0;
        for (CurriculumSubjectDto row : redovi) {
            switch (row.status()) {
                case "POLOZENO" -> brojPolozenih++;
                case "PALI" -> brojNepolozenih++;
                case "BEZ_IZLAZAKA" -> brojBezIzlaska++;
                case "KASNIJE" -> brojKasnije++;
                default -> {
                }
            }
        }
        var prog = s.getStudijskiProgram();
        return new CurriculumProgressDto(
                procenjenaGodina,
                godinaUpisa,
                prog.getSifra(),
                prog.getNaziv(),
                redovi.size(),
                brojPolozenih,
                brojNepolozenih,
                brojBezIzlaska,
                brojKasnije,
                redovi
        );
    }

    private CurriculumAttemptDto toCurriculumAttempt(Ocena o) {
        var t = o.getIspitniTermin();
        return new CurriculumAttemptDto(
                t.getDatumVreme().toString(),
                t.getIspitniRok().getNaziv(),
                o.getVrednostOcene(),
                o.getPoeni()
        );
    }

    /**
     * Godina studija 1–6; školska godina počinje oktobrom (npr. upis 2025 → 1. godina do septembra 2026).
     */
    static int procenjenaGodinaStudijaAkademska(int godinaUpisa, LocalDate d) {
        int cy = d.getYear();
        int cm = d.getMonthValue();
        int pocetakTekuceSkolske = cm >= Month.OCTOBER.getValue() ? cy : cy - 1;
        int g = pocetakTekuceSkolske - godinaUpisa + 1;
        return Math.max(1, Math.min(6, g));
    }

    /**
     * Završeni semestri u tekućoj godini studija: okt–jan = 0, feb–jun = 1 (posle zimskog), jul–sep = 2.
     */
    static int zavrseniSemestriUTekucojGodiniStudija(Month month) {
        int m = month.getValue();
        if (m >= 10 || m == 1) {
            return 0;
        }
        if (m >= 2 && m <= 6) {
            return 1;
        }
        return 2;
    }

    static int ukupnoZavrsenihSemestara(int godinaUpisa, LocalDate d) {
        int gs = procenjenaGodinaStudijaAkademska(godinaUpisa, d);
        int uGodini = zavrseniSemestriUTekucojGodiniStudija(d.getMonth());
        return 2 * (gs - 1) + uGodini;
    }

    /**
     * Za statistiku polaganja: predmeti II semestra u okviru godine kurikuluma smisleni su tek posle letnjeg dela
     * godine (jul–sep), kad je u modelu završen i drugi semestar tekuće školske godine. Okt–jun nema relevantnih
     * položenih za II sem. u tom smislu.
     */
    static boolean statistikaPolaganjaZaDrugiSemestarKurikulumaAktivna(LocalDate d) {
        return zavrseniSemestriUTekucojGodiniStudija(d.getMonth()) == 2;
    }

    /** Za {@code filterGodinaUpisa == 2021}: generacija „2021 i ranije“ ({@code godinaUpisa <= 2021}); inače tačan sklad sa godinom. */
    static boolean studentUpadaUStatistikuGeneracije(int godinaUpisa, Integer filterGodinaUpisa) {
        if (filterGodinaUpisa == null) {
            return true;
        }
        if (filterGodinaUpisa == 2021) {
            return godinaUpisa <= 2021;
        }
        return godinaUpisa == filterGodinaUpisa;
    }

    /**
     * Broj indeksa u API odgovoru: vodeća slova zamenjuju se šifrom studijskog programa (RI, SI, …).
     * Tako se u pregledu smera uvek vidi ispravan prefiks i kad je u bazi ostao generički „RA“.
     */
    static String brojIndeksaSaSifromPrograma(String brojIndeksa, String programSifra) {
        if (brojIndeksa == null || programSifra == null) {
            return brojIndeksa;
        }
        String t = brojIndeksa.trim();
        int i = 0;
        while (i < t.length() && Character.isLetter(t.charAt(i))) {
            i++;
        }
        if (i == 0) {
            return brojIndeksa;
        }
        String tail = t.substring(i).trim();
        if (tail.isEmpty()) {
            return brojIndeksa;
        }
        return programSifra + " " + tail;
    }

    @Transactional(readOnly = true)
    public HeadStudentsBundleDto headStudentsBundle(Long korisnikId) {
        assertSefKatedre(korisnikId);
        var sef = sefKatedreRepository.findByKorisnikIdWithKatedra(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Niste šef katedre"));
        long katedraId = sef.getKatedra().getId();
        List<HeadProgramSummaryDto> programi = studijskiProgramRepository.findByKatedraId(katedraId).stream()
                .map(p -> new HeadProgramSummaryDto(p.getId(), p.getSifra(), p.getNaziv()))
                .toList();
        List<StudentListDto> studenti = studentRepository.findByKatedraId(katedraId).stream()
                .map(st -> {
                    var p = st.getStudijskiProgram();
                    return new StudentListDto(
                            st.getId(),
                            brojIndeksaSaSifromPrograma(st.getBrojIndeksa(), p.getSifra()),
                            st.getKorisnik().getIme(),
                            st.getKorisnik().getPrezime(),
                            st.getKorisnik().getEmail(),
                            p.getNaziv(),
                            p.getId(),
                            p.getSifra()
                    );
                })
                .toList();
        return new HeadStudentsBundleDto(programi, studenti);
    }

    @Transactional(readOnly = true)
    public List<HeadProgramSummaryDto> studyProgramsForHead(Long korisnikId) {
        assertSefKatedre(korisnikId);
        var sef = sefKatedreRepository.findByKorisnikIdWithKatedra(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Niste šef katedre"));
        return studijskiProgramRepository.findByKatedraId(sef.getKatedra().getId()).stream()
                .map(p -> new HeadProgramSummaryDto(p.getId(), p.getSifra(), p.getNaziv()))
                .toList();
    }

    /**
     * Pregled jednog studijskog programa katedre: studenti (procenjena godina po akademskoj godini od oktobra),
     * predmeti sa semestrom u okviru godine kurikuluma, statistika polaganja samo za studente koji su po datumu
     * završili dovoljno semestara da bi mogli da polažu taj predmet (godina + semestar u kurikulumu).
     *
     * @param statistikaGodinaUpisa ako nije {@code null}, u statistiku ulaze samo studenti te kalendarske godine upisa
     *                              (jedna generacija); inače svi studenti programa. Ignoriše se kad je {@code statistikaCeoProgram}.
     * @param statistikaCeoProgram ako je {@code true}: za svaki predmet na programu brojači koriste <strong>sve</strong> studente
     *                           na tom programu (bez filtra generacije i bez „dovoljno semestara“ / II sem. kalendara);
     *                           {@code elegibilnih} = broj studenata na programu, ostalo na osnovu najboljih ocena iz evidencije.
     */
    @Transactional(readOnly = true)
    public HeadProgramPregledDto programPregledForHead(
            Long korisnikId,
            long programId,
            Integer statistikaGodinaUpisa,
            Boolean statistikaCeoProgram) {
        assertSefKatedre(korisnikId);
        var sef = sefKatedreRepository.findByKorisnikIdWithKatedra(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Niste šef katedre"));
        long katedraId = sef.getKatedra().getId();
        StudijskiProgram prog = studijskiProgramRepository.findByIdAndKatedraId(programId, katedraId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Studijski program nije na vašoj katedri"));

        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findByStudijskiProgramIdWithKorisnik(programId);
        List<HeadStudentRowDto> studentRows = new ArrayList<>();
        for (Student s : students) {
            int gy = s.getGodinaUpisa();
            int procenjena = linearAcademicTimeline.koristiStrogiLinearniModel(gy)
                    ? linearAcademicTimeline.studyYearFromGodinaUpisa(gy)
                    : procenjenaGodinaStudijaAkademska(gy, today);
            var k = s.getKorisnik();
            studentRows.add(new HeadStudentRowDto(
                    s.getId(),
                    brojIndeksaSaSifromPrograma(s.getBrojIndeksa(), prog.getSifra()),
                    k.getIme(),
                    k.getPrezime(),
                    k.getEmail(),
                    procenjena,
                    gy
            ));
        }

        List<Predmet> predmeti = predmetRepository.findAllByStudijskiProgramIdOrderByKurikulumSifra(programId);
        List<HeadPredmetRowDto> predmetRows = new ArrayList<>();
        for (Predmet p : predmeti) {
            int gs = p.getKurikulumGodina();
            int sem = p.getKurikulumSemestar();
            predmetRows.add(new HeadPredmetRowDto(
                    p.getId(),
                    p.getSifra(),
                    p.getNaziv(),
                    p.getEspb() != null ? p.getEspb() : 0,
                    gs,
                    sem
            ));
        }

        List<Ocena> ocene = ocenaRepository.findAllByStudijskiProgramId(programId);
        Map<Long, Map<Long, Integer>> bestByStudentPredmet = new HashMap<>();
        for (Ocena o : ocene) {
            Student st = o.getStudent();
            if (!AcademicProgressionRules.ocenaJeDozvoljenaEvidencija(st, o)) {
                continue;
            }
            long sid = st.getId();
            long pid = o.getIspitniTermin().getPredmet().getId();
            int v = o.getVrednostOcene();
            bestByStudentPredmet
                    .computeIfAbsent(sid, x -> new HashMap<>())
                    .merge(pid, v, Math::max);
        }

        boolean iiSemKurikulumaAktivnoZaStat = statistikaPolaganjaZaDrugiSemestarKurikulumaAktivna(today);
        boolean ceoProgram = Boolean.TRUE.equals(statistikaCeoProgram);

        List<HeadPredmetStatDto> stats = new ArrayList<>();
        for (HeadPredmetRowDto pr : predmetRows) {
            int g = pr.godinaStudija();
            int sem = pr.semestar();
            long pid = pr.id();
            int elegibilnih = 0;
            int saIzlaskom = 0;
            int polozili = 0;
            int pali = 0;
            if (ceoProgram) {
                for (HeadStudentRowDto st : studentRows) {
                    elegibilnih++;
                    Integer best = bestByStudentPredmet.getOrDefault(st.id(), Map.of()).get(pid);
                    if (best == null) {
                        continue;
                    }
                    saIzlaskom++;
                    if (best >= 6) {
                        polozili++;
                    } else {
                        pali++;
                    }
                }
            } else {
                boolean predmetIIsem = sem == 2;
                boolean racunajRed = !predmetIIsem || iiSemKurikulumaAktivnoZaStat;
                if (racunajRed) {
                    for (HeadStudentRowDto st : studentRows) {
                        if (!studentUpadaUStatistikuGeneracije(st.godinaUpisa(), statistikaGodinaUpisa)) {
                            continue;
                        }
                        if (!AcademicProgressionRules.studentMozePolagatiPredmetNaDatum(
                                st.godinaUpisa(), today, g, sem)) {
                            continue;
                        }
                        elegibilnih++;
                        Integer best = bestByStudentPredmet.getOrDefault(st.id(), Map.of()).get(pid);
                        if (best == null) {
                            continue;
                        }
                        saIzlaskom++;
                        if (best >= 6) {
                            polozili++;
                        } else {
                            pali++;
                        }
                    }
                }
            }
            Double pct = saIzlaskom == 0
                    ? null
                    : Math.round(100.0 * polozili / saIzlaskom * 100.0) / 100.0;
            stats.add(new HeadPredmetStatDto(
                    pid,
                    pr.sifra(),
                    pr.naziv(),
                    g,
                    sem,
                    elegibilnih,
                    saIzlaskom,
                    polozili,
                    pali,
                    pct
            ));
        }

        var summary = new HeadProgramSummaryDto(prog.getId(), prog.getSifra(), prog.getNaziv());
        String napomena = ceoProgram
                ? ("Režim „ceo program“: za svaki od " + predmetRows.size() + " predmeta na smeru brojači koriste sve studente "
                        + "na programu; u agregat ulaze samo ocene čiji je datum ispita u skladu sa pravilom napredovanja "
                        + "(završeni semestri do tog datuma vs. godina/semestar predmeta u kurikulumu). "
                        + "% položenih = položili / sa izlaskom.")
                : ("Za generacije 2022–2025 važi striktan linearni model (bez ponavljanja godine): uzorak i procenjena godina "
                        + "kurikuluma odgovaraju godini upisa. Starije generacije (≤2021) koriste kalendar koji počinje oktobrom "
                        + "i pravilo o II semestru (jul–sep). Predmeti u okviru iste godine kurikuluma podeljeni su na I i II semestar. "
                        + (statistikaGodinaUpisa != null
                        ? (statistikaGodinaUpisa == 2021
                        ? "Statistika je ograničena na generacije upisa 2021 i ranije."
                        : "Statistika je ograničena na generaciju upisa " + statistikaGodinaUpisa + ".")
                        : "Opciono možete ograničiti statistiku na jednu godinu upisa (jednu generaciju)."));
        return new HeadProgramPregledDto(summary, studentRows, predmetRows, stats, napomena);
    }

    private boolean predmetJeUSvojojFaziStudija(int godinaUpisa, LocalDate today, int gs, int sem) {
        return AcademicProgressionRules.studentMozePolagatiPredmetNaDatum(godinaUpisa, today, gs, sem);
    }

    private SubjectGradeDto toSubjectGrade(Ocena o) {
        var t = o.getIspitniTermin();
        var p = t.getPredmet();
        return new SubjectGradeDto(
                p.getSifra(),
                p.getNaziv(),
                p.getEspb(),
                o.getVrednostOcene(),
                o.getPoeni(),
                t.getDatumVreme().toString(),
                t.getIspitniRok().getNaziv()
        );
    }

    public record StudentProfileDto(
            String ime, String prezime, String email, String brojIndeksa,
            String studijskiProgramNaziv, String studijskiProgramSifra, String katedraNaziv, int godinaUpisa
    ) {
    }

    public record SubjectGradeDto(
            String predmetSifra, String predmetNaziv, int espb, int ocena, Integer poeni,
            String datumIspita, String ispitniRok
    ) {
    }

    public record GpaDto(
            Double prosekNaEspb,
            Double prosekAritmetickiPolozenihPredmeta,
            int zbirEspbPolozenih,
            /** Ukupan broj evidentiranih izlazaka (učitavanja na ispit), posle pravila napredovanja. */
            int ukupnoIspita,
            int brojPolozenihPredmeta,
            int ukupnoPredmetaNaProgramu
    ) {
    }

    public record CurriculumProgressDto(
            int procenjenaGodinaStudija,
            int godinaUpisa,
            String studijskiProgramSifra,
            String studijskiProgramNaziv,
            int ukupnoPredmetaNaProgramu,
            int brojPolozenih,
            int brojNepolozenih,
            int brojBezIzlaska,
            int brojPredmetaKasnije,
            List<CurriculumSubjectDto> predmeti
    ) {
    }

    public record CurriculumSubjectDto(
            long predmetId,
            String sifra,
            String naziv,
            int espb,
            int godinaStudija,
            int semestar,
            String status,
            Integer najboljaOcena,
            List<CurriculumAttemptDto> izlasci
    ) {
    }

    public record CurriculumAttemptDto(
            String datumIspita,
            String ispitniRok,
            int ocena,
            Integer poeni
    ) {
    }

    public record StudentListDto(
            Long id,
            String brojIndeksa,
            String ime,
            String prezime,
            String email,
            String programNaziv,
            Long studijskiProgramId,
            String studijskiProgramSifra
    ) {
    }

    public record HeadStudentsBundleDto(
            List<HeadProgramSummaryDto> programi,
            List<StudentListDto> studenti
    ) {
    }

    public record HeadProgramSummaryDto(long id, String sifra, String naziv) {
    }

    public record HeadStudentRowDto(
            long id,
            String brojIndeksa,
            String ime,
            String prezime,
            String email,
            int procenjenaGodina,
            int godinaUpisa
    ) {
    }

    public record HeadPredmetRowDto(long id, String sifra, String naziv, int espb, int godinaStudija, int semestar) {
    }

    public record HeadPredmetStatDto(
            long predmetId,
            String sifra,
            String naziv,
            int godinaStudija,
            int semestar,
            int brojElegibilnihStudenata,
            int brojSaBarJednimIzlaskom,
            int brojPolozenih,
            int brojPali,
            Double procenatPolozenihOdIzlazaka
    ) {
    }

    public record HeadProgramPregledDto(
            HeadProgramSummaryDto program,
            List<HeadStudentRowDto> studenti,
            List<HeadPredmetRowDto> predmeti,
            List<HeadPredmetStatDto> statistikaPolaganja,
            String napomenaOUzorku
    ) {
    }
}
