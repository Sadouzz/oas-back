package sn.oas.facturation.features.ficheAtelier.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheAtelierResponse {

    private Long id;
    // private Long rendezVousId;

    // private Long clientId;
    private String clientName;
    // private Long vehiculeId;
    private String vehiculeImmatriculation;
    // private Long garageId;

    private String nomChauffeur;
    private String telephoneChauffeur;
    private String niveauEssence;
    private Integer kilometrage;
    private String designationTravaux;

    private List<LigneReception> lignesReception;
    private List<LigneDefaut> lignesDefauts;

    private String nb;
    private LocalDateTime dateSortiePrevue;
    private String garantie;
    private String signatureReceptionnaireBase64;
    private String signatureBase64;
    private String signatureSortieBase64;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean hasOrdreReparation;

    public static FicheAtelierResponse from(sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier fiche) {
        return from(fiche, false);
    }

    public static FicheAtelierResponse from(sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier fiche,
            boolean hasOrdreReparation) {
        if (fiche == null)
            return null;
        return FicheAtelierResponse.builder()
                .id(fiche.getId())
                // .rendezVousId(fiche.getRendezVous() != null ? fiche.getRendezVous().getId() :
                // null)
                // .clientId(fiche.getClient() != null ? fiche.getClient().getId() : null)
                .clientName(fiche.getClient() != null
                        ? (fiche.getClient().getFirstName() + " " + fiche.getClient().getLastName()).trim()
                        : null)
                // .vehiculeId(fiche.getVehicule() != null ? fiche.getVehicule().getId() : null)
                .vehiculeImmatriculation(fiche.getVehicule() != null ? fiche.getVehicule().getImmatriculation() : null)
                // .garageId(fiche.getGarage() != null ? fiche.getGarage().getId() : null)
                .nomChauffeur(fiche.getNomChauffeur())
                .telephoneChauffeur(fiche.getTelephoneChauffeur())
                .niveauEssence(fiche.getNiveauEssence())
                .kilometrage(fiche.getKilometrage())
                .designationTravaux(fiche.getDesignationTravaux())
                .lignesReception(fiche.getLignesReception())
                .lignesDefauts(fiche.getLignesDefauts())
                .nb(fiche.getNb())
                .dateSortiePrevue(fiche.getDateSortiePrevue())
                .garantie(fiche.getGarantie())
                .signatureReceptionnaireBase64(fiche.getSignatureReceptionnaireBase64())
                .signatureBase64(fiche.getSignatureBase64())
                .signatureSortieBase64(fiche.getSignatureSortieBase64())
                .createdAt(fiche.getCreatedAt())
                .updatedAt(fiche.getUpdatedAt())
                .hasOrdreReparation(hasOrdreReparation)
                .build();
    }
}
