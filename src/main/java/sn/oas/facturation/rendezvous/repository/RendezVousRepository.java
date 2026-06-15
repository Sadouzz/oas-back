package sn.oas.facturation.rendezvous.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;

import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByClientIdOrderByDateCreationDesc(Long clientId);
    List<RendezVous> findByClientIdAndStatutOrderByDateCreationDesc(Long clientId, RendezVousStatus statut);
    List<RendezVous> findAllByOrderByDateCreationDesc();
}
