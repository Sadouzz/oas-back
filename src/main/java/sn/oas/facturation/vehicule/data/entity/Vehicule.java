package sn.oas.facturation.vehicule.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;


@Entity
@Table(name = "vehicules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("vehicule")
    private List<FicheAtelier> fichesAtelier;



    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }
}
