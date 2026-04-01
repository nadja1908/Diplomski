package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.security.JwtPrincipal;
import rs.ac.uns.acs.nais.service.ColumnarStatsService;

@RestController
@RequestMapping("/api/student/me")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class ColumnarStudentController {

    private final ColumnarStatsService columnarStatsService;

    @GetMapping("/statistics")
    public Object statistics(@AuthenticationPrincipal JwtPrincipal principal) {
        return columnarStatsService.studentCassandraStats(principal.getKorisnikId());
    }
}
