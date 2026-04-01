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
@RequestMapping("/api/head")
@PreAuthorize("hasRole('SEF_KATEDRE')")
@RequiredArgsConstructor
public class ColumnarHeadController {

    private final ColumnarStatsService columnarStatsService;

    @GetMapping("/subjects/analytics")
    public Object subjectAnalytics(@AuthenticationPrincipal JwtPrincipal principal) {
        return columnarStatsService.headSubjectAnalytics(principal.getKorisnikId());
    }

    @GetMapping("/trends/pass-fail")
    public ColumnarStatsService.PassFailTrendDto passFailTrends(@AuthenticationPrincipal JwtPrincipal principal) {
        return columnarStatsService.headPassFailTrends(principal.getKorisnikId());
    }

    @GetMapping("/performance-overview")
    public ColumnarStatsService.PerformanceOverviewDto performance(@AuthenticationPrincipal JwtPrincipal principal) {
        return columnarStatsService.headPerformanceOverview(principal.getKorisnikId());
    }
}
