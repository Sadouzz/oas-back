package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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

    @Builder.Default
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "type_client", nullable = false, columnDefinition = "varchar(20) default 'PARTICULIER'")
    private TypeClient typeClient = TypeClient.PARTICULIER;

    @Column(name = "raison_sociale")
    private String raisonSociale;

    /** NINEA légal de l'entreprise, distinct du champ ninea qui contient un lien de justificatif. */
    @Column(name = "numero_entreprise")
    private String numeroEntreprise;

    @Column(name = "email_entreprise")
    private String emailEntreprise;

    @Column(name = "adresse_entreprise", columnDefinition = "TEXT")
    private String adresseEntreprise;

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

    /** Plafond cumulé de facturation sur la période d'échéance (indépendant du plafond d'encours). */
    @Column(name = "montant_plafond_echeance", precision = 15, scale = 2)
    private java.math.BigDecimal montantPlafondEcheance;

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
