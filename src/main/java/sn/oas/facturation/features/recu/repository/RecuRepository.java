package sn.oas.facturation.features.recu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.recu.data.entity.Recu;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {
    List<Recu> findByFactureIdOrderByDatePaiementDesc(Long factureId);
    List<Recu> findByFactureClientIdOrderByDatePaiementDesc(Long clientId);
    
    @Query("SELECT COUNT(r) FROM Recu r WHERE EXTRACT(YEAR FROM r.datePaiement) = :year")
    long countByDatePaiementYear(@Param("year") int year);
}
