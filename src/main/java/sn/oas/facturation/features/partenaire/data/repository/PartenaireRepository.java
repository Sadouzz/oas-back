package sn.oas.facturation.features.partenaire.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.partenaire.data.entity.Partenaire;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {
}
