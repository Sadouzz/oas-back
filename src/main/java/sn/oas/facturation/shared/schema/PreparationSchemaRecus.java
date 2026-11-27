package sn.oas.facturation.shared.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sn.oas.facturation.features.client.data.enums.TypeMouvementPortefeuille;
import sn.oas.facturation.shared.documentNumber.DocumentType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prépare le schéma avant que Hibernate ne le mette à jour ({@code ddl-auto: update}).
 * <p>
 * Hibernate ne renomme pas de table et ne modifie pas les contraintes CHECK qu'il a créées pour les enums.
 * Cette étape, idempotente, s'exécute avant la création de l'EntityManagerFactory (voir
 * {@link PreparationSchemaConfig}) :
 * <ol>
 *     <li>l'ancien reçu d'encaissement par facture ({@code recus}, numéros RC) devient {@code recus_facture},
 *     puis le reçu client ({@code recus_client}) devient {@code recus} ;</li>
 *     <li>les contraintes CHECK des types de document et des mouvements de solde prépayé suivent les enums Java ;</li>
 *     <li>les reçus sans numéro reçoivent un numéro PREFIXE-RE-ANNEE-0001 et le compteur RE est recalé.</li>
 * </ol>
 * Seule PostgreSQL est concernée ; sur une autre base (tests), rien n'est fait.
 */
@Component(PreparationSchemaRecus.BEAN)
@Slf4j
public class PreparationSchemaRecus {

    static final String BEAN = "preparationSchemaRecus";

    private final JdbcTemplate jdbc;

    public PreparationSchemaRecus(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        if (estPostgres(dataSource)) {
            preparer();
        }
    }

    private void preparer() {
        executer("renommer les tables de reçus", """
                DO $$
                DECLARE c record;
                BEGIN
                    -- Ancien reçu d'encaissement par facture : il porte facture_id.
                    IF to_regclass('recus_facture') IS NULL AND EXISTS (
                            SELECT 1 FROM information_schema.columns
                            WHERE table_schema = current_schema() AND table_name = 'recus' AND column_name = 'facture_id') THEN
                        ALTER TABLE recus RENAME TO recus_facture;
                        -- Libère les noms de contraintes uniques que Hibernate calculera pour la nouvelle table recus.
                        FOR c IN SELECT con.conname, att.attname
                                 FROM pg_constraint con
                                 JOIN pg_attribute att ON att.attrelid = con.conrelid AND att.attnum = con.conkey[1]
                                 WHERE con.conrelid = 'recus_facture'::regclass AND con.contype = 'u'
                                   AND array_length(con.conkey, 1) = 1 LOOP
                            EXECUTE format('ALTER TABLE recus_facture RENAME CONSTRAINT %I TO %I',
                                    c.conname, 'uk_recus_facture_' || c.attname);
                        END LOOP;
                    END IF;
                    IF to_regclass('recus_client') IS NOT NULL AND to_regclass('recus') IS NULL THEN
                        ALTER TABLE recus_client RENAME TO recus;
                    END IF;
                END $$
                """);

        contrainteEnum("document_sequences", "document_type", "document_sequences_document_type_check",
                Arrays.stream(DocumentType.values()).map(Enum::name).toList());
        contrainteEnum("mouvements_portefeuille_client", "type", "mouvements_portefeuille_client_type_check",
                Arrays.stream(TypeMouvementPortefeuille.values()).map(Enum::name).toList());

        executer("numéroter les reçus existants", """
                DO $$
                BEGIN
                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_schema = current_schema() AND table_name = 'recus' AND column_name = 'client_id') THEN
                        ALTER TABLE recus ADD COLUMN IF NOT EXISTS numero varchar(40);

                        WITH a_numeroter AS (
                            SELECT r.id, g.prefixe,
                                   EXTRACT(YEAR FROM r.date_debut)::int AS annee,
                                   (SELECT COUNT(*) FROM recus d
                                    WHERE d.garage_id = r.garage_id AND d.numero IS NOT NULL
                                      AND EXTRACT(YEAR FROM d.date_debut) = EXTRACT(YEAR FROM r.date_debut))
                                   + ROW_NUMBER() OVER (PARTITION BY r.garage_id, EXTRACT(YEAR FROM r.date_debut)
                                                        ORDER BY r.id) AS rang
                            FROM recus r
                            JOIN garages g ON g.id = r.garage_id
                            WHERE r.numero IS NULL
                        )
                        UPDATE recus r
                        SET numero = n.prefixe || '-RE-' || n.annee || '-' || LPAD(n.rang::text, 4, '0')
                        FROM a_numeroter n
                        WHERE r.id = n.id;

                        INSERT INTO document_sequences (garage_id, document_type, year, next_value)
                        SELECT r.garage_id, 'RE', EXTRACT(YEAR FROM r.date_debut)::int, COUNT(*) + 1
                        FROM recus r
                        WHERE r.numero IS NOT NULL AND r.garage_id IS NOT NULL
                        GROUP BY r.garage_id, EXTRACT(YEAR FROM r.date_debut)
                        ON CONFLICT (garage_id, document_type, year)
                        DO UPDATE SET next_value = GREATEST(document_sequences.next_value, EXCLUDED.next_value);
                    END IF;
                END $$
                """);

        validationDocumentsDejaPayes("factures");
        validationDocumentsDejaPayes("notes_de_prix");
    }

    /**
     * Crée la colonne date_validation d'une table de documents (factures, notes de prix) et, une seule fois
     * à sa création, considère comme validés les documents déjà payés : seuls les documents soldés
     * ensuite par un reçu attendront la validation de l'agent.
     */
    private void validationDocumentsDejaPayes(String table) {
        executer("valider les " + table + " déjà payées", """
                DO $$
                BEGIN
                    IF to_regclass('%1$s') IS NOT NULL AND NOT EXISTS (
                            SELECT 1 FROM information_schema.columns
                            WHERE table_schema = current_schema() AND table_name = '%1$s' AND column_name = 'date_validation') THEN
                        ALTER TABLE %1$s ADD COLUMN date_validation timestamp(6);
                        UPDATE %1$s SET date_validation = now() WHERE statut_paiement = 'PAYE';
                    END IF;
                END $$
                """.formatted(table));
    }

    /** Recrée la contrainte CHECK d'une colonne d'enum avec toutes les valeurs de l'enum Java. */
    private void contrainteEnum(String table, String colonne, String contrainte, List<String> valeurs) {
        String liste = valeurs.stream().map(v -> "'" + v + "'").collect(Collectors.joining(", "));
        executer("contrainte " + contrainte, """
                DO $$
                BEGIN
                    IF to_regclass('%1$s') IS NOT NULL THEN
                        ALTER TABLE %1$s DROP CONSTRAINT IF EXISTS %3$s;
                        ALTER TABLE %1$s ADD CONSTRAINT %3$s CHECK (%2$s IN (%4$s));
                    END IF;
                END $$
                """.formatted(table, colonne, contrainte, liste));
    }

    private void executer(String libelle, String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            // Une étape en échec ne doit pas empêcher le démarrage : Hibernate signalera ce qui manque.
            log.warn("Préparation du schéma : échec de l'étape « {} » : {}", libelle, e.getMessage());
        }
    }

    private static boolean estPostgres(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            log.warn("Préparation du schéma : base inaccessible ({}), étape ignorée.", e.getMessage());
            return false;
        }
    }
}
