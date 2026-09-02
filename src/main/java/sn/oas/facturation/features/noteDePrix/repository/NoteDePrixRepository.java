package sn.oas.facturation.features.noteDePrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;

@Repository
public interface NoteDePrixRepository extends JpaRepository<NoteDePrix, Long> {
}
