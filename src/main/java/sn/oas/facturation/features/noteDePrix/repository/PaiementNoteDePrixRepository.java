package sn.oas.facturation.features.noteDePrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.noteDePrix.data.entity.PaiementNoteDePrix;

import java.util.List;

@Repository
public interface PaiementNoteDePrixRepository extends JpaRepository<PaiementNoteDePrix, Long> {
    List<PaiementNoteDePrix> findByNoteDePrixIdOrderByDatePaiementDescIdDesc(Long noteDePrixId);
}
