import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropConstraint {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://ep-holy-king-aqjd3rk2-pooler.c-8.us-east-1.aws.neon.tech/oas_database?sslmode=require&channelBinding=require";
        String user = "neondb_owner";
        String password = "npg_la0z1vjEkOrt";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE bons_de_commande ALTER COLUMN fournisseur_id DROP NOT NULL;");
            System.out.println("Constraint dropped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
