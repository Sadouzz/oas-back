package sn.oas.facturation.features.ficheAtelier.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "fiches_atelier_pdf")
@Getter
@NoArgsConstructor
public class FicheAtelierPdf {
    @Id
    @Column(name = "fiche_atelier_id")
    private Long ficheAtelierId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiche_atelier_id", insertable = false, updatable = false, nullable = false)
    private FicheAtelier ficheAtelier;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "contenu", columnDefinition = "bytea", nullable = false)
    private byte[] contenu;

    public FicheAtelierPdf(Long ficheAtelierId, byte[] contenu) {
        this.ficheAtelierId = ficheAtelierId;
        this.contenu = contenu;
    }
}
