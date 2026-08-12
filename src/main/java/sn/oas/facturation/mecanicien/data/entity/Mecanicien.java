package sn.oas.facturation.mecanicien.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.garage.data.entity.Garage;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "mecaniciens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class Mecanicien implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;

    @Column(nullable = false)
    private String nom;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "mecaniciens", fetch = FetchType.LAZY)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<FicheAtelier> fichesAtelier = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

}
