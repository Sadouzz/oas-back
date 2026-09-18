package sn.oas.facturation.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Seeder complet de toutes les entités de l'écosystème OAS Facturation.
 * Exécute le script SQL de référence seed_all_entities.sql de manière idempotente
 * dès que la base de données ne contient pas encore de dépôts/stocks.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class FullDataSeeder implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer depotCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM depots", Integer.class
            );

            if (depotCount == null || depotCount == 0) {
                log.info("=== Démarrage du seed complet de toutes les entités OAS ===");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("seed_all_entities.sql"));
                populator.setContinueOnError(false);
                populator.execute(dataSource);
                log.info("=== Seed complet exécuté avec succès (toutes les 50 entités interconnectées) ===");
            } else {
                log.info("=== Les entités OAS sont déjà seedées ({} dépôts détectés) ===", depotCount);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution du seed complet des entités OAS : {}", e.getMessage(), e);
        }
    }
}
