package sn.oas.facturation.features.connectionHistory.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import sn.oas.facturation.features.connectionHistory.data.enums.StatusConnectionHistory;

@Entity
@Table(name = "connection_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String ipAddress;
    @Enumerated(EnumType.STRING)
    private StatusConnectionHistory status;
    private LocalDateTime timestamp;
}