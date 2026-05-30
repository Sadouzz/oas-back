package sn.oas.facturation.devisPrevisionnel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel;

import java.util.List;

@Repository
public interface DevisPrevisionnelRepository extends JpaRepository<DevisPrevisionnel, Long> {
    List<DevisPrevisionnel> findByClientId(Long clientId);
    List<DevisPrevisionnel> findByVehiculeId(Long vehiculeId);
}
