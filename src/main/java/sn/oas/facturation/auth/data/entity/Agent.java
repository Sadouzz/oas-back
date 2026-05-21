package sn.oas.facturation.auth.data.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sn.oas.facturation.auth.data.enums.Role;

import java.util.Collection;
import java.util.List;

@Entity
@DiscriminatorValue("AGENT")
@Table(name = "agents")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Agent extends User{

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}
