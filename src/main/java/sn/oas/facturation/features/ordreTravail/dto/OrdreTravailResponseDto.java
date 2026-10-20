package sn.oas.facturation.features.ordreTravail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreTravailResponseDto {
    private Long id;
    private String numero;
    private ClientDto client;
    private VehiculeDto vehicule;
    private Long proformaId;
    private String proformaNumero;
    private Long ordreReparationId;
    private String ordreReparationNumero;
    private StatutOrdreTravail statut;
    private String statutLabel;
    private Integer etape;
    private List<TechnicienDto> techniciens;
    private List<DocumentDto> bonsDeCommande;
    private List<DocumentDto> bonsDeSortie;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientDto {
        private Long id;
        private String nom;
        private String telephone;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehiculeDto {
        private Long id;
        private String immatriculation;
        private String marque;
        private String modele;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicienDto {
        private Long id;
        private String nom;
        private String prenom;
        private String specialite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDto {
        private Long id;
        private String numero;
    }

    public static OrdreTravailResponseDto from(OrdreTravail ot) {
        if (ot == null) return null;

        ClientDto clientDto = null;
        if (ot.getClient() != null) {
            String nom = (ot.getClient().getFirstName() != null ? ot.getClient().getFirstName() + " " : "")
                    + (ot.getClient().getLastName() != null ? ot.getClient().getLastName() : "");
            clientDto = ClientDto.builder()
                    .id(ot.getClient().getId())
                    .nom(nom.trim())
                    .telephone(ot.getClient().getPhone())
                    .email(ot.getClient().getEmail())
                    .build();
        }

        VehiculeDto vehiculeDto = null;
        if (ot.getVehicule() != null) {
            vehiculeDto = VehiculeDto.builder()
                    .id(ot.getVehicule().getId())
                    .immatriculation(ot.getVehicule().getImmatriculation())
                    .marque(ot.getVehicule().getMarque())
                    .modele(ot.getVehicule().getModele())
                    .build();
        }

        List<TechnicienDto> techniciens = ot.getLignesTechniciens() == null ? Collections.<TechnicienDto>emptyList() :
                ot.getLignesTechniciens().stream()
                        .filter(l -> l.getTechnicien() != null)
                        .map(l -> TechnicienDto.builder()
                                .id(l.getTechnicien().getId())
                                .nom(l.getTechnicien().getLastName())
                                .prenom(l.getTechnicien().getFirstName())
                                .specialite(l.getTechnicien().getSpecialite() != null ? l.getTechnicien().getSpecialite().name() : null)
                                .build())
                        .collect(Collectors.toList());

        List<DocumentDto> bcs = ot.getBonsDeCommande() == null ? Collections.<DocumentDto>emptyList() :
                ot.getBonsDeCommande().stream()
                        .map(bc -> DocumentDto.builder()
                                .id(bc.getId())
                                .numero(bc.getNumero())
                                .build())
                        .collect(Collectors.toList());

        List<DocumentDto> bss = ot.getBonsDeSortie() == null ? Collections.<DocumentDto>emptyList() :
                ot.getBonsDeSortie().stream()
                        .map(bs -> DocumentDto.builder()
                                .id(bs.getId())
                                .numero(bs.getReference())
                                .build())
                        .collect(Collectors.toList());

        return OrdreTravailResponseDto.builder()
                .id(ot.getId())
                .numero(ot.getNumero())
                .client(clientDto)
                .vehicule(vehiculeDto)
                .proformaId(ot.getProforma() != null ? ot.getProforma().getId() : null)
                .proformaNumero(ot.getProforma() != null ? ot.getProforma().getNumero() : null)
                .ordreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null)
                .ordreReparationNumero(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getNumero() : null)
                .statut(ot.getStatut())
                .statutLabel(ot.getStatut() != null ? ot.getStatut().getLabel() : null)
                .etape(ot.getStatut() != null ? ot.getStatut().getEtape() : null)
                .techniciens(techniciens)
                .bonsDeCommande(bcs)
                .bonsDeSortie(bss)
                .createdAt(ot.getCreatedAt())
                .updatedAt(ot.getUpdatedAt())
                .build();
    }
}
