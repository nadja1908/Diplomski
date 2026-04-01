package rs.ac.uns.acs.nais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableCassandraRepositories(basePackages = "rs.ac.uns.acs.nais.cassandra")
@EnableDiscoveryClient
public class ColumnarDatabaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColumnarDatabaseServiceApplication.class, args);
    }
}
