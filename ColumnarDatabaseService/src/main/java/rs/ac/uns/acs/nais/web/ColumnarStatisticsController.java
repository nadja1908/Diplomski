package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.service.ColumnarStatsService;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ColumnarStatisticsController {

    private final ColumnarStatsService columnarStatsService;

    @GetMapping("/rankings")
    public Object rankings() {
        return columnarStatsService.globalRankings();
    }
}
