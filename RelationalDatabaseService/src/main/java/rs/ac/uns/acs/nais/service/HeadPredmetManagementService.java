package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.domain.SadrzajPredmeta;
import rs.ac.uns.acs.nais.domain.StudijskiProgram;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.SadrzajPredmetaRepository;
import rs.ac.uns.acs.nais.repository.StudijskiProgramRepository;
import rs.ac.uns.acs.nais.web.dto.HeadPredmetDetailResponse;
import rs.ac.uns.acs.nais.web.dto.HeadPredmetUpsertRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HeadPredmetManagementService {

    private final AcademicQueryService academicQueryService;
    private final PredmetRepository predmetRepository;
    private final SadrzajPredmetaRepository sadrzajPredmetaRepository;
    private final StudijskiProgramRepository studijskiProgramRepository;

    @Transactional(readOnly = true)
    public List<HeadPredmetDetailResponse> listForHead(Long korisnikId) {
        long katedraId = academicQueryService.katedraIdForHeadOrThrow(korisnikId);
        List<Predmet> predmeti = predmetRepository.findByKatedraId(katedraId);
        if (predmeti.isEmpty()) {
            return List.of();
        }
        List<Long> ids = predmeti.stream().map(Predmet::getId).toList();
        Map<Long, SadrzajPredmeta> sadrzajPoPredmetId = new LinkedHashMap<>();
        for (SadrzajPredmeta sd : sadrzajPredmetaRepository.findAllByPredmetIdIn(ids)) {
            sadrzajPoPredmetId.put(sd.getPredmetId(), sd);
        }
        List<HeadPredmetDetailResponse> out = new ArrayList<>();
        for (Predmet p : predmeti) {
            SadrzajPredmeta sd = sadrzajPoPredmetId.get(p.getId());
            out.add(toResponse(p, sd));
        }
        return out;
    }

    @Transactional
    public HeadPredmetDetailResponse create(Long korisnikId, HeadPredmetUpsertRequest req) {
        long katedraId = academicQueryService.katedraIdForHeadOrThrow(korisnikId);
        StudijskiProgram prog = studijskiProgramRepository
                .findByIdAndKatedraId(req.studijskiProgramId(), katedraId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Studijski program ne pripada vašoj katedri"));
        String sifraNorm = Objects.requireNonNull(req.sifra(), "").trim();
        String nazivNorm = Objects.requireNonNull(req.naziv(), "").trim();
        validateCore(sifraNorm, nazivNorm, req.espb());
        assertNoDuplicateSifra(sifraNorm);
        assertNoDuplicateNaziv(nazivNorm, null);
        Predmet p = new Predmet();
        p.setSifra(sifraNorm);
        p.setNaziv(nazivNorm);
        p.setEspb(req.espb());
        p.setStudijskiProgram(prog);
        p.setKatedra(prog.getKatedra());
        p.setKratakOpis(blankToNull(req.kratakOpis()));
        p.setKurikulumGodina(req.kurikulumGodina());
        p.setKurikulumSemestar(req.kurikulumSemestar());
        predmetRepository.saveAndFlush(p);
        SadrzajPredmeta sd = buildContent(p, req);
        sadrzajPredmetaRepository.save(sd);
        return toResponse(p, sd);
    }

    @Transactional
    public HeadPredmetDetailResponse update(Long korisnikId, long predmetId, HeadPredmetUpsertRequest req) {
        long katedraId = academicQueryService.katedraIdForHeadOrThrow(korisnikId);
        Predmet p = predmetRepository
                .findByIdAndKatedraIdFetchProgram(predmetId, katedraId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Predmet nije pronađen"));
        StudijskiProgram prog = studijskiProgramRepository
                .findByIdAndKatedraId(req.studijskiProgramId(), katedraId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Studijski program ne pripada vašoj katedri"));
        String sifraNorm = Objects.requireNonNull(req.sifra(), "").trim();
        String nazivNorm = Objects.requireNonNull(req.naziv(), "").trim();
        validateCore(sifraNorm, nazivNorm, req.espb());
        if (!p.getSifra().trim().equals(sifraNorm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Šifra predmeta se ne može menjati.");
        }
        assertNoDuplicateNaziv(nazivNorm, p.getId());
        p.setNaziv(nazivNorm);
        p.setEspb(req.espb());
        p.setStudijskiProgram(prog);
        p.setKatedra(prog.getKatedra());
        p.setKratakOpis(blankToNull(req.kratakOpis()));
        p.setKurikulumGodina(req.kurikulumGodina());
        p.setKurikulumSemestar(req.kurikulumSemestar());
        predmetRepository.save(p);
        SadrzajPredmeta sd = sadrzajPredmetaRepository.findById(p.getId()).orElse(null);
        if (sd == null) {
            sd = buildContent(p, req);
        } else {
            sd.setCilj(blankToNull(req.cilj()));
            sd.setIshodiUcenja(blankToNull(req.ishodiUcenja()));
            sd.setMetodeNastave(blankToNull(req.metodeNastave()));
            sd.setTemeKursa(blankToNull(req.temeKursa()));
        }
        sadrzajPredmetaRepository.save(sd);
        return toResponse(p, sd);
    }

    private static void validateCore(String sifraNorm, String nazivNorm, Integer espb) {
        if (sifraNorm.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Šifra predmeta ne sme biti prazna");
        }
        if (nazivNorm.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv predmeta ne sme biti prazan");
        }
        if (espb == null || espb <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ESPB mora biti pozitivan broj");
        }
    }

    private void assertNoDuplicateSifra(String sifraNorm) {
        if (predmetRepository.countByNormalizedSifra(sifraNorm) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Već postoji predmet sa tom šifrom.");
        }
    }

    private void assertNoDuplicateNaziv(String nazivNorm, Long excludePredmetId) {
        long count = excludePredmetId == null
                ? predmetRepository.countByNormalizedNaziv(nazivNorm)
                : predmetRepository.countByNormalizedNazivExcludingPredmet(nazivNorm, excludePredmetId);
        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Već postoji predmet sa istim nazivom.");
        }
    }

    private static SadrzajPredmeta buildContent(Predmet p, HeadPredmetUpsertRequest req) {
        SadrzajPredmeta sd = new SadrzajPredmeta();
        sd.setPredmet(p);
        sd.setCilj(blankToNull(req.cilj()));
        sd.setIshodiUcenja(blankToNull(req.ishodiUcenja()));
        sd.setMetodeNastave(blankToNull(req.metodeNastave()));
        sd.setTemeKursa(blankToNull(req.temeKursa()));
        return sd;
    }

    private static HeadPredmetDetailResponse toResponse(Predmet p, SadrzajPredmeta sd) {
        var prog = p.getStudijskiProgram();
        String cilj = sd != null ? nvl(sd.getCilj()) : "";
        String ish = sd != null ? nvl(sd.getIshodiUcenja()) : "";
        String met = sd != null ? nvl(sd.getMetodeNastave()) : "";
        String teme = sd != null ? nvl(sd.getTemeKursa()) : "";
        return new HeadPredmetDetailResponse(
                p.getId(),
                p.getSifra(),
                p.getNaziv(),
                p.getEspb() != null ? p.getEspb() : 0,
                prog.getId(),
                prog.getSifra(),
                prog.getNaziv(),
                nvl(p.getKratakOpis()),
                p.getKurikulumGodina(),
                p.getKurikulumSemestar(),
                cilj,
                ish,
                met,
                teme);
    }

    private static String blankToNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}