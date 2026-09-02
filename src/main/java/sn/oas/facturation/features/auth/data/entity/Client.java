package sn.oas.facturation.features.auth.data.entity;

import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }
}