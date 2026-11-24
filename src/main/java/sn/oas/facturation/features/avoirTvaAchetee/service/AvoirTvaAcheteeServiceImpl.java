package sn.oas.facturation.features.avoirTvaAchetee.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeRequest;
import sn.oas.facturation.features.avoirTvaAchetee.repository.AvoirTvaAcheteeRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AvoirTvaAcheteeServiceImpl implements AvoirTvaAcheteeService {

    private static final int MAX_PAGE_SIZE = 500;

    private final AvoirTvaAcheteeRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AvoirTvaAchetee create(AvoirTvaAcheteeRequest request) {
        if (request == null) {
            throw new BadRequestException("Les informations de l'avoir TVA achetée sont obligatoires.");
        }
        if (request.designation() == null || request.designation().isBlank()) {
            throw new BadRequestException("La désignation est obligatoire.");
        }
        if (request.dateOperation() == null) {
            throw new BadRequestException("La date est obligatoire.");
        }
        if (request.montantTva() == null || request.montantTva().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant de l'avoir TVA achetée doit être positif ou nul.");
        }

        AvoirTvaAchetee entity = AvoirTvaAchetee.builder()
                .designation(request.designation().trim())
                .dateOperation(request.dateOperation())
                .montantTva(request.montantTva().setScale(2, RoundingMode.HALF_UP))
                .agent(resolveCurrentAgent())
                .build();
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvoirTvaAchetee> getByMonth(YearMonth month, int page, int size) {
        YearMonth target = month != null ? month : YearMonth.now();
        int effectivePage = Math.max(0, page);
        int effectiveSize = size > 0 ? Math.min(size, MAX_PAGE_SIZE) : 10;
        LocalDate debut = target.atDay(1);
        LocalDate fin = target.atEndOfMonth();
        return repository.findByDateOperationBetweenOrderByDateOperationDescIdDesc(
                debut, fin, PageRequest.of(effectivePage, effectiveSize));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AvoirTvaAchetee existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avoir TVA achetée introuvable avec l'id : " + id));
        repository.delete(existing);
    }

    private Agent resolveCurrentAgent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        String username = auth.getName();
        User user = userRepository.findFirstByUsernameOrEmail(username, username).orElse(null);
        return (user instanceof Agent agent) ? agent : null;
    }
}
