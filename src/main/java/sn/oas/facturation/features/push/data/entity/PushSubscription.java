package sn.oas.facturation.features.push.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.user.data.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions", uniqueConstraints = @UniqueConstraint(name = "uk_push_subscription_endpoint", columnNames = "endpoint"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PushSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, columnDefinition = "text") private String endpoint;
    @Column(name = "public_key", nullable = false, length = 512) private String publicKey;
    @Column(name = "auth_secret", nullable = false, length = 512) private String authSecret;
    @Column(name = "created_at", nullable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
