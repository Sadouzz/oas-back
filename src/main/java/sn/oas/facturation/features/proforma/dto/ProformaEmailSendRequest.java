package sn.oas.facturation.features.proforma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProformaEmailSendRequest(
        @NotBlank @Email String email
) { }
