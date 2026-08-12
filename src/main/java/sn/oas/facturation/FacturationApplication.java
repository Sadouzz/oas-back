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
	public org.springframework.boot.CommandLineRunner dropConstraint(javax.sql.DataSource dataSource) {
		return args -> {
			try (java.sql.Connection conn = dataSource.getConnection();
				 java.sql.Statement stmt = conn.createStatement()) {
				stmt.execute("ALTER TABLE document_sequences DROP CONSTRAINT IF EXISTS document_sequences_document_type_check;");
				System.out.println("========== Constraint document_sequences_document_type_check dropped successfully ==========");
			} catch (Exception e) {
				System.err.println("Failed to drop constraint: " + e.getMessage());
			}
		};
	}
}
