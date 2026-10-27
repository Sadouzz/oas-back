package sn.oas.facturation.features.ordreTravail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOrdreTravailRequestDto {
    private String nomFichier;
    private String url;
    private TypeDocumentOt type; // PHOTO, PDF, DOCUMENT, AUTRE
    private Long taille;
    private String remarque;
    private String publicId;
    private Integer etape;
    private Long technicienId;
}

