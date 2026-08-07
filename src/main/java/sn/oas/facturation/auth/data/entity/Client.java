package sn.oas.facturation.auth.data.entity;

import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.persistence.*;
import lombok.Builder;
import sn.oas.facturation.auth.data.enums.TypeClient;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "clients")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CLIENT")
@NoArgsConstructor
public class Client extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_client")
    @Builder.Default
    private TypeClient typeClient = TypeClient.PARTICULIER;

    @Column(name = "raison_sociale")
    private String raisonSociale;

    @Column(name = "numero_entreprise")
    private String numeroEntreprise;

    @Column(name = "email_entreprise")
    private String emailEntreprise;

    @Column(name = "adresse_entreprise", columnDefinition = "TEXT")
    private String adresseEntreprise;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }
}
