package sn.oas.facturation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixDatabaseConstraintsRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("--- Vérification et correction des contraintes CHECK dans la base de données ---");
        String[] tables = {"proformas", "factures", "avoirs_ht", "avoirs_ttc", "bons_de_livraison", "notes_de_prix", "ordres_reparation", "bons_de_sortie"};
        
        for (String table : tables) {
            try {
                // Pour Postgres, une contrainte check générée automatiquement sur un champ enum a souvent le nom <table_name>_<column_name>_check
                // On essaie de supprimer la contrainte 'statut'
                String constraintName = table + "_statut_check";
                jdbcTemplate.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraintName);
                log.info("Contrainte {} supprimée avec succès sur la table {}", constraintName, table);
            } catch (Exception e) {
                log.warn("Impossible de supprimer la contrainte sur la table {} : {}", table, e.getMessage());
            }
        }
        log.info("--- Fin de la vérification des contraintes ---");
    }
}
