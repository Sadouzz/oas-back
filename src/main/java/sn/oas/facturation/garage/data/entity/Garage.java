package sn.oas.facturation.garage.data.entity;

import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "garages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    private String ville;

    private String adresse;

    private String contact;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    // Depending on your logic, 'etat' might be better as an Enum or Boolean.
    // Kept as String here based on the general UML representation.
    //private String etat;

    @JsonIgnore // pour prévenir les erreurs de récursion infinie lors de la sérialisation des
                // objets JSON (boucle Garage -> User -> Garage).
    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @JsonIgnore // On évite la boucle JSON Garage -> PieceDetache -> Garage
    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PieceDetache> piecesDetachees;

    @JsonIgnore // On évite la boucle JSON Garage -> Mecanicien -> Garage
    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mecanicien> mecaniciens;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }
}