package sn.oas.facturation.bonDeLivraison.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.bonDeLivraison.data.entity.BonDeLivraison;

import java.util.List;

@Repository
public interface BonDeLivraisonRepository extends JpaRepository<BonDeLivraison, Long> {

    @Query("SELECT b FROM BonDeLivraison b WHERE " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BonDeLivraison> searchBonsDeLivraison(@Param("keyword") String keyword);
    
    List<BonDeLivraison> findTop5ByOrderByDateCreationDesc();
}
