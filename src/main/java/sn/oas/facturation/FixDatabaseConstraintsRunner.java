package sn.oas.facturation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
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
        
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_type_check");
            log.info("Contrainte users_type_check supprimée avec succès sur la table users");
        } catch (Exception e) {
            log.warn("Impossible de supprimer la contrainte users_type_check sur la table users : {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE pieces_detachees SET prix_unitaire = prix WHERE prix IS NOT NULL AND prix_unitaire IS NULL");
            log.info("Valeurs de prix transférées vers prix_unitaire avec succès.");
        } catch (Exception e) {
            log.warn("Impossible de transférer les valeurs de prix : {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE pieces_detachees ALTER COLUMN categorie DROP NOT NULL");
            log.info("Contrainte NOT NULL supprimée sur la colonne categorie de pieces_detachees.");
        } catch (Exception e) {
            log.warn("Impossible de supprimer la contrainte NOT NULL sur categorie : {}", e.getMessage());
        }

        // --- NEW FIX: DROP OBSOLETE FOREIGN KEYS TO mecaniciens TABLE ---
        try {
            // Delete bad data that prevents foreign key creation
            jdbcTemplate.execute("DELETE FROM fiche_mecaniciens WHERE mecanicien_id NOT IN (SELECT id FROM techniciens)");
            jdbcTemplate.execute("DELETE FROM fiche_mecaniciens_reparation WHERE mecanicien_id NOT IN (SELECT id FROM techniciens)");
            log.info("Données obsolètes de mécaniciens supprimées.");

            // Drop specific constraint from error trace
            jdbcTemplate.execute("ALTER TABLE fiche_mecaniciens DROP CONSTRAINT IF EXISTS fk2rmbsma50tmll1pb60urfupdu");
            
            // Drop any other FK on fiche_mecaniciens pointing to mecaniciens by querying information_schema
            jdbcTemplate.execute(
                "DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT tc.constraint_name FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name " +
                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = 'fiche_mecaniciens' AND ccu.table_name = 'mecaniciens') " +
                "LOOP EXECUTE 'ALTER TABLE fiche_mecaniciens DROP CONSTRAINT ' || r.constraint_name; END LOOP; " +
                "END $$;"
            );

            jdbcTemplate.execute(
                "DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT tc.constraint_name FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name " +
                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = 'fiche_mecaniciens_reparation' AND ccu.table_name = 'mecaniciens') " +
                "LOOP EXECUTE 'ALTER TABLE fiche_mecaniciens_reparation DROP CONSTRAINT ' || r.constraint_name; END LOOP; " +
                "END $$;"
            );
            log.info("Anciennes clés étrangères vers 'mecaniciens' supprimées avec succès.");
        } catch (Exception e) {
            log.warn("Impossible de supprimer les anciennes clés étrangères : {}", e.getMessage());
        }

        log.info("--- Fin de la vérification des contraintes ---");
    }
}
