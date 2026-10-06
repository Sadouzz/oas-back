package sn.oas.facturation.features.pdfTemplate.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate;
import java.util.List;
import java.util.Optional;
public interface PdfTemplateRepository extends JpaRepository<PdfTemplate, Long> {
    @Query("select distinct t from PdfTemplate t where (:documentType member of t.assignedDocumentTypes) or (t.assignedDocumentTypes is empty and t.documentType = :documentType) order by t.name asc, t.version desc")
    List<PdfTemplate> findAssignedToDocumentType(@Param("documentType") String documentType);
    List<PdfTemplate> findAllByOrderByDocumentTypeAscNameAscVersionDesc();
    Optional<PdfTemplate> findFirstByDocumentTypeAndNameOrderByVersionDesc(String documentType, String name);
    Optional<PdfTemplate> findFirstByDocumentTypeAndActiveTrueOrderByVersionDesc(String documentType);
    @Query("select distinct t from PdfTemplate t where ((:documentType member of t.assignedDocumentTypes) or (t.assignedDocumentTypes is empty and t.documentType = :documentType)) and (:documentType <> 'FACTURE' or coalesce(t.layoutKey, 'AVEC_ENTETE') = :layoutKey) and t.active = true order by t.name asc, t.version desc")
    List<PdfTemplate> findActiveForLayout(@Param("documentType") String documentType, @Param("layoutKey") String layoutKey);
    @Query("select distinct t from PdfTemplate t where ((:documentType member of t.assignedDocumentTypes) or (t.assignedDocumentTypes is empty and t.documentType = :documentType)) and (:documentType <> 'FACTURE' or coalesce(t.layoutKey, 'AVEC_ENTETE') = :layoutKey) and t.active = true order by t.version desc, t.createdAt desc, t.id desc")
    List<PdfTemplate> findActiveForLayoutByVersion(@Param("documentType") String documentType, @Param("layoutKey") String layoutKey);
    @Modifying
    @Query("update PdfTemplate t set t.active = false where ((:documentType member of t.assignedDocumentTypes) or (t.assignedDocumentTypes is empty and t.documentType = :documentType)) and t.active = true")
    int deactivateByDocumentType(@Param("documentType") String documentType);
    @Modifying
    @Query("update PdfTemplate t set t.active = false where t.documentType = :documentType and t.layoutKey = :layoutKey and t.active = true")
    int deactivateByDocumentTypeAndLayoutKey(@Param("documentType") String documentType, @Param("layoutKey") String layoutKey);
    @Modifying
    @Query("update PdfTemplate t set t.active = false where ((:documentType member of t.assignedDocumentTypes) or (t.assignedDocumentTypes is empty and t.documentType = :documentType)) and t.name = :name and coalesce(t.layoutKey, 'AVEC_ENTETE') = :layoutKey and t.active = true")
    int deactivateFamily(@Param("documentType") String documentType, @Param("name") String name, @Param("layoutKey") String layoutKey);
}
