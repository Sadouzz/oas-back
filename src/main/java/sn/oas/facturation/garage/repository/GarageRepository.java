package sn.oas.facturation.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.garage.data.entity.Garage;

import java.util.Optional;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
    Optional<Garage> findByPrefixeIgnoreCase(String prefixe);
    Optional<Garage> findByNomIgnoreCase(String nom);
}
