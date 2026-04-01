package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1000)
@RequiredArgsConstructor
@Slf4j
public class StatisticsWarmup implements ApplicationRunner {

    private final StatisticsSyncService statisticsSyncService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            statisticsSyncService.rebuildFromRelationalService();
            log.info("Cassandra stats synced from relational service.");
        } catch (Exception e) {
            log.warn("Initial Cassandra sync failed: {}", e.getMessage());
        }
    }
}
