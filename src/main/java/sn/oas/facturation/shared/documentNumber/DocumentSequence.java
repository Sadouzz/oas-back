package sn.oas.facturation.shared.documentNumber;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.garage.data.entity.Garage;

@Entity
@Table(name = "document_sequences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"garage_id", "document_type", "year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private int year;

    @Column(name = "next_value", nullable = false)
    private long nextValue;
}
