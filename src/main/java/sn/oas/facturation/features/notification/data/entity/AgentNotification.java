package sn.oas.facturation.features.notification.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.auth.data.entity.Agent;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateCreation = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}
