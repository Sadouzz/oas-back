package sn.oas.facturation.features.bonDeLivraison.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonListRow;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonSortieReference;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BonDeLivraisonRepository extends JpaRepository<BonDeLivraison, Long> {
    Optional<BonDeLivraison> findByOrdreReparationId(Long ordreReparationId);
    boolean existsByOrdreReparationId(Long ordreReparationId);

    @Query(value = "SELECT new sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonListRow(" +
            "b.id, b.numero, b.dateCreation, o.id, o.numero, c.id, " +
            "CONCAT(COALESCE(c.firstName, ''), CONCAT(' ', COALESCE(c.lastName, ''))), " +
            "v.id, v.immatriculation, b.tvaApplicable, b.montantHT, b.montantTVA, b.montantTTC, b.remarque, " +
            "f.id, f.numero, n.id, n.numero) " +
            "FROM BonDeLivraison b LEFT JOIN b.ordreReparation o LEFT JOIN b.client c LEFT JOIN b.vehicule v " +
            "LEFT JOIN b.facture f LEFT JOIN b.noteDePrix n",
            countQuery = "SELECT COUNT(b) FROM BonDeLivraison b")
    Page<BonDeLivraisonListRow> findListRows(Pageable pageable);

    @Query(value = "SELECT new sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonListRow(" +
            "b.id, b.numero, b.dateCreation, o.id, o.numero, c.id, " +
            "CONCAT(COALESCE(c.firstName, ''), CONCAT(' ', COALESCE(c.lastName, ''))), " +
            "v.id, v.immatriculation, b.tvaApplicable, b.montantHT, b.montantTVA, b.montantTTC, b.remarque, " +
            "f.id, f.numero, n.id, n.numero) " +
            "FROM BonDeLivraison b LEFT JOIN b.ordreReparation o LEFT JOIN b.client c LEFT JOIN b.vehicule v " +
            "LEFT JOIN b.facture f LEFT JOIN b.noteDePrix n WHERE " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(DISTINCT b) FROM BonDeLivraison b LEFT JOIN b.ordreReparation o LEFT JOIN b.client c LEFT JOIN b.vehicule v WHERE " +
                    "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BonDeLivraisonListRow> searchListRows(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonSortieReference(" +
            "b.id, bs.id, bs.reference) FROM BonDeLivraison b JOIN b.bonsDeSortie bs " +
            "WHERE b.id IN :bonIds ORDER BY b.id, bs.reference")
    java.util.List<BonDeLivraisonSortieReference> findSortieReferences(@Param("bonIds") java.util.Collection<Long> bonIds);

    @Query(value = "SELECT DISTINCT b FROM BonDeLivraison b LEFT JOIN b.ordreReparation o LEFT JOIN b.client c LEFT JOIN b.vehicule v WHERE " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(DISTINCT b) FROM BonDeLivraison b LEFT JOIN b.ordreReparation o LEFT JOIN b.client c LEFT JOIN b.vehicule v WHERE " +
                    "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BonDeLivraison> search(@Param("keyword") String keyword, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BonDeLivraison b WHERE b.id = :id")
    Optional<BonDeLivraison> findByIdForConversion(@Param("id") Long id);
}
