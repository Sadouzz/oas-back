package sn.oas.facturation.features.vehicule.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.shared.entity.BaseEntity;

@Entity
@Table(name = "vehicules")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String immatriculation;

    private Integer annee;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false)
    private String marque;
    
    @Column(nullable = false)
    private Double kilometrage;

    @Column(name = "numero_chassis", unique = true)
    private String numeroChassis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties("vehicules")
    private Client client;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrdreReparation> ordresReparation;
}
