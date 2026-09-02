package sn.oas.facturation.features.avoirHT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.avoirHT.data.entity.AvoirHT;

import java.util.List;

@Repository
public interface AvoirHTRepository extends JpaRepository<AvoirHT, Long> {

    @Query("SELECT a FROM AvoirHT a WHERE " +
            "LOWER(a.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AvoirHT> searchAvoirsHT(@Param("keyword") String keyword);

    List<AvoirHT> findTop5ByOrderByDateCreationDesc();
}
