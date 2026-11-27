package sn.oas.facturation.features.recu.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.recu.data.entity.Recu;

import java.util.Optional;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long>, JpaSpecificationExecutor<Recu> {

    Optional<Recu> findFirstByClientIdAndDateFinIsNullOrderByIdDesc(Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Recu r WHERE r.id = :id")
    Optional<Recu> findByIdForUpdate(@Param("id") Long id);
}
