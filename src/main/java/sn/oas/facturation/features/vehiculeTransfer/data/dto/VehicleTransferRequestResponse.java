package sn.oas.facturation.features.vehiculeTransfer.data.dto;

import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferRequest;
import java.time.LocalDateTime;

public record VehicleTransferRequestResponse(Long id, Long vehicleId, String immatriculation, String marque, String modele,
        String chassis, Long requesterId, String requesterName, Long currentOwnerId, String currentOwnerName,
        String status, String requestNote, String decisionNote, LocalDateTime requestedAt, LocalDateTime decidedAt) {
    public static VehicleTransferRequestResponse forAgent(VehicleTransferRequest r) {
        var v = r.getVehicule(); var owner = r.getCurrentOwner(); var requester = r.getRequester();
        return new VehicleTransferRequestResponse(r.getId(), v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getNumeroChassis(), requester.getId(), fullName(requester.getFirstName(), requester.getLastName()),
                owner.getId(), fullName(owner.getFirstName(), owner.getLastName()), r.getStatus().name(), r.getRequestNote(),
                r.getDecisionNote(), r.getRequestedAt(), r.getDecidedAt());
    }
    public static VehicleTransferRequestResponse forClient(VehicleTransferRequest r) {
        var v = r.getVehicule();
        return new VehicleTransferRequestResponse(r.getId(), v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getNumeroChassis(), r.getRequester().getId(), null, null, null, r.getStatus().name(), r.getRequestNote(),
                r.getDecisionNote(), r.getRequestedAt(), r.getDecidedAt());
    }
    private static String fullName(String first, String last) { return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim(); }
}
