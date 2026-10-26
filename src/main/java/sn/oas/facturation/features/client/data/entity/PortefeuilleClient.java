package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

@Entity
@Table(name = "portefeuilles_clients", uniqueConstraints =
        @UniqueConstraint(name = "uk_portefeuille_client_garage", columnNames = {"client_id", "garage_id"}))
@EntityListeners(TenantListener.class)
@org.hibernate.annotations.FilterDef(name = "garageFilter", parameters = @org.hibernate.annotations.ParamDef(name = "garageId", type = Long.class))
@org.hibernate.annotations.Filter(name = "garageFilter", condition = "garage_id = :garageId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortefeuilleClient implements TenantAware {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "solde", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @Version
    private long version;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (dateCreation == null) dateCreation = now;
        dateModification = now;
        if (solde == null) solde = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() { dateModification = LocalDateTime.now(); }
}
