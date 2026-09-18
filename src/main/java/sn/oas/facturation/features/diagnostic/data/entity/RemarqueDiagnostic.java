package sn.oas.facturation.features.diagnostic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

import java.time.LocalDateTime;

@Entity
@Table(name = "remarques_diagnostic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemarqueDiagnostic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    @JsonIgnoreProperties({ "piecesJointes", "remarques", "ordreReparation" })
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id")
    @JsonIgnoreProperties({ "password", "authorities", "garage" })
    private Technicien technicien;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
}
