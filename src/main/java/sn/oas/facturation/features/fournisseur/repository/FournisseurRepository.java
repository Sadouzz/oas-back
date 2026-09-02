package sn.oas.facturation.features.fournisseur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    @Query("SELECT f FROM Fournisseur f WHERE f.archived = false AND " +
            "(LOWER(f.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.nomEntreprise) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Fournisseur> searchFournisseurs(@Param("keyword") String keyword);
    boolean existsByMatricule(String matricule);
}
