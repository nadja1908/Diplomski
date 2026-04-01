package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ispitni_rok")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IspitniRok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String naziv;

    @Column(name = "skolska_godina", nullable = false, length = 20)
    private String skolskaGodina;

    @Column(nullable = false, length = 50)
    private String tip;
}
