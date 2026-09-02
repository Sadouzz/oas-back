package sn.oas.facturation.features.vehicule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    Optional<Vehicule> findByImmatriculation(String immatriculation);
    boolean existsByImmatriculation(String immatriculation);
    List<Vehicule> findByClientId(Long clientId);

    @Query("SELECT v FROM Vehicule v WHERE " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.numeroChassis) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vehicule> searchVehicules(@Param("keyword") String keyword);

    @Query("SELECT v FROM Vehicule v WHERE " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.numeroChassis) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Vehicule> searchVehicules(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<Vehicule> findTop5ByOrderByCreatedAtDesc();
}
