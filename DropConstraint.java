import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropConstraint {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/oas";
        String user = "postgres";
        String password = "passer";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE stock_mouvements DROP CONSTRAINT IF EXISTS stock_mouvements_type_check;");
            System.out.println("Constraint stock_mouvements_type_check dropped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
