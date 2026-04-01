package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "katedra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Katedra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sifra;

    @Column(nullable = false)
    private String naziv;
}
