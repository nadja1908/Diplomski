package rs.ac.uns.acs.nais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "rs.ac.uns.acs.nais.repository")
@EnableDiscoveryClient
public class RelationalDatabaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelationalDatabaseServiceApplication.class, args);
    }
}
