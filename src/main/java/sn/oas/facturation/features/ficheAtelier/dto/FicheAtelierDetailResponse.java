package sn.oas.facturation.features.ficheAtelier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<<< HEAD:src/main/java/sn/oas/facturation/features/ficheAtelier/dto/FicheAtelierDetailsResponse.java
public class FicheAtelierDetailsResponse {
========
public class FicheAtelierDetailResponse {
>>>>>>>> df01ca9ef4b4992bd227f8c230c4b61f793d83f2:src/main/java/sn/oas/facturation/features/ficheAtelier/dto/FicheAtelierDetailResponse.java

    private Long id;
    private String numero;
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
    private DevisPrevisionnelSummary devisPrevisionnel;

<<<<<<<< HEAD:src/main/java/sn/oas/facturation/features/ficheAtelier/dto/FicheAtelierDetailsResponse.java
    public record DevisPrevisionnelSummary(
            Long id,
            String numero,
            BigDecimal montantTotal,
            StatutFacturation statut,
            String notesReparation,
            LocalDateTime dateCreation
    ) {
        public static DevisPrevisionnelSummary from(DevisPrevisionnel devis) {
            if (devis == null) return null;
            return new DevisPrevisionnelSummary(
                    devis.getId(),
                    devis.getNumero(),
                    devis.getMontantTotal(),
                    devis.getStatut(),
                    devis.getNotesReparation(),
                    devis.getDateCreation()
            );
        }
    }

    public static FicheAtelierDetailsResponse from(FicheAtelier fiche) {
        if (fiche == null)
            return null;
        return FicheAtelierDetailsResponse.builder()
========
    public static FicheAtelierDetailResponse from(
            sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier fiche) {
        return from(fiche, false);
    }

    public static FicheAtelierDetailResponse from(
            sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier fiche,
            boolean hasOrdreReparation) {
        if (fiche == null)
            return null;
        return FicheAtelierDetailResponse.builder()
>>>>>>>> df01ca9ef4b4992bd227f8c230c4b61f793d83f2:src/main/java/sn/oas/facturation/features/ficheAtelier/dto/FicheAtelierDetailResponse.java
                .id(fiche.getId())
                .numero(fiche.getNumero())
                .clientName(fiche.getClient() != null
                        ? (fiche.getClient().getFirstName() + " " + fiche.getClient().getLastName()).trim()
                        : null)
                .vehiculeImmatriculation(fiche.getVehicule() != null ? fiche.getVehicule().getImmatriculation() : null)
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
                .hasOrdreReparation(fiche.getOrdreReparation() != null)
                .devisPrevisionnel(DevisPrevisionnelSummary.from(fiche.getDevisPrevisionnel()))
                .build();
    }
}
