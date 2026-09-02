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
    private Long rendezVousId;
    
    private Long clientId;
    private String clientName;
    private Long vehiculeId;
    private String vehiculeImmatriculation;
    private Long garageId;

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
}
