package sn.oas.facturation.garage.data.entity;

import sn.oas.facturation.auth.data.entity.User;
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

    // Depending on your logic, 'etat' might be better as an Enum or Boolean.
    // Kept as String here based on the general UML representation.
    //private String etat;

    @JsonIgnore // pour prévenir les erreurs de récursion infinie lors de la sérialisation des
                // objets JSON (boucle Garage -> User -> Garage).
    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}