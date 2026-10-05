package sn.oas.facturation.features.vehiculeTransfer.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleOwnershipPeriod;
import java.util.Optional;
public interface VehicleOwnershipPeriodRepository extends JpaRepository<VehicleOwnershipPeriod, Long> {
    Optional<VehicleOwnershipPeriod> findFirstByVehiculeIdAndEndedAtIsNullOrderByStartedAtDesc(Long vehicleId);
    boolean existsByVehiculeId(Long vehicleId);
}
