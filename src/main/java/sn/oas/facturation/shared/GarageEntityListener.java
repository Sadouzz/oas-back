package sn.oas.facturation.shared;

import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.shared.entity.GarageAware;

/**
 * Listener JPA qui injecte automatiquement le Garage de l'utilisateur
 * connecté dans toute entité implémentant {@link GarageAware},
 * juste avant sa première persistance en base.
 *
 * <p>Pour activer ce comportement sur une entité, ajoutez simplement :
 * <pre>
 *   {@code @EntityListeners(GarageEntityListener.class)}
 * </pre>
 * et faites implémenter {@link GarageAware} à l'entité.
 */
public class GarageEntityListener {

    private static final Logger log = LoggerFactory.getLogger(GarageEntityListener.class);

    @PrePersist
    public void onPrePersist(Object entity) {
        if (!(entity instanceof GarageAware garageAware)) {
            return;
        }

        // Si le garage est déjà renseigné, on ne l'écrase pas.
        if (garageAware.getGarage() != null) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("GarageEntityListener : aucun utilisateur authentifié trouvé pour {}, le garage ne sera pas injecté.",
                    entity.getClass().getSimpleName());
            return;
        }

        try {
            UserRepository userRepository = ApplicationContextProvider.getBean(UserRepository.class);
            String username = auth.getName();

            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);

            if (user == null) {
                log.warn("GarageEntityListener : utilisateur '{}' introuvable.", username);
                return;
            }

            Garage garage = user.getGarage();
            if (garage == null) {
                log.warn("GarageEntityListener : l'utilisateur '{}' n'a pas de garage associé.", username);
                return;
            }

            garageAware.setGarage(garage);
            log.debug("GarageEntityListener : garage '{}' injecté dans {}.",
                    garage.getId(), entity.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("GarageEntityListener : erreur lors de l'injection du garage pour {}.", entity.getClass().getSimpleName(), e);
        }
    }
}
