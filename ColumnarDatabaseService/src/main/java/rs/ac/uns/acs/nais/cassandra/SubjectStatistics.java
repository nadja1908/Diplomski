package rs.ac.uns.acs.nais.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("subject_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectStatistics {

    @PrimaryKey
    private Long predmetId;

    @Column("naziv_predmeta")
    private String nazivPredmeta;

    @Column("ukupno_polaganja")
    private Long ukupnoPolaganja;

    @Column("polozeno")
    private Long polozeno;

    @Column("pali")
    private Long pali;

    @Column("zbir_ocena")
    private Long zbirOcena;

    @Column("broj_ocena")
    private Long brojOcena;
}
