package sn.oas.facturation.features.ordreTravail.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

@Entity
@Table(name = "lignes_technicien_ordre_travail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ligne_technicien_ot", columnNames = { "ordre_travail_id", "technicien_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneTechnicienOrdreTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_travail_id", nullable = false)
    private OrdreTravail ordreTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id", nullable = false)
    @JsonIgnoreProperties({ "password", "authorities", "garage", "hibernateLazyInitializer", "handler" })
    private Technicien technicien;

}
