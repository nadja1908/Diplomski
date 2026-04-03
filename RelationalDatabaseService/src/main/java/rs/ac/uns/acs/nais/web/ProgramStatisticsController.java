package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.ProgramStatisticsResponse;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsFilterOptions;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.StatisticsQueryParams;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.SubjectStatisticsRow;
import rs.ac.uns.acs.nais.academic.LinearAcademicTimeline;
import rs.ac.uns.acs.nais.repository.SefKatedreRepository;
import rs.ac.uns.acs.nais.repository.StudijskiProgramRepository;
import rs.ac.uns.acs.nais.service.ProgramSubjectAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SEF_KATEDRE')")
public class ProgramStatisticsController {

    private final ProgramSubjectAnalyticsService analyticsService;
    private final SefKatedreRepository sefKatedreRepository;
    private final StudijskiProgramRepository studijskiProgramRepository;
    private final LinearAcademicTimeline linearAcademicTimeline;

    private StatisticsQueryParams normalizeAndValidate(StatisticsQueryParams raw) {
        Integer gu = raw.godinaUpisa();
        if (gu == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "godinaUpisa je obavezna (generacije 2022–" + linearAcademicTimeline.referenceIntakeYear() + ").");
        }
        int ref = linearAcademicTimeline.referenceIntakeYear();
        if (gu < 2022 || gu > ref) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "godinaUpisa mora biti između 2022 i " + ref + ".");
        }
        return linearAcademicTimeline.normalizeStatisticsParams(raw);
    }

    @GetMapping("/subjects")
    public ProgramStatisticsResponse subjects(
            @AuthenticationPrincipal Long korisnikId,
            @RequestParam(value = "studyProgramId", required = false) Long studyProgramId,
            @RequestParam(value = "godinaUpisa", required = false) Integer godinaUpisa,
            @RequestParam(value = "skolskaGodina", required = false) String skolskaGodina,
            @RequestParam(value = "kurikulumGodina", required = false) Integer kurikulumGodina,
            @RequestParam(value = "semestar", required = false) Integer semestar,
            @RequestParam(value = "predmetId", required = false) Long predmetId,
            @RequestParam(value = "includeGenerationBreakdown", defaultValue = "false") boolean includeGenerationBreakdown
    ) {
        long programId = resolveProgramOrThrow(korisnikId, studyProgramId);
        StatisticsQueryParams p = normalizeAndValidate(new StatisticsQueryParams(
                godinaUpisa, skolskaGodina, kurikulumGodina, semestar,
                predmetId, includeGenerationBreakdown));
        return analyticsService.computeForProgram(programId, p);
    }

    @GetMapping("/subjects/hardest")
    public List<SubjectStatisticsRow> hardest(
            @AuthenticationPrincipal Long korisnikId,
            @RequestParam(value = "studyProgramId", required = false) Long studyProgramId,
            @RequestParam(value = "godinaUpisa", required = false) Integer godinaUpisa,
            @RequestParam(value = "skolskaGodina", required = false) String skolskaGodina,
            @RequestParam(value = "kurikulumGodina", required = false) Integer kurikulumGodina,
            @RequestParam(value = "semestar", required = false) Integer semestar,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        StatisticsQueryParams p = normalizeAndValidate(
                new StatisticsQueryParams(godinaUpisa, skolskaGodina, kurikulumGodina, semestar, null, false));
        var resp = analyticsService.computeForProgram(resolveProgramOrThrow(korisnikId, studyProgramId), p);
        return resp.rankings().hardestByPassRate().stream().limit(Math.max(1, Math.min(limit, 50))).toList();
    }

    @GetMapping("/subjects/easiest")
    public List<SubjectStatisticsRow> easiest(
            @AuthenticationPrincipal Long korisnikId,
            @RequestParam(value = "studyProgramId", required = false) Long studyProgramId,
            @RequestParam(value = "godinaUpisa", required = false) Integer godinaUpisa,
            @RequestParam(value = "skolskaGodina", required = false) String skolskaGodina,
            @RequestParam(value = "kurikulumGodina", required = false) Integer kurikulumGodina,
            @RequestParam(value = "semestar", required = false) Integer semestar,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        StatisticsQueryParams p = normalizeAndValidate(
                new StatisticsQueryParams(godinaUpisa, skolskaGodina, kurikulumGodina, semestar, null, false));
        var resp = analyticsService.computeForProgram(resolveProgramOrThrow(korisnikId, studyProgramId), p);
        return resp.rankings().easiestByPassRate().stream().limit(Math.max(1, Math.min(limit, 50))).toList();
    }

    @GetMapping("/subjects/{subjectId}")
    public SubjectStatisticsRow subjectOne(
            @AuthenticationPrincipal Long korisnikId,
            @PathVariable long subjectId,
            @RequestParam(value = "studyProgramId", required = false) Long studyProgramId,
            @RequestParam(value = "godinaUpisa", required = false) Integer godinaUpisa,
            @RequestParam(value = "skolskaGodina", required = false) String skolskaGodina,
            @RequestParam(value = "includeGenerationBreakdown", defaultValue = "true") boolean includeGenerationBreakdown
    ) {
        long programId = resolveProgramOrThrow(korisnikId, studyProgramId);
        try {
            StatisticsQueryParams p = normalizeAndValidate(new StatisticsQueryParams(
                    godinaUpisa, skolskaGodina, null, null, null, includeGenerationBreakdown));
            return analyticsService.subjectDetail(programId, subjectId, p);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/filter-options")
    public StatisticsFilterOptions filterOptions(
            @AuthenticationPrincipal Long korisnikId,
            @RequestParam(value = "studyProgramId", required = false) Long studyProgramId,
            @RequestParam(value = "godinaUpisa", required = false) Integer godinaUpisa
    ) {
        long programId = resolveProgramOrThrow(korisnikId, studyProgramId);
        return analyticsService.filterOptions(programId, godinaUpisa);
    }

    private long resolveProgramOrThrow(Long korisnikId, Long studyProgramId) {
        if (korisnikId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niste prijavljeni");
        }
        if (!hasRole("SEF_KATEDRE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nedozvoljena uloga");
        }
        if (studyProgramId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "studyProgramId je obavezan za šefa katedre");
        }
        var sef = sefKatedreRepository.findByKorisnikIdWithKatedra(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Niste šef katedre"));
        studijskiProgramRepository.findByIdAndKatedraId(studyProgramId, sef.getKatedra().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program nije na vašoj katedri"));
        return studyProgramId;
    }

    private static boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String target = "ROLE_" + role;
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(target::equals);
    }
}
