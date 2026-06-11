package sn.oas.facturation.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.piecedetache.data.entity.PDP;

@Repository
public interface PDPRepository extends JpaRepository<PDP, Long> {
}
