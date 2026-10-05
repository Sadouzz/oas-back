package sn.oas.facturation.features.pdfTemplate.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.user.data.entity.User;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pdf_templates", indexes = {
        @Index(name = "idx_pdf_template_type_active", columnList = "document_type, active"),
        @Index(name = "idx_pdf_template_type_layout", columnList = "document_type, layout_key, active"),
        @Index(name = "idx_pdf_template_name_version", columnList = "template_name, version")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PdfTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "template_name", nullable = false, length = 120) private String name;
    @Column(name = "document_type", nullable = false, length = 60) private String documentType;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pdf_template_document_types", joinColumns = @JoinColumn(name = "pdf_template_id"),
            indexes = @Index(name = "idx_pdf_template_doc_types", columnList = "document_type, pdf_template_id"))
    @Column(name = "document_type", nullable = false, length = 60)
    @Builder.Default private Set<String> assignedDocumentTypes = new LinkedHashSet<>();
    @Column(name = "layout_key", length = 40) @Builder.Default private String layoutKey = "AVEC_ENTETE";
    @Column(nullable = false) private Integer version;
    @Column(nullable = false) @Builder.Default private boolean active = false;
    @Column(name = "blocks_json", nullable = false, columnDefinition = "text") private String blocksJson;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_user_id") private User createdBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
