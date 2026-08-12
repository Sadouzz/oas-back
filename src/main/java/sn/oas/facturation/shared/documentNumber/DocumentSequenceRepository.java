package sn.oas.facturation.shared.documentNumber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DocumentSequence s WHERE s.garage.id = :garageId AND s.documentType = :documentType AND s.year = :year")
    Optional<DocumentSequence> findByGarageIdAndDocumentTypeAndYearWithLock(
            @Param("garageId") Long garageId, 
            @Param("documentType") DocumentType documentType, 
            @Param("year") int year);
}
