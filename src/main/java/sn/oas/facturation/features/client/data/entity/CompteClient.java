package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Compte financier d'entreprise, distinct du compte d'authentification du client. */
@Entity
@Table(name = "comptes_clients", uniqueConstraints = @UniqueConstraint(name = "uk_compte_client_client", columnNames = "client_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompteClient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "solde_credit", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal soldeCredit = BigDecimal.ZERO;

    @Version
    private long version;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (dateCreation == null) dateCreation = now;
        dateModification = now;
        if (soldeCredit == null) soldeCredit = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() { dateModification = LocalDateTime.now(); }
}
