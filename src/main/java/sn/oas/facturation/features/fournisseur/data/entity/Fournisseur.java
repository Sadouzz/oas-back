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
public class Fournisseur extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    private String matricule;
    private String nomEntreprise;
    private String nom;
    private String prenom;

    @Builder.Default
    private boolean archived = false;
}
