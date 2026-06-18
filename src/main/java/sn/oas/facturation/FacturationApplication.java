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
				System.out.println("====== DB CLEANUP INFO: " + e.getMessage() + " ======");
			}
		};
	}

}
