package sn.oas.facturation.shared.tenant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.enums.Role;

public class TenantContext {

    public static String getCurrentTenant() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "anonymous";
            }

            if (auth.getPrincipal() instanceof Agent agent) {
                if (agent.getRole() == Role.SUPER_AGENT) {
                    ServletRequestAttributes attributes = 
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
                        if (garageIdHeader != null && !garageIdHeader.trim().isEmpty()) {
                            return "garage_" + garageIdHeader.trim();
                        }
                    }
                }
                if (agent.getGarage() != null && agent.getGarage().getId() != null) {
                    return "garage_" + agent.getGarage().getId();
                }
            } else if (auth.getPrincipal() instanceof Technicien technicien) {
                if (technicien.getGarage() != null && technicien.getGarage().getId() != null) {
                    return "garage_" + technicien.getGarage().getId();
                }
            }
        } catch (Exception ignored) {
        }
        return "default";
    }
}
