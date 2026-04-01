package rs.ac.uns.acs.nais.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;

@PrimaryKeyClass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMonthlyTrendKey implements Serializable {

    @PrimaryKeyColumn(name = "predmet_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long predmetId;

    @PrimaryKeyColumn(name = "mesec", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private String mesec;
}
