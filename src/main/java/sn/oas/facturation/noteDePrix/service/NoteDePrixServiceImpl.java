package sn.oas.facturation.noteDePrix.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.noteDePrix.dto.NoteDePrixResponse;
import sn.oas.facturation.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.repository.PDPRepository;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;

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
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final sn.oas.facturation.piecedetache.repository.StockMouvementRepository stockMouvementRepository;

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

    @Override
    @Transactional
    public NoteDePrixResponse createNoteDePrix(NoteDePrixRequest request) {
        OrdreReparation fiche = null;
        if (request.getOrdreReparationId() != null) {
            fiche = ordreReparationRepository.findById(request.getOrdreReparationId()).orElse(null);
        }

        NoteDePrix note = NoteDePrix.builder()
                .numero(documentNumberGeneratorService
                        .generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.NP))
                .ordreReparation(fiche)
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
                    piece = pdpRepository.findById(lp.getPieceId()).orElse(null);
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

        Agent agentConnecte = getAgentConnecte();
        note.setAgent(agentConnecte);

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
        note.setStatutPaiement(sn.oas.facturation.facture.data.enums.StatutPaiement.NON_PAYE);

        if (fiche != null) {
            note.setGarage(fiche.getGarage());
        }

        NoteDePrix saved = noteDePrixRepository.save(note);
        // Mouvement de stock : La Note de prix diminue le stock réel (SORTIE RÉELLE)
        for (LigneFacturationPiece lfp : saved.getLignesFacturationPieces()) {
            if (lfp.getPiece() != null && lfp.getPiece() instanceof PDP pdp) {
                double qteReelleAvant = pdp.getQteReelle() != null ? pdp.getQteReelle()
                        : (pdp.getStockMagasin() + pdp.getStockAtelier());
                pdp.setQteReelle(Math.max(0.0, qteReelleAvant - lfp.getQuantite()));
                pdpRepository.save(pdp);

                stockMouvementRepository.save(sn.oas.facturation.piecedetache.data.entity.StockMouvement.builder()
                        .type(sn.oas.facturation.piecedetache.data.enums.TypeMouvement.SORTIE_REELLE)
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

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public NoteDePrixResponse updateNoteDePrix(Long id, NoteDePrixRequest request) {
        NoteDePrix note = noteDePrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note de prix non trouvée"));

        if (request.getOrdreReparationId() != null) {
            note.setOrdreReparation(ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElse(note.getOrdreReparation()));
        }
        if (request.getKilometrage() != null)
            note.setKilometrage(request.getKilometrage());
        if (request.getRemarque() != null)
            note.setRemarque(request.getRemarque());

        note.getLignesFacturationPieces().clear();
        note.getLignesFacturationMainDoeuvres().clear();

        BigDecimal montantTotalHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null) {
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if (lp.getPieceId() != null) {
                    piece = pdpRepository.findById(lp.getPieceId()).orElse(null);
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
        }

        if (request.getLignesMainDoeuvres() != null) {
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
        }

        note.setMontantHT(montantTotalHT);
        note.setMontantTotal(montantTotalHT);

        return mapToResponse(noteDePrixRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDePrixResponse getNoteDePrix(Long id) {
        return mapToResponse(noteDePrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note de prix non trouvée")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDePrixResponse> getAllNotesDePrix() {
        return noteDePrixRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNoteDePrix(Long id) {
        noteDePrixRepository.deleteById(id);
    }

    private NoteDePrixResponse mapToResponse(NoteDePrix note) {
        if (note == null) return null;

        Long clientId = null;
        String clientNom = null;
        if (note.getClient() != null) {
            clientId = note.getClient().getId();
            String fn = note.getClient().getFirstName() != null ? note.getClient().getFirstName() : "";
            String ln = note.getClient().getLastName() != null ? note.getClient().getLastName() : "";
            clientNom = (fn + " " + ln).trim();
        } else if (note.getOrdreReparation() != null && note.getOrdreReparation().getVehicule() != null
                && note.getOrdreReparation().getVehicule().getClient() != null) {
            clientId = note.getOrdreReparation().getVehicule().getClient().getId();
            String fn = note.getOrdreReparation().getVehicule().getClient().getFirstName() != null ? note.getOrdreReparation().getVehicule().getClient().getFirstName() : "";
            String ln = note.getOrdreReparation().getVehicule().getClient().getLastName() != null ? note.getOrdreReparation().getVehicule().getClient().getLastName() : "";
            clientNom = (fn + " " + ln).trim();
        }

        Long vehiculeId = null;
        String vehiculeImmat = null;
        if (note.getVehicule() != null) {
            vehiculeId = note.getVehicule().getId();
            vehiculeImmat = note.getVehicule().getImmatriculation();
        } else if (note.getOrdreReparation() != null && note.getOrdreReparation().getVehicule() != null) {
            vehiculeId = note.getOrdreReparation().getVehicule().getId();
            vehiculeImmat = note.getOrdreReparation().getVehicule().getImmatriculation();
        }

        List<LigneFacturationPieceResponse> piecesResp = (note.getLignesFacturationPieces() == null) ? new ArrayList<>()
                : note.getLignesFacturationPieces().stream()
                        .map(lp -> {
                            int q = lp.getQuantite() != null ? lp.getQuantite() : 0;
                            int p = lp.getPrix() != null ? lp.getPrix() : 0;
                            return LigneFacturationPieceResponse.builder()
                                    .id(lp.getId())
                                    .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                    .designationPiece(
                                            lp.getPiece() != null ? lp.getPiece().getDesignation() : "Pièce")
                                    .quantite(q)
                                    .prix(p)
                                    .montantTotal(p * q)
                                    .build();
                        })
                        .collect(Collectors.toList());

        List<LigneFacturationMainDoeuvreResponse> moResp = (note.getLignesFacturationMainDoeuvres() == null) ? new ArrayList<>()
                : note.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> {
                            int h = lm.getNbreHeure() != null ? lm.getNbreHeure() : 0;
                            int t = lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0;
                            return LigneFacturationMainDoeuvreResponse.builder()
                                    .id(lm.getId())
                                    .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                    .descriptionMainDoeuvre(
                                            lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null
                                                    ? lm.getMainDoeuvre().getCategorie().getNom()
                                                    : "Main d'œuvre")
                                    .nbreHeure(h)
                                    .tarifHoraire(t)
                                    .montantTotal(h * t)
                                    .build();
                        })
                        .collect(Collectors.toList());

        return NoteDePrixResponse.builder()
                .id(note.getId())
                .numero(note.getNumero())
                .dateCreation(note.getDateCreation())
                .montantHT(note.getMontantHT() != null ? note.getMontantHT() : BigDecimal.ZERO)
                .montantTotal(note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO)
                .montantPaye(note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO)
                .resteAPayer(note.getResteAPayer() != null ? note.getResteAPayer() : (note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO))
                .statutPaiement(note.getStatutPaiement() != null ? note.getStatutPaiement().name() : "NON_PAYE")
                .modePaiement(note.getModePaiement())
                .numeroBonDeCommande(note.getNumeroBonDeCommande())
                .montantAutre(note.getMontantAutre() != null ? note.getMontantAutre() : BigDecimal.ZERO)
                .clientId(clientId)
                .clientNom(clientNom)
                .vehiculeId(vehiculeId)
                .vehiculeImmatriculation(vehiculeImmat)
                .kilometrage(note.getKilometrage())
                .remarque(note.getRemarque())
                .statut(note.getStatut() != null ? note.getStatut().name() : null)
                .lignesPieces(piecesResp)
                .lignesMainDoeuvres(moResp)
                .build();
    }
}
