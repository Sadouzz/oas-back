package sn.oas.facturation.ficheAtelier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;

import java.util.List;

@Repository
public interface FicheAtelierRepository extends JpaRepository<FicheAtelier, Long> {

    @Query("SELECT f FROM FicheAtelier f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.descriptionTravaux) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.listeReception) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.listeDefauts) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FicheAtelier> searchFichesAtelier(@Param("keyword") String keyword);
    List<FicheAtelier> findByVehiculeClientIdOrderByDateCreationDesc(Long clientId);
}
