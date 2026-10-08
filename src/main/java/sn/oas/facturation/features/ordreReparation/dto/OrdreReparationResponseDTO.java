package sn.oas.facturation.features.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreReparationResponseDTO {
    private Long id;
    private String numero;
    private String descriptionTravaux;
    private List<LigneTravailOrdre> lignesTravaux;
    private List<LigneReceptionOrdre> lignesReception;
    private String listeDefauts;
    private LocalDateTime dateCreation;
    private LocalDateTime updatedAt;
    private LocalDateTime dateSortie;
    private LocalDateTime dateRestitution;
    private Integer garantieMois;
    private Long ficheAtelierId;
    private StatutOrdreReparation statut;
    
    private VehiculeDto vehicule;
    private DiagnosticDto diagnostic;
    private List<TechnicienDto> techniciens;
    private List<TechnicienDto> techniciensReparation;
    private BonDeSortieDto bonDeSortie;
    private List<LigneOrdreReparationPieceDto> lignesOrdreReparationPieces;
    private List<LigneOrdreReparationMainDoeuvreDto> lignesOrdreReparationMainDoeuvres;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehiculeDto {
        private Long id;
        private String immatriculation;
        private String marque;
        private String modele;
        private Integer kilometrage;
        private ClientDto client;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicienDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String specialite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BonDeSortieDto {
        private Long id;
        private String reference;
        private String statut;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneOrdreReparationPieceDto {
        private Long id;
        private PieceDto piece;
        private Boolean isCustom;
        private String designationPds;
        private Integer quantite;
        private Double prix;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceDto {
        private Long id;
        private String reference;
        private String designation;
        private Double prix;
        private String type;
        private Integer stockMagasin;
        private Integer stockAtelier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneOrdreReparationMainDoeuvreDto {
        private Long id;
        private MainDoeuvreDto mainDoeuvre;
        private Integer nbreHeure;
        private Double prix;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainDoeuvreDto {
        private Long id;
        private Double prix;
        private Integer nbreHeure;
        private String description;
        private CategorieDto categorie;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorieDto {
        private String nom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticDto {
        private Long id;
    }

}
