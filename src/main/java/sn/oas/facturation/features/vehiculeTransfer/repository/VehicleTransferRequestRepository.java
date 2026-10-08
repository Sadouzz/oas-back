package sn.oas.facturation.features.vehiculeTransfer.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferRequest;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferStatus;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface VehicleTransferRequestRepository extends JpaRepository<VehicleTransferRequest, Long> {
    boolean existsByVehiculeIdAndRequesterIdAndStatus(Long vehicleId, Long requesterId, VehicleTransferStatus status);
    List<VehicleTransferRequest> findByStatusOrderByRequestedAtAsc(VehicleTransferStatus status);
    List<VehicleTransferRequest> findByRequesterIdOrderByRequestedAtDesc(Long requesterId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from VehicleTransferRequest r join fetch r.vehicule v join fetch v.client join fetch r.requester join fetch r.currentOwner where r.id = :id")
    java.util.Optional<VehicleTransferRequest> findForDecision(@Param("id") Long id);
}
