package sn.oas.facturation.shared.tenant;

import jakarta.persistence.PrePersist;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.garage.data.entity.Garage;

public class TenantListener {

    @PrePersist
    public void setGarage(Object entity) {
        if (entity instanceof TenantAware tenantAware) {
            if (tenantAware.getGarage() == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Agent agent) {
                    if (agent.getRole() == Role.SUPER_AGENT) {
                        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                        if (attributes != null) {
                            String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
                            if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                                Garage garage = new Garage();
                                garage.setId(Long.parseLong(garageIdHeader));
                                tenantAware.setGarage(garage);
                            }
                        }
                    } else {
                        tenantAware.setGarage(agent.getGarage());
                    }
                }
            }
        }
    }
}

