package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.acs.nais.domain.SadrzajPredmeta;

import java.util.Collection;
import java.util.List;

public interface SadrzajPredmetaRepository extends JpaRepository<SadrzajPredmeta, Long> {

    List<SadrzajPredmeta> findAllByPredmetIdIn(Collection<Long> predmetIds);
}
