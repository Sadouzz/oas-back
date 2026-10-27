package sn.oas.facturation.features.ordreTravail.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents_ordre_travail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOrdreTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_travail_id", nullable = false)
    @JsonIgnoreProperties({ "documents", "bonsDeCommande", "bonsDeSortie", "lignesTechniciens", "garage" })
    private OrdreTravail ordreTravail;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TypeDocumentOt type = TypeDocumentOt.DOCUMENT;

    @Column(name = "taille")
    private Long taille;

    @Column(columnDefinition = "TEXT")
    private String remarque;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "etape")
    @Builder.Default
    private Integer etape = 4; // Par défaut : Étape 4 Réparation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id")
    @JsonIgnoreProperties({ "password", "authorities", "garage" })
    private Technicien technicien;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.etape == null) {
            this.etape = 4;
        }
        if (this.type == null) {
            this.type = TypeDocumentOt.DOCUMENT;
        }
    }
}

