package rs.ac.uns.acs.nais.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;

public interface SubjectMonthlyTrendRepository extends CassandraRepository<SubjectMonthlyTrend, SubjectMonthlyTrendKey> {

    List<SubjectMonthlyTrend> findByKeyPredmetId(Long predmetId);
}
