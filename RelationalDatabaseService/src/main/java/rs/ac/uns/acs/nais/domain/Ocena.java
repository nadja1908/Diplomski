package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ocena",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "ispitni_termin_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ocena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ispitni_termin_id")
    private IspitniTermin ispitniTermin;

    private Integer poeni;

    @Column(name = "vrednost_ocene", nullable = false)
    private Integer vrednostOcene;
}
