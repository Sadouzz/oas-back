package sn.oas.facturation.features.pdfTemplate.data.dto;
import sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
public record PdfTemplateResponse(Long id, String name, String documentType, Integer version, boolean active,
        List<PdfBlockRequest> blocks, LocalDateTime createdAt, String layoutKey, Set<String> documentTypes) {
    public static PdfTemplateResponse from(PdfTemplate template, List<PdfBlockRequest> blocks) {
        return new PdfTemplateResponse(template.getId(), template.getName(), template.getDocumentType(), template.getVersion(),
                template.isActive(), blocks, template.getCreatedAt(), template.getLayoutKey(),
                template.getAssignedDocumentTypes() == null || template.getAssignedDocumentTypes().isEmpty()
                        ? Set.of(template.getDocumentType()) : Set.copyOf(template.getAssignedDocumentTypes()));
    }
}
