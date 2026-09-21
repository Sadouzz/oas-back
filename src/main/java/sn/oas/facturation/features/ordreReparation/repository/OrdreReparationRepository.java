package sn.oas.facturation.features.ordreReparation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdreReparationRepository extends JpaRepository<OrdreReparation, Long> {

    @Query("SELECT f FROM OrdreReparation f JOIN FETCH f.vehicule v LEFT JOIN FETCH v.client ORDER BY COALESCE(f.updatedAt, f.dateCreation) DESC, f.id DESC")
    List<OrdreReparation> findAllWithVehiculeAndClient();

    @Query("SELECT f FROM OrdreReparation f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.descriptionTravaux) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.listeDefauts) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY COALESCE(f.updatedAt, f.dateCreation) DESC, f.id DESC")
    List<OrdreReparation> searchOrdresReparation(@Param("keyword") String keyword);
    List<OrdreReparation> findByVehiculeClientIdOrderByDateCreationDesc(Long clientId);
    List<OrdreReparation> findByVehiculeIdOrderByDateCreationDesc(Long vehiculeId);
    List<OrdreReparation> findByVehiculeIdAndStatut(Long vehiculeId, StatutOrdreReparation statut);
    OrdreReparation findTopByOrderByIdDesc();
    OrdreReparation findTopByNumeroStartingWithOrderByNumeroDesc(String prefix);
    boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<StatutOrdreReparation> statuts);
    long countByStatut(StatutOrdreReparation statut);
    long countByStatutNotIn(List<StatutOrdreReparation> statuts);
    List<OrdreReparation> findTop5ByOrderByIdDesc();
    boolean existsByFicheAtelierId(Long ficheAtelierId);
    Optional<OrdreReparation> findFirstByFicheAtelierId(Long ficheAtelierId);
    Optional<OrdreReparation> findFirstByVehiculeIdAndStatutNotIn(Long vehiculeId, List<StatutOrdreReparation> statuts);

    // Portail technicien : ordres où le technicien figure dans le diagnostic OU réparation.
    @Query("SELECT DISTINCT f FROM OrdreReparation f " +
            "LEFT JOIN f.diagnostic d LEFT JOIN f.techniciensReparation t2 " +
            "WHERE d.technicien.id = :technicienId OR t2.id = :technicienId " +
            "ORDER BY COALESCE(f.updatedAt, f.dateCreation) DESC, f.id DESC")
    List<OrdreReparation> findByTechnicienAssigne(@Param("technicienId") Long technicienId);

    @Query(value = "SELECT DISTINCT f FROM OrdreReparation f " +
            "LEFT JOIN f.diagnostic d LEFT JOIN f.techniciensReparation t2 " +
            "WHERE d.technicien.id = :technicienId OR t2.id = :technicienId",
           countQuery = "SELECT COUNT(DISTINCT f) FROM OrdreReparation f " +
            "LEFT JOIN f.diagnostic d LEFT JOIN f.techniciensReparation t2 " +
            "WHERE d.technicien.id = :technicienId OR t2.id = :technicienId")
    Page<OrdreReparation> findByTechnicienAssigne(@Param("technicienId") Long technicienId, Pageable pageable);

    @Query(value = "SELECT DISTINCT f FROM OrdreReparation f " +
            "LEFT JOIN f.diagnostic d LEFT JOIN f.techniciensReparation t2 " +
            "LEFT JOIN f.vehicule v " +
            "WHERE (d.technicien.id = :technicienId OR t2.id = :technicienId) AND (" +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(DISTINCT f) FROM OrdreReparation f " +
            "LEFT JOIN f.diagnostic d LEFT JOIN f.techniciensReparation t2 " +
            "LEFT JOIN f.vehicule v " +
            "WHERE (d.technicien.id = :technicienId OR t2.id = :technicienId) AND (" +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OrdreReparation> searchByTechnicienAssigne(@Param("technicienId") Long technicienId, @Param("keyword") String keyword, Pageable pageable);
}
