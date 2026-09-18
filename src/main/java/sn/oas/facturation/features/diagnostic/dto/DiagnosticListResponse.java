package sn.oas.facturation.features.diagnostic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticListResponse {

    private Long id;
    private Long ordreReparationId;
    private String ordreReparationNumero;

    private VehiculeSummary vehicule;

    private Long technicienId;
    private String technicienNom;
    private List<TechnicienSummary> techniciens;

    private String observations;
    private String pannesDetectees;
    private String recommandations;
    private Integer kilometrage;
    private StatutDiagnostic statut;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    private Integer nombrePiecesJointes;
    private Integer nombreRemarques;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehiculeSummary {
        private Long id;
        private String immatriculation;
        private String marque;
        private String modele;
        private ClientSummary client;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicienSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
        private String specialite;
    }
}
