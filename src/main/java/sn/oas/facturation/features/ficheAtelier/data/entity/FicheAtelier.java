package sn.oas.facturation.features.ficheAtelier.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fiches_atelier")
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheAtelier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id", nullable = true)
    private RendezVous rendezVous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "nom_chauffeur")
    private String nomChauffeur;

    @Column(name = "telephone_chauffeur")
    private String telephoneChauffeur;

    @Column(name = "niveau_essence")
    private String niveauEssence; // 1/4, 1/2, 3/4, Full

    @Column(name = "kilometrage")
    private Integer kilometrage;

    @Column(name = "designation_travaux", columnDefinition = "TEXT")
    private String designationTravaux;

    // JSON array of dynamic lines for "Réception" (radio/boolean equivalent)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lignes_reception", columnDefinition = "jsonb")
    private List<LigneReception> lignesReception;

    // JSON array of dynamic lines for "Défauts" (checkbox equivalent)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lignes_defauts", columnDefinition = "jsonb")
    private List<LigneDefaut> lignesDefauts;

    @Column(columnDefinition = "TEXT")
    private String nb; // Note générale

    @Column(name = "date_sortie_prevue")
    private LocalDateTime dateSortiePrevue;

    private String garantie; // ex: "1 mois après livraison"

    @Column(name = "signature_receptionnaire_base64", columnDefinition = "TEXT")
    private String signatureReceptionnaireBase64;

    @Column(name = "signature_base64", columnDefinition = "TEXT")
    private String signatureBase64; // Signature Client

    @Column(name = "signature_sortie_base64", columnDefinition = "TEXT")
    private String signatureSortieBase64;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
