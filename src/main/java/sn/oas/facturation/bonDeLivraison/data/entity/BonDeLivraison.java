package sn.oas.facturation.bonDeLivraison.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.facturation.data.entity.FactureTTC;

@Entity
@Table(name = "bons_de_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BonDeLivraison extends FactureTTC {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_commande_id", nullable = true)
    private BonDeCommande bonDeCommande;

    @Column(nullable = false)
    private Boolean paye;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (this.paye == null) {
            this.paye = false;
        }
    }
}
