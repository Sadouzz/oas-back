package sn.oas.facturation.features.noteDePrix.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.repository.PDPRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.features.piecedetache.repository.StockMouvementRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteDePrixServiceImpl implements NoteDePrixService {

    private final NoteDePrixRepository noteDePrixRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final PDPRepository pdpRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final StockMouvementRepository stockMouvementRepository;
    private final GarageRepository garageRepository;

    private Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            return (user instanceof Agent) ? (Agent) user : null;
        }
        return null;
    }

    private Garage getGarageConnecte(Agent agent) {
        if (agent != null && agent.getGarage() != null) {
            return agent.getGarage();
        }
        org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
            if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                try {
                    Garage g = garageRepository.findById(Long.parseLong(garageIdHeader)).orElse(null);
                    if (g != null) return g;
                } catch (Exception ignored) {}
            }
        }
        Garage g = documentNumberGeneratorService.getCurrentGarage();
        if (g != null) return g;
        return garageRepository.findAll().stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public NoteDePrix createNoteDePrix(NoteDePrixRequest request) {
        OrdreReparation fiche = null;
        if (request.getOrdreReparationId() != null) {
            fiche = ordreReparationRepository.findById(request.getOrdreReparationId()).orElse(null);
        }

        Agent agentConnecte = getAgentConnecte();
        Garage garage = (fiche != null && fiche.getGarage() != null) ? fiche.getGarage() : getGarageConnecte(agentConnecte);

        NoteDePrix note = NoteDePrix.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.NP))
                .ordreReparation(fiche)
                .agent(agentConnecte)
                .garage(garage)
                .kilometrage(request.getKilometrage())
                .remarque(request.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        BigDecimal montantTotalHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null && !request.getLignesPieces().isEmpty()) {
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if (lp.getPieceId() != null) {
                    PieceDetache pd = pieceDetacheRepository.findById(lp.getPieceId()).orElse(null);
                    if (pd instanceof PDP p) {
                        piece = p;
                    }
                }
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(piece)
                        .quantite(lp.getQuantite() != null ? lp.getQuantite() : 1)
                        .prix(lp.getPrix() != null ? lp.getPrix() : 0)
                        .build();
                int ligneTotal = ligne.getPrix() * ligne.getQuantite();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationPieces().add(ligne);
            }
        } else if (fiche != null && fiche.getLignesOrdreReparationPieces() != null) {
            for (var lf : fiche.getLignesOrdreReparationPieces()) {
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(lf.getPiece())
                        .quantite(lf.getQuantite())
                        .prix(lf.getPrix())
                        .build();
                int ligneTotal = ligne.getPrix() * ligne.getQuantite();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationPieces().add(ligne);
            }
        }

        if (request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty()) {
            for (var lm : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = null;
                if (lm.getMainDoeuvreId() != null) {
                    md = mainDoeuvreRepository.findById(lm.getMainDoeuvreId()).orElse(null);
                }
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(md)
                        .nbreHeure(lm.getNbreHeure() != null ? lm.getNbreHeure() : 1)
                        .tarifHoraire(lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0)
                        .build();
                int ligneTotal = ligne.getNbreHeure() * ligne.getTarifHoraire();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        } else if (fiche != null && fiche.getLignesOrdreReparationMainDoeuvres() != null) {
            for (var lf : fiche.getLignesOrdreReparationMainDoeuvres()) {
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(lf.getMainDoeuvre())
                        .nbreHeure(lf.getNbreHeure())
                        .tarifHoraire(lf.getPrix())
                        .build();
                int ligneTotal = ligne.getNbreHeure() * ligne.getTarifHoraire();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId()).orElse(null);
        } else if (fiche != null && fiche.getVehicule() != null) {
            client = fiche.getVehicule().getClient();
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId()).orElse(null);
        } else if (fiche != null) {
            vehicule = fiche.getVehicule();
        }

        if (client == null && vehicule != null) {
            client = vehicule.getClient();
        }

        note.setClient(client);
        note.setVehicule(vehicule);
        note.setModePaiement(request.getModePaiement() != null ? request.getModePaiement() : "ESPECE");
        note.setNumeroBonDeCommande(request.getNumeroBonDeCommande());
        note.setMontantAutre(request.getMontantAutre() != null ? request.getMontantAutre() : BigDecimal.ZERO);
        BigDecimal totalFinal = montantTotalHT.add(note.getMontantAutre());
        note.setMontantHT(totalFinal);
        note.setMontantTotal(totalFinal);
        note.setMontantPaye(BigDecimal.ZERO);
        note.setResteAPayer(totalFinal);
        note.setStatutPaiement(StatutPaiement.NON_PAYE);

        NoteDePrix saved = noteDePrixRepository.save(note);
        // Mouvement de stock : La Note de prix diminue le stock réel (SORTIE RÉELLE)
        for (LigneFacturationPiece lfp : saved.getLignesFacturationPieces()) {
            if (lfp.getPiece() != null && lfp.getPiece() instanceof PDP pdp) {
                double qteReelleAvant = pdp.getQteReelle() != null ? pdp.getQteReelle()
                        : (pdp.getStockMagasin() + pdp.getStockAtelier());
                pdp.setQteReelle(Math.max(0.0, qteReelleAvant - lfp.getQuantite()));
                pdpRepository.save(pdp);

                stockMouvementRepository.save(StockMouvement.builder()
                        .type(TypeMouvement.SORTIE_REELLE)
                        .quantite((double) lfp.getQuantite())
                        .stockMagasinAvant(pdp.getStockMagasin())
                        .stockAtelierAvant(pdp.getStockAtelier())
                        .stockMagasinApres(pdp.getStockMagasin())
                        .stockAtelierApres(pdp.getStockAtelier())
                        .stockReelApres(pdp.getQteReelle())
                        .prenom(agentConnecte != null ? agentConnecte.getFirstName() : "")
                        .nom(agentConnecte != null ? agentConnecte.getLastName() : "")
                        .numDocument(saved.getNumero())
                        .typeDocument("Note de prix")
                        .numeroSerie(pdp.getReference())
                        .immatriculation(fiche != null && fiche.getVehicule() != null ? fiche.getVehicule().getImmatriculation() : (vehicule != null ? vehicule.getImmatriculation() : ""))
                        .motif("Note de prix " + saved.getNumero())
                        .piece(pdp)
                        .agent(agentConnecte)
                        .garage(saved.getGarage())
                        .build());
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public NoteDePrix updateNoteDePrix(Long id, NoteDePrixRequest request) {
        NoteDePrix note = noteDePrixRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id));

        if (request.getOrdreReparationId() != null) {
            note.setOrdreReparation(ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElse(note.getOrdreReparation()));
        }
        if (request.getKilometrage() != null) {
            note.setKilometrage(request.getKilometrage());
        }
        if (request.getRemarque() != null) {
            note.setRemarque(request.getRemarque());
        }
        if (request.getClientId() != null) {
            clientRepository.findById(request.getClientId()).ifPresent(note::setClient);
        }
        if (request.getVehiculeId() != null) {
            vehiculeRepository.findById(request.getVehiculeId()).ifPresent(note::setVehicule);
        }
        if (request.getModePaiement() != null) {
            note.setModePaiement(request.getModePaiement());
        }
        if (request.getNumeroBonDeCommande() != null) {
            note.setNumeroBonDeCommande(request.getNumeroBonDeCommande());
        }

        if (request.getLignesPieces() != null) {
            note.getLignesFacturationPieces().clear();
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if (lp.getPieceId() != null) {
                    PieceDetache pd = pieceDetacheRepository.findById(lp.getPieceId()).orElse(null);
                    if (pd instanceof PDP p) {
                        piece = p;
                    }
                }
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(piece)
                        .quantite(lp.getQuantite() != null ? lp.getQuantite() : 1)
                        .prix(lp.getPrix() != null ? lp.getPrix() : 0)
                        .build();
                note.getLignesFacturationPieces().add(ligne);
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            note.getLignesFacturationMainDoeuvres().clear();
            for (var lm : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = null;
                if (lm.getMainDoeuvreId() != null) {
                    md = mainDoeuvreRepository.findById(lm.getMainDoeuvreId()).orElse(null);
                }
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(md)
                        .nbreHeure(lm.getNbreHeure() != null ? lm.getNbreHeure() : 1)
                        .tarifHoraire(lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0)
                        .build();
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        }

        if (request.getMontantAutre() != null) {
            note.setMontantAutre(request.getMontantAutre());
        }

        // Recalculate totals if lines or montantAutre were updated
        if (request.getLignesPieces() != null || request.getLignesMainDoeuvres() != null || request.getMontantAutre() != null) {
            BigDecimal piecesSum = note.getLignesFacturationPieces().stream()
                    .map(l -> BigDecimal.valueOf((long) (l.getPrix() != null ? l.getPrix() : 0) * (l.getQuantite() != null ? l.getQuantite() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal moSum = note.getLignesFacturationMainDoeuvres().stream()
                    .map(l -> BigDecimal.valueOf((long) (l.getTarifHoraire() != null ? l.getTarifHoraire() : 0) * (l.getNbreHeure() != null ? l.getNbreHeure() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalHT = piecesSum.add(moSum).add(note.getMontantAutre() != null ? note.getMontantAutre() : BigDecimal.ZERO);
            note.setMontantHT(totalHT);
            note.setMontantTotal(totalHT);
            BigDecimal paye = note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO;
            BigDecimal reste = totalHT.subtract(paye).max(BigDecimal.ZERO);
            note.setResteAPayer(reste);
            if (reste.compareTo(BigDecimal.ZERO) == 0 && totalHT.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PAYE);
            } else if (paye.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PARTIEL);
            } else {
                note.setStatutPaiement(StatutPaiement.NON_PAYE);
            }
        }

        if (request.getMontantPaye() != null) {
            note.setMontantPaye(request.getMontantPaye());
            BigDecimal total = note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO;
            BigDecimal reste = total.subtract(request.getMontantPaye()).max(BigDecimal.ZERO);
            note.setResteAPayer(reste);
            if (reste.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PAYE);
            } else if (request.getMontantPaye().compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PARTIEL);
            } else {
                note.setStatutPaiement(StatutPaiement.NON_PAYE);
            }
        }

        return noteDePrixRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDePrix getNoteDePrix(Long id) {
        return noteDePrixRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDePrix> getAllNotesDePrix() {
        return noteDePrixRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteNoteDePrix(Long id) {
        if (!noteDePrixRepository.existsById(id)) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id);
        }
        noteDePrixRepository.deleteById(id);
    }
}
