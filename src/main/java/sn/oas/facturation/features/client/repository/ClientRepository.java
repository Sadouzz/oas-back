package sn.oas.facturation.features.client.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import sn.oas.facturation.features.client.data.entity.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Client c WHERE c.id = :id")
    java.util.Optional<Client> findByIdForUpdate(@Param("id") Long id);

    boolean existsByNumeroEntrepriseIgnoreCaseAndIdNot(String numeroEntreprise, Long id);
    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Client> searchClients(@Param("keyword") String keyword);

    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Client> searchClients(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<Client> findTop5ByOrderByCreatedAtDesc();

    org.springframework.data.domain.Page<Client> findByEnabled(boolean enabled, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT MAX(c.matricule) FROM Client c WHERE c.matricule LIKE 'CLT-%'")
    String findMaxClientMatricule();
}
