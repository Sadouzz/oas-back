package sn.oas.facturation.features.garage.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sn.oas.facturation.shared.entity.BaseEntity;

@Entity
@Table(name = "garages")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Garage extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String localite;

    @Column(nullable = false, unique = true, length = 5)
    private String prefixe;

    @Column(name = "numero_fixe")
    private String numeroFixe;

    @Column(name = "numero_whatsapp")
    private String numeroWhatsapp;

    @Column
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean archived = false;
}
