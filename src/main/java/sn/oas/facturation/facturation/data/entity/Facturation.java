package sn.oas.facturation.facturation.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; 

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Facturation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private BigDecimal montantTotal;

    //@Column(nullable = false)
    //private String statut; // Vous pouvez remplacer String par une Enum (ex: StatutFacturation)

    @Column(columnDefinition = "TEXT", nullable = true)
    private String remarque;

    @OneToMany(mappedBy = "facturation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneFacturationPiece> lignesFacturationPieces = new ArrayList<>();
    
    @OneToMany(mappedBy = "facturation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneFacturationMainDoeuvre> lignesFacturationMainDoeuvres = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = java.time.LocalDateTime.now();
        }
        if (this.dateModification == null) {
            this.dateModification = java.time.LocalDateTime.now();
        }
    }
}