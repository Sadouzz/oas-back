package sn.oas.facturation.mecanicien.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.shared.GarageEntityListener;
import sn.oas.facturation.shared.entity.GarageAware;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mecaniciens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(GarageEntityListener.class)
public class Mecanicien implements GarageAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @ManyToMany(mappedBy = "mecaniciens", fetch = FetchType.LAZY)
    @Builder.Default
    private List<FicheAtelier> fichesAtelier = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }

}