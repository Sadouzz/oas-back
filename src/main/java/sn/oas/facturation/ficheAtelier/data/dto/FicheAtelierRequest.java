package sn.oas.facturation.ficheAtelier.data.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.ficheAtelier.data.entity.LigneReception;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheAtelierRequest {

    private Long rendezVousId;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le véhicule est obligatoire")
    private Long vehiculeId;

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
}
