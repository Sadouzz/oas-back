package sn.oas.facturation.shared.documentNumber;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.Role;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class DocumentNumberGeneratorService {

    private final GarageRepository garageRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextNumber(DocumentType type) {
        Garage garage = getCurrentGarage();
        return generateNextNumber(garage, type);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextNumber(Garage garage, DocumentType type) {
        Garage targetGarage = garage;
        if (targetGarage == null) {
            targetGarage = getCurrentGarage();
        }
        if (targetGarage == null || targetGarage.getPrefixe() == null || targetGarage.getPrefixe().isBlank()) {
            // Dernier recours : premier garage de la base
            targetGarage = garageRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Garage ou préfixe du garage manquant"));
        }
        final Garage finalGarage = targetGarage;

        int currentYear = Year.now().getValue();

        // The pessimistic row lock used previously could not lock a missing sequence row.
        // Atomic upsert protects both first use and concurrent allocations.
        Long allocatedValue = jdbcTemplate.queryForObject("""
                INSERT INTO document_sequences (garage_id, document_type, year, next_value)
                VALUES (?, ?, ?, 2)
                ON CONFLICT (garage_id, document_type, year)
                DO UPDATE SET next_value = document_sequences.next_value + 1
                RETURNING next_value - 1
                """,
                Long.class,
                finalGarage.getId(),
                type.name(),
                currentYear);
        long currentValue = allocatedValue;

        return String.format("%s-%s-%d-%04d", finalGarage.getPrefixe(), type.name(), currentYear, currentValue);
    }

    public Garage getCurrentGarage() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Agent agent = null;
            if (auth.getPrincipal() instanceof Agent a) {
                agent = a;
            } else if (auth.getName() != null) {
                User user = userRepository.findByEmail(auth.getName())
                        .or(() -> userRepository.findByUsername(auth.getName()))
                        .orElse(null);
                if (user instanceof Agent a) {
                    agent = a;
                }
            }

            if (agent != null) {
                if (agent.getRole() == Role.SUPER_AGENT) {
                    org.springframework.web.context.request.ServletRequestAttributes attributes = 
                        (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
                        if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                            return garageRepository.findById(Long.parseLong(garageIdHeader)).orElse(agent.getGarage());
                        }
                    }
                }
                if (agent.getGarage() != null) {
                    return agent.getGarage();
                }
            }
        }
        return garageRepository.findAll().stream().findFirst().orElse(null);
    }
}
