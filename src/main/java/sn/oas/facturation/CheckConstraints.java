package sn.oas.facturation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckConstraints {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/garage_oas";
        String user = "postgres";
        String password = "oas"; // Using default oas password or postgres, let's try
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("--- CHECKING CONSTRAINTS ---");
            String query = "SELECT conname, pg_get_constraintdef(c.oid) FROM pg_constraint c JOIN pg_namespace n ON n.oid = c.connamespace WHERE conname LIKE '%statut%' OR conname LIKE '%fiche%' OR conname LIKE '%facturation%'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getString(1) + " : " + rs.getString(2));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            // Try without password or with empty password if 'oas' fails
            try (Connection conn = DriverManager.getConnection(url, user, "root");
                 Statement stmt = conn.createStatement()) {
                 
                System.out.println("--- CHECKING CONSTRAINTS (root) ---");
                String query = "SELECT conname, pg_get_constraintdef(c.oid) FROM pg_constraint c JOIN pg_namespace n ON n.oid = c.connamespace WHERE conname LIKE '%statut%' OR conname LIKE '%fiche%' OR conname LIKE '%facturation%'";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    System.out.println(rs.getString(1) + " : " + rs.getString(2));
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
