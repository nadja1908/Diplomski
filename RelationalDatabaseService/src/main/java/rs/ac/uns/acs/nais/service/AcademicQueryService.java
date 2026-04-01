package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.domain.Korisnik;
import rs.ac.uns.acs.nais.domain.Ocena;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.Student;
import rs.ac.uns.acs.nais.repository.KorisnikRepository;
import rs.ac.uns.acs.nais.repository.OcenaRepository;
import rs.ac.uns.acs.nais.repository.SefKatedreRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AcademicQueryService {

    private final KorisnikRepository korisnikRepository;
    private final StudentRepository studentRepository;
    private final SefKatedreRepository sefKatedreRepository;
    private final OcenaRepository ocenaRepository;

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
                s.getBrojIndeksa(),
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
                .map(this::toSubjectGrade)
                .toList();
    }

    @Transactional(readOnly = true)
    public GpaDto gpa(Long korisnikId) {
        Student s = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new IllegalStateException("Student nije pronađen"));
        List<Ocena> ocene = ocenaRepository.findByStudentIdWithDetails(s.getId());
        int espbSum = 0;
        double weighted = 0;
        for (Ocena o : ocene) {
            if (o.getVrednostOcene() < 6) {
                continue;
            }
            Predmet p = o.getIspitniTermin().getPredmet();
            int e = p.getEspb() != null ? p.getEspb() : 0;
            espbSum += e;
            weighted += o.getVrednostOcene() * e;
        }
        Double prosek = espbSum == 0 ? null : Math.round((weighted / espbSum) * 100.0) / 100.0;
        return new GpaDto(prosek, espbSum, ocene.size());
    }

    public List<StudentListDto> studentsForHead(Korisnik korisnik) {
        var sef = sefKatedreRepository.findByKorisnikIdWithKatedra(korisnik.getId())
                .orElseThrow(() -> new IllegalStateException("Niste šef katedre"));
        return studentRepository.findByKatedraId(sef.getKatedra().getId()).stream()
                .map(st -> new StudentListDto(
                        st.getId(),
                        st.getBrojIndeksa(),
                        st.getKorisnik().getIme(),
                        st.getKorisnik().getPrezime(),
                        st.getKorisnik().getEmail(),
                        st.getStudijskiProgram().getNaziv()
                ))
                .toList();
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

    public record GpaDto(Double prosekNaEspb, int zbirEspbPolozenih, int ukupnoIspita) {
    }

    public record StudentListDto(
            Long id, String brojIndeksa, String ime, String prezime, String email, String programNaziv
    ) {
    }
}
