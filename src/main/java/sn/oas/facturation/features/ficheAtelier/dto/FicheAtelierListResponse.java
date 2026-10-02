package sn.oas.facturation.features.ficheAtelier.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FicheAtelierListResponse(
        Long id,
        String numero,
        String clientName,
        String vehiculeImmatriculation,
        String nomChauffeur,
        List<LigneReception> lignesReception,
        List<LigneDefaut> lignesDefauts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean hasOrdreReparation) {

    @JsonProperty("defautsConstates")
    public List<LigneDefaut> getDefautsConstates() {
        return this.lignesDefauts;
    }

    @JsonProperty("reception")
    public List<LigneReception> getReception() {
        return this.lignesReception;
    }

    public static FicheAtelierListResponse from(FicheAtelier f) {
        if (f == null)
            return null;

        String clientName = null;
        if (f.getClient() != null) {
            String fn = f.getClient().getFirstName() != null ? f.getClient().getFirstName() : "";
            String ln = f.getClient().getLastName() != null ? f.getClient().getLastName() : "";
            clientName = (fn + " " + ln).trim();
        }

        return FicheAtelierListResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .clientName(clientName)
                .vehiculeImmatriculation(f.getVehicule() != null ? f.getVehicule().getImmatriculation() : null)
                .nomChauffeur(f.getNomChauffeur())
                .lignesReception(f.getLignesReception())
                .lignesDefauts(f.getLignesDefauts())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .hasOrdreReparation(f.getOrdreReparation() != null)
                .build();
    }
}
