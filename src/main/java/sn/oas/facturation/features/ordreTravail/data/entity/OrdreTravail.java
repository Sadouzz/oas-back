package sn.oas.facturation.features.ordreTravail.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ordres_travail")
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class OrdreTravail implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_id")
    private Proforma proforma;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id")
    private OrdreReparation ordreReparation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutOrdreTravail statut = StatutOrdreTravail.RECAPITULATIF;

    @OneToMany
    @JoinColumn(name = "ordre_travail_id")
    @Builder.Default
    private List<BonDeCommande> bonsDeCommande = new ArrayList<>();

    @OneToMany(mappedBy = "ordreTravail", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BonDeSortie> bonsDeSortie = new ArrayList<>();

    @OneToMany(mappedBy = "ordreTravail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneTechnicienOrdreTravail> lignesTechniciens = new ArrayList<>();

    @OneToMany(mappedBy = "ordreTravail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DocumentOrdreTravail> documents = new ArrayList<>();

    @Column(name = "rapport_reparation", columnDefinition = "TEXT")
    private String rapportReparation;

    @Column(name = "date_fin_reparation")
    private LocalDateTime dateFinReparation;

    @Column(name = "remarques_preparation", columnDefinition = "TEXT")
    private String remarquesPreparation;

    @Column(name = "date_sortie")
    private LocalDateTime dateSortie;

    @Column(name = "kilometrage_sortie")
    private Double kilometrageSortie;

    @Column(name = "remarques_client", columnDefinition = "TEXT")
    private String remarquesClient;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public Garage getGarage() {
        return this.garage;
    }

    @Override
    public void setGarage(Garage garage) {
        this.garage = garage;
    }

    public void addBonDeSortie(BonDeSortie bonDeSortie) {
        if (bonDeSortie != null) {
            if (this.bonsDeSortie == null) {
                this.bonsDeSortie = new ArrayList<>();
            }
            this.bonsDeSortie.add(bonDeSortie);
            bonDeSortie.setOrdreTravail(this);
        }
    }

    public void removeBonDeSortie(BonDeSortie bonDeSortie) {
        if (bonDeSortie != null && this.bonsDeSortie != null) {
            this.bonsDeSortie.remove(bonDeSortie);
            bonDeSortie.setOrdreTravail(null);
        }
    }

    public void addDocument(DocumentOrdreTravail document) {
        if (document != null) {
            if (this.documents == null) {
                this.documents = new ArrayList<>();
            }
            this.documents.add(document);
            document.setOrdreTravail(this);
        }
    }

    public void removeDocument(DocumentOrdreTravail document) {
        if (document != null && this.documents != null) {
            this.documents.remove(document);
            document.setOrdreTravail(null);
        }
    }
}
