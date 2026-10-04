package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class ClientHeaderDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
}
