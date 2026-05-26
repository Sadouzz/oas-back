package sn.oas.facturation.main_doeuvre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;

@Repository
public interface MainDoeuvreRepository extends JpaRepository<MainDoeuvre, Long> {
}
