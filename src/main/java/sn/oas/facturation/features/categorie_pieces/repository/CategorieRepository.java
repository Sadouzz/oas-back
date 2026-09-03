package sn.oas.facturation.features.categorie_pieces.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByNom(String nom);
    List<Categorie> findByDepotId(Long depotId);
    Page<Categorie> findByDepotId(Long depotId, Pageable pageable);
    boolean existsByNom(String nom);

    @Query("SELECT c FROM Categorie c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Categorie> searchCategories(@Param("keyword") String keyword);

    @Query("SELECT c FROM Categorie c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Categorie> searchCategories(@Param("keyword") String keyword, Pageable pageable);
}
