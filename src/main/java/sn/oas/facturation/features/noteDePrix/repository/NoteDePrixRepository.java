package sn.oas.facturation.features.noteDePrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;

import java.util.List;

@Repository
public interface NoteDePrixRepository extends JpaRepository<NoteDePrix, Long> {

    @Query(value = "SELECT n FROM NoteDePrix n " +
            "LEFT JOIN n.client c " +
            "LEFT JOIN n.vehicule v " +
            "LEFT JOIN n.ordreReparation o " +
            "WHERE LOWER(n.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.modePaiement) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(n) FROM NoteDePrix n " +
            "LEFT JOIN n.client c " +
            "LEFT JOIN n.vehicule v " +
            "LEFT JOIN n.ordreReparation o " +
            "WHERE LOWER(n.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.modePaiement) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<NoteDePrix> searchNotesDePrix(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM NoteDePrix n " +
            "LEFT JOIN n.client c " +
            "LEFT JOIN n.vehicule v " +
            "LEFT JOIN n.ordreReparation o " +
            "WHERE LOWER(n.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.modePaiement) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.numero) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NoteDePrix> searchNotesDePrix(@Param("keyword") String keyword);

    List<NoteDePrix> findTop5ByOrderByDateCreationDesc();
}
