package sn.oas.facturation.marketplace.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "marketplace_produits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Double prix;

    @Column(length = 4000)
    private String mediaUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean archive = false;
}
