package sn.oas.facturation.messagerie.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private User expediteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id")
    private User destinataire;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Column(name = "date_envoi", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @PrePersist
    protected void onCreate() {
        if (this.dateEnvoi == null) {
            this.dateEnvoi = LocalDateTime.now();
        }
    }
}
