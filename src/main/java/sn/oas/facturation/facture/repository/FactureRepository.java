package sn.oas.facturation.facture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.facture.data.entity.Facture;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    @Query("SELECT f FROM Facture f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Facture> searchFactures(@Param("keyword") String keyword);

    List<Facture> findTop5ByOrderByDateCreationDesc();
}
