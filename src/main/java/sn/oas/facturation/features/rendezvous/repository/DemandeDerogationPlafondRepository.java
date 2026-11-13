package sn.oas.facturation.features.rendezvous.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.oas.facturation.features.rendezvous.data.entity.DemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;

import java.util.List;
import java.util.Optional;

public interface DemandeDerogationPlafondRepository extends JpaRepository<DemandeDerogationPlafond, Long> {
    Optional<DemandeDerogationPlafond> findFirstByClientIdAndStatutOrderByDateDemandeDesc(
            Long clientId, StatutDemandeDerogationPlafond statut);

    @EntityGraph(attributePaths = {"client", "vehicule", "garage"})
    Page<DemandeDerogationPlafond> findAllByStatutOrderByDateDemandeAsc(
            StatutDemandeDerogationPlafond statut, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DemandeDerogationPlafond d where d.id = :id")
    Optional<DemandeDerogationPlafond> findByIdForUpdate(@Param("id") Long id);
}
