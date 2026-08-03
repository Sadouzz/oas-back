package sn.oas.facturation.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.marketplace.data.entity.Produit;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByArchiveFalseAndDisponibleTrue();

    List<Produit> findByArchiveTrue();

    long countByArchiveFalse();

    long countByArchiveFalseAndDisponibleTrue();

    long countByArchiveTrue();

    @Query("SELECT p FROM Produit p WHERE p.archive = false AND p.disponible = true AND " +
            "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Produit> searchDisponibles(@Param("keyword") String keyword);

    List<Produit> findTop5ByArchiveFalseAndDisponibleTrueOrderByIdDesc();
}
