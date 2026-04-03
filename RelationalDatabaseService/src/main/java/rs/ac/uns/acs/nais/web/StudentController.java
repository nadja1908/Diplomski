package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.dto.stats.ProgramSubjectStatisticsDtos.UnpassedSubjectPassRateDto;
import rs.ac.uns.acs.nais.service.AcademicQueryService;
import rs.ac.uns.acs.nais.service.ProgramSubjectAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/student/me")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final AcademicQueryService academicQueryService;
    private final ProgramSubjectAnalyticsService programSubjectAnalyticsService;

    @GetMapping("/profile")
    public AcademicQueryService.StudentProfileDto profile(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.studentProfile(korisnikId);
    }

    @GetMapping("/subjects-grades")
    public Object subjectsGrades(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.subjectsAndGrades(korisnikId);
    }

    @GetMapping("/gpa")
    public AcademicQueryService.GpaDto gpa(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.gpa(korisnikId);
    }

    @GetMapping("/curriculum-progress")
    public AcademicQueryService.CurriculumProgressDto curriculumProgress(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.curriculumProgress(korisnikId);
    }

    /**
     * Predmeti koje student još nije položio, sa stopom prolaznosti na celom studijskom programu (SQL agregat).
     */
    @GetMapping("/unpassed-subject-pass-rates")
    public List<UnpassedSubjectPassRateDto> unpassedSubjectPassRates(
            @AuthenticationPrincipal Long korisnikId) {
        try {
            return programSubjectAnalyticsService.unpassedSubjectsPassRatesSorted(korisnikId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
