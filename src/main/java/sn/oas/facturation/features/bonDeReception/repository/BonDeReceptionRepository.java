package sn.oas.facturation.features.bonDeReception.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;

import java.util.List;

@Repository
public interface BonDeReceptionRepository extends JpaRepository<BonDeReception, Long> {

    @Query(value = "SELECT b FROM BonDeReception b " +
            "LEFT JOIN b.bonDeCommande bc " +
            "LEFT JOIN bc.vehicule bcv " +
            "LEFT JOIN bcv.client bcc " +
            "LEFT JOIN bc.fournisseur bcf " +
            "LEFT JOIN b.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bc.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(bcc.firstName, ' ', bcc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nomEntreprise) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BonDeReception> searchBonsDeReception(@Param("keyword") String keyword);

    @Query(value = "SELECT b FROM BonDeReception b " +
            "LEFT JOIN b.bonDeCommande bc " +
            "LEFT JOIN bc.vehicule bcv " +
            "LEFT JOIN bcv.client bcc " +
            "LEFT JOIN bc.fournisseur bcf " +
            "LEFT JOIN b.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bc.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(bcc.firstName, ' ', bcc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nomEntreprise) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(b) FROM BonDeReception b " +
            "LEFT JOIN b.bonDeCommande bc " +
            "LEFT JOIN bc.vehicule bcv " +
            "LEFT JOIN bcv.client bcc " +
            "LEFT JOIN bc.fournisseur bcf " +
            "LEFT JOIN b.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bc.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcv.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(bcc.firstName, ' ', bcc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcc.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bcf.nomEntreprise) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<BonDeReception> searchBonsDeReception(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<BonDeReception> findTop5ByOrderByDateCreationDesc();
}
