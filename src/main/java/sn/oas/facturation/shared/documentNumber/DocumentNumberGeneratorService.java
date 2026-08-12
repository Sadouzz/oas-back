package sn.oas.facturation.shared.documentNumber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.garage.data.entity.Garage;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class DocumentNumberGeneratorService {

    private final DocumentSequenceRepository documentSequenceRepository;
    private final sn.oas.facturation.garage.repository.GarageRepository garageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextNumber(DocumentType type) {
        Garage garage = getCurrentGarage();
        if (garage == null || garage.getPrefixe() == null || garage.getPrefixe().isBlank()) {
            throw new IllegalArgumentException("Garage ou préfixe du garage manquant");
        }

        int currentYear = Year.now().getValue();

        DocumentSequence sequence = documentSequenceRepository
                .findByGarageIdAndDocumentTypeAndYearWithLock(garage.getId(), type, currentYear)
                .orElseGet(() -> DocumentSequence.builder()
                        .garage(garage)
                        .documentType(type)
                        .year(currentYear)
                        .nextValue(1L)
                        .build());

        long currentValue = sequence.getNextValue();
        
        sequence.setNextValue(currentValue + 1);
        documentSequenceRepository.save(sequence);

        return String.format("%s-%s-%d-%04d", garage.getPrefixe(), type.name(), currentYear, currentValue);
    }

    private Garage getCurrentGarage() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof sn.oas.facturation.auth.data.entity.Agent agent) {
            if (agent.getRole() == sn.oas.facturation.auth.data.enums.Role.SUPER_AGENT) {
                org.springframework.web.context.request.ServletRequestAttributes attributes = 
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
                    if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                        return garageRepository.findById(Long.parseLong(garageIdHeader))
                            .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé pour l'ID : " + garageIdHeader));
                    }
                }
            } else {
                return agent.getGarage();
            }
        }
        return null;
    }
}
