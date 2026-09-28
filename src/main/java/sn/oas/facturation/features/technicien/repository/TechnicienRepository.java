package sn.oas.facturation.features.technicien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.data.enums.SpecialiteTechnicien;

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

    @Query("SELECT t FROM Technicien t WHERE " +
            "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Technicien> searchTechniciens(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Technicien> findBySpecialite(SpecialiteTechnicien specialite, org.springframework.data.domain.Pageable pageable);

    List<Technicien> findBySpecialite(SpecialiteTechnicien specialite);

    @Query("SELECT t FROM Technicien t WHERE t.specialite = :specialite AND (" +
            "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<Technicien> searchTechniciensWithSpecialite(
            @Param("keyword") String keyword,
            @Param("specialite") SpecialiteTechnicien specialite,
            org.springframework.data.domain.Pageable pageable);
}
