package sn.oas.facturation.shared.schema;

import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fait passer {@link PreparationSchemaRecus} avant la mise à jour du schéma par Hibernate. */
@Configuration
public class PreparationSchemaConfig {

    @Bean
    public static EntityManagerFactoryDependsOnPostProcessor preparationSchemaAvantJpa() {
        return new EntityManagerFactoryDependsOnPostProcessor(PreparationSchemaRecus.BEAN);
    }
}
