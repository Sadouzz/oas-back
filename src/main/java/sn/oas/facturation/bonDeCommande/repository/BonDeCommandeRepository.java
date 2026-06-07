package sn.oas.facturation.bonDeCommande.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonDeCommandeRepository extends JpaRepository<BonDeCommande, Long> {

    @Query("""
            SELECT bc FROM BonDeCommande bc
            WHERE LOWER(bc.numero) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(bc.observation) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<BonDeCommande> searchBonsDeCommande(@Param("keyword") String keyword);

    Optional<BonDeCommande> findByNumero(String numero);

    List<BonDeCommande> findByFournisseurId(Long fournisseurId);

    List<BonDeCommande> findByVehiculeId(Long vehiculeId);

    List<BonDeCommande> findTop5ByOrderByDateCommandeDesc();
}