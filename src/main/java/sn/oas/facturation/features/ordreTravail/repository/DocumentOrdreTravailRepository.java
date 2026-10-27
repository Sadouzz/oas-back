package sn.oas.facturation.features.ordreTravail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.ordreTravail.data.entity.DocumentOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;

import java.util.List;

@Repository
public interface DocumentOrdreTravailRepository extends JpaRepository<DocumentOrdreTravail, Long> {
    List<DocumentOrdreTravail> findByOrdreTravailIdOrderByCreatedAtDesc(Long ordreTravailId);
    List<DocumentOrdreTravail> findByOrdreTravailIdAndTypeOrderByCreatedAtDesc(Long ordreTravailId, TypeDocumentOt type);
    List<DocumentOrdreTravail> findByOrdreTravailIdAndEtapeOrderByCreatedAtDesc(Long ordreTravailId, Integer etape);
    List<DocumentOrdreTravail> findByOrdreTravailIdAndEtapeAndTypeOrderByCreatedAtDesc(Long ordreTravailId, Integer etape, TypeDocumentOt type);
}

