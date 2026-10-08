package sn.oas.facturation.features.vehiculeTransfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferDecision;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferRequestCreate;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferRequestResponse;
import sn.oas.facturation.features.vehiculeTransfer.service.VehicleTransferService;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-transfers")
@RequiredArgsConstructor
public class VehicleTransferController {
    private final VehicleTransferService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    public ResponseEntity<VehicleTransferRequestResponse> request(@Valid @RequestBody VehicleTransferRequestCreate request) {
        return ResponseEntity.accepted().body(service.request(request));
    }
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    public List<VehicleTransferRequestResponse> myRequests() { return service.myRequests(); }
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    public List<VehicleTransferRequestResponse> pending() { return service.pendingRequests(); }
    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    public VehicleTransferRequestResponse decide(@PathVariable Long id, @Valid @RequestBody VehicleTransferDecision decision) {
        return service.decide(id, decision);
    }
}
