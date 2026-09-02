package sn.oas.facturation.features.rendezvous.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;

import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByClientIdOrderByDateCreationDesc(Long clientId);
    List<RendezVous> findByClientIdAndStatutOrderByDateCreationDesc(Long clientId, RendezVousStatus statut);
    List<RendezVous> findAllByOrderByDateCreationDesc();
    boolean existsByVehiculeIdAndStatut(Long vehiculeId, RendezVousStatus statut);
    boolean existsByVehiculeIdAndStatutAndDateRendezVousAfter(Long vehiculeId, RendezVousStatus statut, java.time.LocalDateTime date);
}
