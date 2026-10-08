package sn.oas.facturation.features.pdfTemplate.data.dto;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public record PdfTemplateWriteRequest(@NotBlank String name, @NotBlank String documentType, boolean active,
                                      List<PdfBlockRequest> blocks, String layoutKey, List<String> documentTypes) {
    public PdfTemplateWriteRequest(String name, String documentType, boolean active, List<PdfBlockRequest> blocks, String layoutKey) {
        this(name, documentType, active, blocks, layoutKey, null);
    }
    public PdfTemplateWriteRequest(String name, String documentType, boolean active, List<PdfBlockRequest> blocks) {
        this(name, documentType, active, blocks, null, null);
    }
}
