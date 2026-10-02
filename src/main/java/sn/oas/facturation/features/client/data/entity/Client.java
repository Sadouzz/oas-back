package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "clients")
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CLIENT")
@NoArgsConstructor
public class Client extends User {

    @Column(name = "adresse", nullable = true)
    private String adresse;

    @Builder.Default
    @OneToMany(mappedBy = "client")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vehicule> vehicules = new ArrayList<>();

    @Column(name = "code_client", nullable = true, unique = true)
    private String codeClient;


    //Compte client fidèle
    @Builder.Default
    @Column(name = "is_client_fidele", nullable = false, columnDefinition = "boolean default false")
    private boolean isClientFidele = false;

    @Column(name = "montant_remise", nullable = true)
    private Integer montantRemise;

    @Column(name = "montant_plafond", nullable = true)
    private Integer montantPlafond;

    @Column(name = "echeance", nullable = true)
    private Integer echeance;

    @Column(name = "ninea", nullable = true)
    private String ninea;

    @Column(name = "rccm", nullable = true)
    private String rccm;

    @Column(name = "rib", nullable = true)
    private String rib;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }
}