package sn.oas.facturation.main_doeuvre.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorie_main_doeuvre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorieMainDoeuvre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;
}
