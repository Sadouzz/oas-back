package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreTravail.data.entity.DocumentOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOrdreTravailResponseDto {
    private Long id;
    private Long ordreTravailId;
    private String nomFichier;
    private String url;
    private TypeDocumentOt type;
    private Long taille;
    private String remarque;
    private String publicId;
    private Integer etape;
    private Long technicienId;
    private String technicienNom;
    private LocalDateTime createdAt;

    public static DocumentOrdreTravailResponseDto from(DocumentOrdreTravail doc) {
        if (doc == null) {
            return null;
        }
        String techNom = null;
        Long techId = null;
        if (doc.getTechnicien() != null) {
            techId = doc.getTechnicien().getId();
            String prenom = doc.getTechnicien().getFirstName() != null ? doc.getTechnicien().getFirstName() + " " : "";
            String nom = doc.getTechnicien().getLastName() != null ? doc.getTechnicien().getLastName() : "";
            techNom = (prenom + nom).trim();
        }
        return DocumentOrdreTravailResponseDto.builder()
                .id(doc.getId())
                .ordreTravailId(doc.getOrdreTravail() != null ? doc.getOrdreTravail().getId() : null)
                .nomFichier(doc.getNomFichier())
                .url(doc.getUrl())
                .type(doc.getType())
                .taille(doc.getTaille())
                .remarque(doc.getRemarque())
                .publicId(doc.getPublicId())
                .etape(doc.getEtape())
                .technicienId(techId)
                .technicienNom(techNom)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}

