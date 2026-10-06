package sn.oas.facturation.features.vehiculeTransfer.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_transfer_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleTransferRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "vehicule_id", nullable = false) private Vehicule vehicule;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requester_client_id", nullable = false) private Client requester;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "current_owner_client_id", nullable = false) private Client currentOwner;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by_user_id") private User reviewedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default private VehicleTransferStatus status = VehicleTransferStatus.PENDING;
    @Column(name = "request_note", columnDefinition = "TEXT") private String requestNote;
    @Column(name = "decision_note", columnDefinition = "TEXT") private String decisionNote;
    @CreationTimestamp @Column(name = "requested_at", nullable = false, updatable = false) private LocalDateTime requestedAt;
    @Column(name = "decided_at") private LocalDateTime decidedAt;
}
