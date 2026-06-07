package sn.oas.facturation.mecanicien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;

import java.util.List;

@Repository
public interface MecanicienRepository extends JpaRepository<Mecanicien, Long> {

    @Query("SELECT m FROM Mecanicien m WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Mecanicien> searchMecaniciens(@Param("keyword") String keyword);
}
