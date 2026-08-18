package sn.oas.facturation.ordreReparation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;

import java.util.List;

@Repository
public interface OrdreReparationRepository extends JpaRepository<OrdreReparation, Long> {

    @Query("SELECT f FROM OrdreReparation f JOIN FETCH f.vehicule v LEFT JOIN FETCH v.client ORDER BY f.id DESC")
    List<OrdreReparation> findAllWithVehiculeAndClient();

    @Query("SELECT f FROM OrdreReparation f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.descriptionTravaux) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.listeDefauts) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<OrdreReparation> searchOrdresReparation(@Param("keyword") String keyword);
    List<OrdreReparation> findByVehiculeClientIdOrderByDateCreationDesc(Long clientId);
    List<OrdreReparation> findByVehiculeIdAndStatut(Long vehiculeId, sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation statut);
    OrdreReparation findTopByOrderByIdDesc();
    OrdreReparation findTopByNumeroStartingWithOrderByNumeroDesc(String prefix);
    boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation> statuts);
    boolean existsByFicheAtelierId(Long ficheAtelierId);

    // Portail technicien : ordres où le technicien figure dans le pool diagnostic OU réparation.
    @Query("SELECT DISTINCT f FROM OrdreReparation f " +
            "LEFT JOIN f.techniciens t1 LEFT JOIN f.techniciensReparation t2 " +
            "WHERE t1.id = :technicienId OR t2.id = :technicienId " +
            "ORDER BY f.dateCreation DESC")
    List<OrdreReparation> findByTechnicienAssigne(@Param("technicienId") Long technicienId);
}
