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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.enums.Role;

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
                
                ServletRequestAttributes attributes = 
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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
                } else if (agent.getGarage() != null) {
                    Session session = entityManager.unwrap(Session.class);
                    Filter filter = session.enableFilter("garageFilter");
                    filter.setParameter("garageId", agent.getGarage().getId());
                }
            } else if (auth != null && auth.getPrincipal() instanceof Technicien) {
                // Un technicien connecté n'a pas de rôle SUPER_AGENT équivalent : il est
                // toujours filtré par son propre garage, s'il en a un.
                Technicien technicien = (Technicien) auth.getPrincipal();
                if (technicien.getGarage() != null) {
                    Session session = entityManager.unwrap(Session.class);
                    Filter filter = session.enableFilter("garageFilter");
                    filter.setParameter("garageId", technicien.getGarage().getId());
                }
            }
        } catch (Exception e) {
            // Log or ignore to prevent crashing the repository call
        }
    }
}
