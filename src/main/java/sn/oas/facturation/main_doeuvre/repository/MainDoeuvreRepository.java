package sn.oas.facturation.main_doeuvre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;

import java.util.List;

@Repository
public interface MainDoeuvreRepository extends JpaRepository<MainDoeuvre, Long> {

    @Query("SELECT m FROM MainDoeuvre m WHERE " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.categorie.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MainDoeuvre> searchMainDoeuvres(@Param("keyword") String keyword);
}
