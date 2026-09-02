package sn.oas.facturation.features.garage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.garage.data.entity.Garage;

import java.util.Optional;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
    Optional<Garage> findByPrefixeIgnoreCase(String prefixe);
    Optional<Garage> findByNomIgnoreCase(String nom);
    Page<Garage> findByArchivedFalse(Pageable pageable);
}
