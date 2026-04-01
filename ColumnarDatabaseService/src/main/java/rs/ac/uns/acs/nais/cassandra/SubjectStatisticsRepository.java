package rs.ac.uns.acs.nais.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface SubjectStatisticsRepository extends CassandraRepository<SubjectStatistics, Long> {
}
