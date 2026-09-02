package sn.oas.facturation.features.technicien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.auth.data.entity.Technicien;

import java.util.List;

@Repository
public interface TechnicienRepository extends JpaRepository<Technicien, Long> {

    @Query("SELECT t FROM Technicien t WHERE " +
            "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Technicien> searchTechniciens(@Param("keyword") String keyword);
}
