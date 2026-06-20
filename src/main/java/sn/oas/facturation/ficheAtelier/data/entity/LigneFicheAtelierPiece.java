package sn.oas.facturation.ficheAtelier.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.piecedetache.data.entity.PDP;

@Entity
@Table(name = "lignes_fiche_atelier_piece")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneFicheAtelierPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_atelier_id", nullable = false)
    private FicheAtelier ficheAtelier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private PDP piece;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;
}
