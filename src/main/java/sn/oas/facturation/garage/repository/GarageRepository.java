package sn.oas.facturation.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.garage.data.entity.Garage;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
}
