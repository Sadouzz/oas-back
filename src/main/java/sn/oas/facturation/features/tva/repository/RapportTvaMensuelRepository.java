package sn.oas.facturation.features.tva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.tva.data.entity.RapportTvaMensuel;

import java.util.Optional;

@Repository
public interface RapportTvaMensuelRepository extends JpaRepository<RapportTvaMensuel, Long> {

    Optional<RapportTvaMensuel> findFirstByPeriodeOrderByIdDesc(String periode);
}
