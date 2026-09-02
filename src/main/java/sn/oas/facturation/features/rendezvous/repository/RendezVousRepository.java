package sn.oas.facturation.features.rendezvous.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByClientIdOrderByDateCreationDesc(Long clientId);
    Page<RendezVous> findByClientId(Long clientId, Pageable pageable);
    List<RendezVous> findByClientIdAndStatutOrderByDateCreationDesc(Long clientId, RendezVousStatus statut);
    Page<RendezVous> findByClientIdAndStatut(Long clientId, RendezVousStatus statut, Pageable pageable);
    List<RendezVous> findAllByOrderByDateCreationDesc();
    Page<RendezVous> findByStatut(RendezVousStatus statut, Pageable pageable);

    @Query("SELECT r FROM RendezVous r WHERE " +
            "LOWER(r.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.motif) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.commentaire) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<RendezVous> searchRendezVous(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByVehiculeIdAndStatut(Long vehiculeId, RendezVousStatus statut);
    boolean existsByVehiculeIdAndStatutAndDateRendezVousAfter(Long vehiculeId, RendezVousStatus statut, LocalDateTime date);
}
