package sn.oas.facturation.features.devisPrevisionnel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevisPrevisionnelRepository extends JpaRepository<DevisPrevisionnel, Long> {
    List<DevisPrevisionnel> findByClientId(Long clientId);

    List<DevisPrevisionnel> findByClientIdOrderByDateCreationDesc(Long clientId);

    List<DevisPrevisionnel> findByClientIdOrderByUpdatedAtDesc(Long clientId);

    long countByClientIdAndStatut(Long clientId, StatutFacturation statut);

    List<DevisPrevisionnel> findByVehiculeId(Long vehiculeId);

    List<DevisPrevisionnel> findByVehiculeIdOrderByUpdatedAtDesc(Long vehiculeId);

    Optional<DevisPrevisionnel> findByFicheAtelierId(Long ficheAtelierId);

    List<DevisPrevisionnel> findByFicheAtelierIdOrderByDateCreationDesc(Long ficheAtelierId);

    List<DevisPrevisionnel> findByFicheAtelierIdOrderByUpdatedAtDesc(Long ficheAtelierId);

    Optional<DevisPrevisionnel> findFirstByFicheAtelierIdOrderByDateCreationDesc(Long ficheAtelierId);

    Optional<DevisPrevisionnel> findFirstByFicheAtelierIdOrderByUpdatedAtDesc(Long ficheAtelierId);

    List<DevisPrevisionnel> findByOrdreReparationId(Long ordreReparationId);

    List<DevisPrevisionnel> findByOrdreReparationIdOrderByDateCreationDesc(Long ordreReparationId);

    List<DevisPrevisionnel> findByOrdreReparationIdOrderByUpdatedAtDesc(Long ordreReparationId);

    Optional<DevisPrevisionnel> findFirstByOrdreReparationIdOrderByDateCreationDesc(Long ordreReparationId);

    Optional<DevisPrevisionnel> findFirstByOrdreReparationIdOrderByUpdatedAtDesc(Long ordreReparationId);

    @Query("SELECT d FROM DevisPrevisionnel d WHERE " +
            "LOWER(d.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.notesReparation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY COALESCE(d.updatedAt, d.dateCreation) DESC, d.id DESC")
    List<DevisPrevisionnel> searchDevis(@Param("keyword") String keyword);

    @Query("SELECT d FROM DevisPrevisionnel d WHERE " +
            "LOWER(d.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.notesReparation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<DevisPrevisionnel> searchDevis(@Param("keyword") String keyword, Pageable pageable);
}
