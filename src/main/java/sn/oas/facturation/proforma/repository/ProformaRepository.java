package sn.oas.facturation.proforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.proforma.data.entity.Proforma;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProformaRepository extends JpaRepository<Proforma, Long> {

    @Query("SELECT p FROM Proforma p WHERE " +
            "LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.bonDeCommande.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ficheAtelier.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ficheAtelier.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ficheAtelier.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ficheAtelier.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ficheAtelier.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Proforma> searchProformas(@Param("keyword") String keyword);

    List<Proforma> findTop5ByOrderByDateCreationDesc();

    Optional<Proforma> findByFicheAtelierId(Long ficheAtelierId);

    @Query("SELECT p FROM Proforma p WHERE p.ficheAtelier.vehicule.client.id = :clientId ORDER BY p.dateCreation DESC")
    List<Proforma> findByClientId(@Param("clientId") Long clientId);
}
