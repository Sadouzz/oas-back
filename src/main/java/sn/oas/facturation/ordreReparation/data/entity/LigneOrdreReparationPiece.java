package sn.oas.facturation.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.piecedetache.data.entity.PDP;

@Entity
@Table(name = "lignes_ordre_reparation_piece")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdreReparationPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false)
    private OrdreReparation ordreReparation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private PDP piece;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;
}
