package sn.oas.facturation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/oas";
        String user = "postgres";
        String password = "passer";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("--- lignes_ordre_reparation_piece columns ---");
            ResultSet rs = stmt.executeQuery("SELECT column_name, is_nullable FROM information_schema.columns WHERE table_name = 'lignes_ordre_reparation_piece'");
            while (rs.next()) {
                System.out.println(rs.getString("column_name") + " : " + rs.getString("is_nullable"));
            }
            
            System.out.println("--- lignes_facturation_piece columns ---");
            ResultSet rs2 = stmt.executeQuery("SELECT column_name, is_nullable FROM information_schema.columns WHERE table_name = 'lignes_facturation_piece'");
            while (rs2.next()) {
                System.out.println(rs2.getString("column_name") + " : " + rs2.getString("is_nullable"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
