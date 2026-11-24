package sn.oas.facturation.features.avoirTvaAchetee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvoirTvaAcheteeRepository extends JpaRepository<AvoirTvaAchetee, Long> {

    Page<AvoirTvaAchetee> findByDateOperationBetweenOrderByDateOperationDescIdDesc(
            LocalDate debut, LocalDate fin, Pageable pageable);

    List<AvoirTvaAchetee> findByDateOperationBetweenOrderByDateOperationDescIdDesc(
            LocalDate debut, LocalDate fin);

    @Query("SELECT COALESCE(SUM(a.montantTva), 0) FROM AvoirTvaAchetee a "
            + "WHERE a.dateOperation >= :debut AND a.dateOperation <= :fin")
    BigDecimal sumMontantTvaBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
