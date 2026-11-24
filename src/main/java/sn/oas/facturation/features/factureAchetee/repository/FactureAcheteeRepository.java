package sn.oas.facturation.features.factureAchetee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FactureAcheteeRepository extends JpaRepository<FactureAchetee, Long> {

    Page<FactureAchetee> findByDateFactureBetweenOrderByDateFactureDescIdDesc(
            LocalDate debut, LocalDate fin, Pageable pageable);

    List<FactureAchetee> findByDateFactureBetweenOrderByDateFactureDescIdDesc(
            LocalDate debut, LocalDate fin);

    @Query("SELECT COALESCE(SUM(f.montantTva), 0) FROM FactureAchetee f "
            + "WHERE f.dateFacture >= :debut AND f.dateFacture <= :fin")
    BigDecimal sumMontantTvaBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
