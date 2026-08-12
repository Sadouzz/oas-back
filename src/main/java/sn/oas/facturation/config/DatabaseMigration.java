package sn.oas.facturation.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void dropStatutConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE ordres_reparation DROP CONSTRAINT IF EXISTS ordres_reparation_statut_check");
            System.out.println("✅ Constraint 'ordres_reparation_statut_check' dropped successfully!");
        } catch (Exception e) {}
        
        try {
            jdbcTemplate.execute("ALTER TABLE facturation DROP CONSTRAINT IF EXISTS facturation_statut_check");
            System.out.println("✅ Constraint 'facturation_statut_check' dropped successfully!");
        } catch (Exception e) {}
        
        try {
            jdbcTemplate.execute("ALTER TABLE proforma DROP CONSTRAINT IF EXISTS proforma_statut_check");
            System.out.println("✅ Constraint 'proforma_statut_check' dropped successfully!");
        } catch (Exception e) {}
    }
}
