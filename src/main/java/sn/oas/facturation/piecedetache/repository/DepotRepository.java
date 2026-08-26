package sn.oas.facturation.piecedetache.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.piecedetache.data.entity.Depot;
@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {}
