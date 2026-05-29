package sn.oas.facturation.mecanicien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;

@Repository
public interface MecanicienRepository extends JpaRepository<Mecanicien, Long> {
}
