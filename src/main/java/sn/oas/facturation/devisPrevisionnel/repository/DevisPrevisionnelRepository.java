package sn.oas.facturation.devisPrevisionnel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel;

import java.util.List;

@Repository
public interface DevisPrevisionnelRepository extends JpaRepository<DevisPrevisionnel, Long> {
    List<DevisPrevisionnel> findByClientId(Long clientId);

    List<DevisPrevisionnel> findByClientIdOrderByDateCreationDesc(Long clientId);

    List<DevisPrevisionnel> findByVehiculeId(Long vehiculeId);

    java.util.Optional<DevisPrevisionnel> findByFicheAtelierId(Long ficheAtelierId);

    @Query("SELECT d FROM DevisPrevisionnel d WHERE " +
            "LOWER(d.notesReparation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DevisPrevisionnel> searchDevis(@Param("keyword") String keyword);
}
