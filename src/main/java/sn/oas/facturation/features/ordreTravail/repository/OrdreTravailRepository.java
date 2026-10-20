package sn.oas.facturation.features.ordreTravail.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdreTravailRepository extends JpaRepository<OrdreTravail, Long> {

    Optional<OrdreTravail> findByNumero(String numero);

    Optional<OrdreTravail> findByOrdreReparationId(Long ordreReparationId);

    Optional<OrdreTravail> findByProformaId(Long proformaId);

    List<OrdreTravail> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<OrdreTravail> findByVehiculeIdOrderByCreatedAtDesc(Long vehiculeId);

    @Query("SELECT ot FROM OrdreTravail ot " +
           "LEFT JOIN ot.client c " +
           "LEFT JOIN ot.vehicule v " +
           "LEFT JOIN ot.ordreReparation or_ " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "       LOWER(ot.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(or_.numero) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OrdreTravail> search(@Param("keyword") String keyword, Pageable pageable);
}
