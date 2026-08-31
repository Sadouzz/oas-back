import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RenameTables {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/oas";
        String user = "postgres";
        String password = "passer";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            try { stmt.execute("ALTER TABLE IF EXISTS fiches_atelier RENAME TO ordres_reparation;"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE IF EXISTS ligne_fiche_atelier_piece RENAME TO ligne_ordre_reparation_piece;"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE IF EXISTS ligne_fiche_atelier_main_doeuvre RENAME TO ligne_ordre_reparation_main_doeuvre;"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE IF EXISTS bons_de_livraison RENAME TO bons_de_reception;"); } catch (Exception ignored) {}
            
            System.out.println("Tables successfully processed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
