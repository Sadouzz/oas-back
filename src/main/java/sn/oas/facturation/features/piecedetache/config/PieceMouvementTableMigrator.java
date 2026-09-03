package sn.oas.facturation.features.piecedetache.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(0)
public class PieceMouvementTableMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // Check if old table stock_mouvements exists
            Integer oldTableCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'stock_mouvements'",
                Integer.class
            );
            if (oldTableCount != null && oldTableCount > 0) {
                Integer countNew = jdbcTemplate.queryForObject("SELECT count(*) FROM piece_mouvements", Integer.class);
                Integer countOld = jdbcTemplate.queryForObject("SELECT count(*) FROM stock_mouvements", Integer.class);
                if ((countNew == null || countNew == 0) && (countOld != null && countOld > 0)) {
                    log.info("Migrating {} rows from stock_mouvements to piece_mouvements...", countOld);
                    jdbcTemplate.execute("INSERT INTO piece_mouvements SELECT * FROM stock_mouvements ON CONFLICT DO NOTHING");
                    log.info("Migration from stock_mouvements to piece_mouvements completed.");
                }
            }
        } catch (Exception e) {
            log.warn("PieceMouvement migration skipped: {}", e.getMessage());
        }
    }
}
