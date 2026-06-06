package sn.oas.facturation.proforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.proforma.data.entity.Proforma;

import java.util.List;

@Repository
public interface ProformaRepository extends JpaRepository<Proforma, Long> {

    @Query("SELECT p FROM Proforma p WHERE " +
            "LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Proforma> searchProformas(@Param("keyword") String keyword);

    List<Proforma> findTop5ByOrderByDateCreationDesc();
}
