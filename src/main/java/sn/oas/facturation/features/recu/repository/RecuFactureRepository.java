package sn.oas.facturation.features.recu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.recu.data.entity.RecuFacture;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RecuFactureRepository extends JpaRepository<RecuFacture, Long> {
    List<RecuFacture> findByFactureIdOrderByDatePaiementDesc(Long factureId);
    List<RecuFacture> findByFactureClientIdOrderByDatePaiementDesc(Long clientId);
    
    @Query("SELECT COUNT(r) FROM RecuFacture r WHERE EXTRACT(YEAR FROM r.datePaiement) = :year")
    long countByDatePaiementYear(@Param("year") int year);
}
