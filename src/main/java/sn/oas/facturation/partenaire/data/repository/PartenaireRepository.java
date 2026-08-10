package sn.oas.facturation.partenaire.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.partenaire.data.entity.Partenaire;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {
}
