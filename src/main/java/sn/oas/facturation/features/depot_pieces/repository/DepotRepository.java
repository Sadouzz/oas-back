package sn.oas.facturation.features.depot_pieces.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    Optional<Depot> findFirstByNom(String nom);
    Optional<Depot> findFirstByNomIgnoreCase(String nom);
    Optional<Depot> findFirstByGarageIdAndNomIgnoreCase(Long garageId, String nom);

    default Optional<Depot> findByNom(String nom) {
        return findFirstByNomIgnoreCase(nom);
    }
    boolean existsByNom(String nom);

    @Query("SELECT d FROM Depot d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (d.description IS NOT NULL AND LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Depot> searchDepots(@Param("keyword") String keyword);

    @Query("SELECT d FROM Depot d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (d.description IS NOT NULL AND LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Depot> searchDepots(@Param("keyword") String keyword, Pageable pageable);
}
