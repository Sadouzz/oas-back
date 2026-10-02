package sn.oas.facturation.features.ficheAtelier.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonAlias({"reception", "lignesReceptions", "checklist"})
    private List<LigneReception> lignesReception;

    @JsonAlias({"defautsConstates", "defauts", "defautsCarrosserie", "rubriquesDefauts", "lignesDefaut"})
    private List<LigneDefaut> lignesDefauts;

    private String nb;
    private LocalDateTime dateSortiePrevue;
    private String garantie;
    private String signatureReceptionnaireBase64;
    private String signatureBase64;

    @JsonSetter("defautsConstates")
    public void setDefautsConstates(List<LigneDefaut> defautsConstates) {
        if (this.lignesDefauts == null || this.lignesDefauts.isEmpty()) {
            this.lignesDefauts = defautsConstates;
        }
    }

    @JsonSetter("reception")
    public void setReception(List<LigneReception> reception) {
        if (this.lignesReception == null || this.lignesReception.isEmpty()) {
            this.lignesReception = reception;
        }
    }
}
