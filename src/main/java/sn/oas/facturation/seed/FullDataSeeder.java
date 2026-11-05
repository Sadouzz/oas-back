package sn.oas.facturation.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Seeder de démonstration des modules métier liés d'OAS Facturation.
 * Exécute le script SQL de référence seed_all_entities.sql de manière idempotente
 * lorsque ses données marqueurs ne sont pas présentes.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class FullDataSeeder implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer seedMarkerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categorie_main_doeuvre", Integer.class
            );

            if (seedMarkerCount == null || seedMarkerCount == 0) {
                log.info("=== Démarrage du seed complet de toutes les entités OAS ===");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("seed_all_entities.sql"));
                populator.setContinueOnError(false);
                populator.execute(dataSource);
                log.info("=== Jeu de démonstration des modules métier inséré avec succès ===");
            } else {
                log.info("=== Les entités OAS sont déjà seedées ({} catégories de main-d'œuvre détectées) ===", seedMarkerCount);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution du seed complet des entités OAS : {}", e.getMessage(), e);
            throw new IllegalStateException("Le seed complet OAS a échoué", e);
        }
    }
}
