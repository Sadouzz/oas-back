package sn.oas.facturation.features.vehiculeTransfer.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_ownership_periods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleOwnershipPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "vehicule_id", nullable = false) private Vehicule vehicule;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false) private Client client;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "ended_at") private LocalDateTime endedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "transfer_request_id") private VehicleTransferRequest transferRequest;
}
