package sn.oas.facturation.auth.data.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sn.oas.facturation.auth.data.enums.Specialite;
import sn.oas.facturation.garage.data.entity.Garage;

import java.util.Collection;
import java.util.List;

/**
 * Compte utilisateur "Technicien" : type d'utilisateur à part entière (au même niveau que
 * {@link Client} et {@link Agent}), avec son propre login. Remplace fonctionnellement
 * l'ancien module mecanicien/ (entité Mecanicien, qui n'était pas un compte utilisateur) :
 * voir rapport de la tâche pour la justification détaillée du remplacement.
 */
@Entity
@DiscriminatorValue("TECHNICIEN")
@Table(name = "techniciens")
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Technicien extends User {

    @Column(name = "adresse")
    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialite")
    private Specialite specialite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_TECHNICIEN"));
    }
}
