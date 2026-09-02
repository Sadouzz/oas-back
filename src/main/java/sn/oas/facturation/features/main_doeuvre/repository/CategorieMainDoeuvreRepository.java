package sn.oas.facturation.features.main_doeuvre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.main_doeuvre.data.entity.CategorieMainDoeuvre;

import java.util.List;

@Repository
public interface CategorieMainDoeuvreRepository extends JpaRepository<CategorieMainDoeuvre, Long> {

    @Query("SELECT c FROM CategorieMainDoeuvre c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CategorieMainDoeuvre> searchCategories(@Param("keyword") String keyword);
}
