package sn.oas.facturation.recu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.facture.data.entity.Facture;
import sn.oas.facturation.facture.data.enums.StatutPaiement;
import sn.oas.facturation.facture.repository.FactureRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.data.enums.StatutFiche;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.recu.data.entity.Recu;
import sn.oas.facturation.recu.dto.RecuRequest;
import sn.oas.facturation.recu.dto.RecuResponse;
import sn.oas.facturation.recu.repository.RecuRepository;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecuServiceImpl implements RecuService {

    private final RecuRepository recuRepository;
    private final FactureRepository factureRepository;
    private final FicheAtelierRepository ficheAtelierRepository;

    @Override
    @Transactional
    public RecuResponse create(RecuRequest request) {
        Facture facture = factureRepository.findById(request.getFactureId())
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable"));

        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new IllegalStateException("Cette facture est déjà totalement payée");
        }

        if (request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
        }

        if (request.getMontant().compareTo(facture.getResteAPayer()) > 0) {
            throw new IllegalArgumentException("Le montant du reçu dépasse le reste à payer (" + facture.getResteAPayer() + ")");
        }

        int year = Year.now().getValue();
        long count = recuRepository.countByDatePaiementYear(year) + 1;
        String numero = String.format("REC-%d-%04d", year, count);

        Recu recu = Recu.builder()
                .numero(numero)
                .facture(facture)
                .montant(request.getMontant())
                .modePaiement(request.getModePaiement())
                .remarque(request.getRemarque())
                .build();

        recu = recuRepository.save(recu);

        // Update Facture
        facture.setMontantPaye(facture.getMontantPaye().add(request.getMontant()));
        facture.setResteAPayer(facture.getMontantTotal().subtract(facture.getMontantPaye()));

        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatutPaiement(StatutPaiement.PAYE);

            // Advance Fiche Atelier to TERMINE if it was waiting for payment
            if (facture.getFicheAtelier() != null) {
                FicheAtelier fiche = facture.getFicheAtelier();
                if (fiche.getStatut() == StatutFiche.EN_ATTENTE_PAIEMENT) {
                    fiche.setStatut(StatutFiche.TERMINE);
                    ficheAtelierRepository.save(fiche);
                }
            }
        } else {
            facture.setStatutPaiement(StatutPaiement.PARTIEL);
        }

        factureRepository.save(facture);

        return mapToResponse(recu);
    }

    @Override
    public List<RecuResponse> getByFacture(Long factureId) {
        return recuRepository.findByFactureIdOrderByDatePaiementDesc(factureId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RecuResponse> getClientRecus(sn.oas.facturation.auth.data.entity.Client client) {
        return recuRepository.findByFactureClientIdOrderByDatePaiementDesc(client.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RecuResponse> getAll() {
        return recuRepository.findAll()
                .stream().map(this::mapToResponse)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    private RecuResponse mapToResponse(Recu r) {
        return RecuResponse.builder()
                .id(r.getId())
                .numero(r.getNumero())
                .factureId(r.getFacture().getId())
                .numeroFacture(r.getFacture().getNumero())
                .clientNom(r.getFacture().getClient() != null ? r.getFacture().getClient().getFirstName() + " " + r.getFacture().getClient().getLastName() : null)
                .numeroFicheAtelier(r.getFacture().getFicheAtelier() != null ? r.getFacture().getFicheAtelier().getNumero() : null)
                .montant(r.getMontant())
                .modePaiement(r.getModePaiement())
                .remarque(r.getRemarque())
                .datePaiement(r.getDatePaiement())
                .build();
    }
}
