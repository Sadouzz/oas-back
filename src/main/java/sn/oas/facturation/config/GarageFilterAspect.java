package sn.oas.facturation.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.enums.Role;

@Aspect
@Component
public class GarageFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* sn.oas.facturation..*Repository.*(..))")
    public void enableGarageFilter() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Agent) {
                Agent agent = (Agent) auth.getPrincipal();
                
                org.springframework.web.context.request.ServletRequestAttributes attributes = 
                        (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                String garageIdHeader = null;
                if (attributes != null) {
                    garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
                }

                if (agent.getRole() == Role.SUPER_AGENT) {
                    if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                        Session session = entityManager.unwrap(Session.class);
                        Filter filter = session.enableFilter("garageFilter");
                        filter.setParameter("garageId", Long.parseLong(garageIdHeader));
                    }
                } else if (agent.getRole() != Role.MASTER && agent.getGarage() != null) {
                    Session session = entityManager.unwrap(Session.class);
                    Filter filter = session.enableFilter("garageFilter");
                    filter.setParameter("garageId", agent.getGarage().getId());
                }
            }
        } catch (Exception e) {
            // Log or ignore to prevent crashing the repository call
        }
    }
}
