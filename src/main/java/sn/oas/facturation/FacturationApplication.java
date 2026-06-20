package sn.oas.facturation;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FacturationApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(FacturationApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	org.springframework.boot.CommandLineRunner cleanupDb(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE main_doeuvre DROP COLUMN IF EXISTS categorie CASCADE;");
				System.out.println("====== DB CLEANUP: Obsolete column 'categorie' dropped successfully ======");
			} catch (Exception e) {
				System.out.println("====== DB CLEANUP INFO (main_doeuvre): " + e.getMessage() + " ======");
			}
			try {
				jdbcTemplate.execute("ALTER TABLE bons_de_livraison DROP COLUMN IF EXISTS paye CASCADE;");
				System.out.println("====== DB CLEANUP: Obsolete column 'paye' on 'bons_de_livraison' dropped successfully ======");
			} catch (Exception e) {
				System.out.println("====== DB CLEANUP INFO (bons_de_livraison): " + e.getMessage() + " ======");
			}
			try {
				jdbcTemplate.execute("ALTER TABLE bons_de_commande DROP CONSTRAINT IF EXISTS bons_de_commande_statut_check;");
				System.out.println("====== DB CLEANUP: Obsolete constraint 'bons_de_commande_statut_check' dropped successfully ======");
			} catch (Exception e) {
				System.out.println("====== DB CLEANUP INFO (bons_de_commande_statut_check): " + e.getMessage() + " ======");
			}
			try {
				jdbcTemplate.execute("ALTER TABLE fiche_atelier DROP CONSTRAINT IF EXISTS fiche_atelier_statut_check;");
				System.out.println("====== DB CLEANUP: Obsolete constraint 'fiche_atelier_statut_check' dropped successfully ======");
			} catch (Exception e) {
				System.out.println("====== DB CLEANUP INFO (fiche_atelier_statut_check): " + e.getMessage() + " ======");
			}
			try {
				jdbcTemplate.execute("ALTER TABLE recus DROP COLUMN IF EXISTS montant_paye CASCADE;");
				jdbcTemplate.execute("ALTER TABLE recus DROP COLUMN IF EXISTS reste_a_payer CASCADE;");
				jdbcTemplate.execute("ALTER TABLE recus DROP COLUMN IF EXISTS statut_paiement CASCADE;");
				System.out.println("====== DB CLEANUP: Obsolete columns on 'recus' dropped successfully ======");
			} catch (Exception e) {
				System.out.println("====== DB CLEANUP INFO (recus): " + e.getMessage() + " ======");
			}
		};
	}

}
