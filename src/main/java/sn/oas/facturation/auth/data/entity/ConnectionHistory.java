package sn.oas.facturation.auth.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    private String email;
    private String ipAddress;
    private String status;
    private LocalDateTime timestamp;
}