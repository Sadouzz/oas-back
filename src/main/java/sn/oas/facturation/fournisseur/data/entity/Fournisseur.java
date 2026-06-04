package sn.oas.facturation.fournisseur.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.shared.GarageEntityListener;
import sn.oas.facturation.shared.entity.GarageAware;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(GarageEntityListener.class)
public class Fournisseur implements GarageAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String matricule;
    private String nomEntreprise;
    private String nom;
    private String prenom;
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = true)
    private Garage garage;
}