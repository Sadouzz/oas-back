package sn.oas.facturation.shared;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utilitaire permettant un accès statique au contexte Spring.
 * Utilisé notamment par {@code GarageEntityListener} qui est instancié
 * par JPA et non par Spring, et qui a donc besoin d'un moyen de récupérer
 * des beans Spring (ex : UserRepository).
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException(
                    "ApplicationContext non initialisé. Vérifiez que ApplicationContextProvider est bien un @Component Spring.");
        }
        return context.getBean(beanClass);
    }
}
