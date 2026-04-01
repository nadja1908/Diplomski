package rs.ac.uns.acs.nais.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("subject_monthly_trend")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMonthlyTrend {

    @PrimaryKey
    private SubjectMonthlyTrendKey key;

    @Column("polozeno")
    private int polozeno;

    @Column("pali")
    private int pali;

    @Column("prosecna_ocena")
    private double prosecnaOcena;
}
