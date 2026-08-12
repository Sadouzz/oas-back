package sn.oas.facturation.avoirTTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.avoirTTC.data.entity.AvoirTTC;

import java.util.List;

@Repository
public interface AvoirTTCRepository extends JpaRepository<AvoirTTC, Long> {

    @Query("SELECT a FROM AvoirTTC a WHERE " +
            "LOWER(a.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AvoirTTC> searchAvoirsTTC(@Param("keyword") String keyword);

    List<AvoirTTC> findTop5ByOrderByDateCreationDesc();
}
