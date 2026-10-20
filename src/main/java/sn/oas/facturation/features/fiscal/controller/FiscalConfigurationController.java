package sn.oas.facturation.features.fiscal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.fiscal.data.entity.FiscalConfiguration;
import sn.oas.facturation.features.fiscal.service.FiscalConfigurationService;
import sn.oas.facturation.features.fiscal.dto.FiscalConfigurationRequest;

@RestController
@RequestMapping("/api/fiscal-configuration")
@RequiredArgsConstructor
public class FiscalConfigurationController {
    private final FiscalConfigurationService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FiscalConfiguration> getCurrent() {
        return ResponseEntity.ok(service.getCurrent());
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_AGENT', 'SUPER_AGENT', 'ROLE_MASTER', 'MASTER')")
    public ResponseEntity<FiscalConfiguration> update(@Valid @RequestBody FiscalConfigurationRequest request) {
        return ResponseEntity.ok(service.update(request));
    }
}
