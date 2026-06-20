package sn.oas.facturation.bonDeCommande.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;

import java.math.BigDecimal;

@Entity
@Table(name = "Ligne_Bon_De_Commande")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonDeCommandePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantite;

    @Builder.Default
    private Integer quantiteRecue = 0;

    private BigDecimal prixUnitaire;

    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_commande_id")
    private BonDeCommande bonDeCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_detachee_id")
    private PieceDetache pieceDetachee;

    private String designationPds;

    private String referencePds;

    private String categoriePds;
}