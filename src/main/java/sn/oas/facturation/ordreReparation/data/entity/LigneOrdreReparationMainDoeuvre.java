package sn.oas.facturation.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;

@Entity
@Table(name = "lignes_ordre_reparation_main_doeuvre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdreReparationMainDoeuvre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false)
    private OrdreReparation ordreReparation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_doeuvre_id", nullable = false)
    private MainDoeuvre mainDoeuvre;

    @Column(nullable = false)
    private Integer nbreHeure;
    
    @Column(nullable = false)
    private Integer prix;
}
