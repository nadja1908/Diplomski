package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "preduslov",
        uniqueConstraints = @UniqueConstraint(columnNames = {"predmet_id", "preduslov_predmet_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preduslov {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predmet_id")
    private Predmet predmet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preduslov_predmet_id")
    private Predmet preduslovPredmet;
}
