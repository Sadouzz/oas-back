package sn.oas.facturation.features.fournisseur.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.entity.BaseEntity;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.features.garage.data.entity.Garage;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@Table(name = "fournisseurs")
public class Fournisseur extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;
    
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "email", nullable = true)
    private String email;
    
    @Column(name = "adresse", nullable = true)
    private String adresse;

    @Column(name = "remise", nullable = true)
    private Integer remise;

    


    @Builder.Default
    private boolean archived = false;

    
}
