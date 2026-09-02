package sn.oas.facturation.features.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientLightDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
}
