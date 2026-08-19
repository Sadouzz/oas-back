package sn.oas.facturation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/oas";
        String user = "postgres";
        String password = "passer";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE lignes_ordre_reparation_piece ALTER COLUMN piece_id DROP NOT NULL;");
            System.out.println("Dropped NOT NULL on lignes_ordre_reparation_piece");
            stmt.execute("ALTER TABLE lignes_facturation_piece ALTER COLUMN piece_id DROP NOT NULL;");
            System.out.println("Dropped NOT NULL on lignes_facturation_piece");
            System.out.println("Database constraints successfully dropped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
