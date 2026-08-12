package sn.oas.facturation.noteDePrix.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteDePrixServiceImpl implements NoteDePrixService {

    private final NoteDePrixRepository noteDePrixRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final PDPRepository pdpRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    @Transactional
    public NoteDePrixResponse createNoteDePrix(NoteDePrixRequest request) {
        FicheAtelier fiche = null;
        if (request.getFicheAtelierId() != null) {
            fiche = ficheAtelierRepository.findById(request.getFicheAtelierId())
                    .orElseThrow(() -> new RuntimeException("Fiche atelier non trouvée"));
        }

        NoteDePrix note = NoteDePrix.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.NP))
                .ficheAtelier(fiche)
                .kilometrage(request.getKilometrage())
                .remarque(request.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        BigDecimal montantTotalHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null) {
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if(lp.getPieceId() != null) {
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
                if(lm.getMainDoeuvreId() != null) {
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

        NoteDePrix saved = noteDePrixRepository.save(note);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public NoteDePrixResponse updateNoteDePrix(Long id, NoteDePrixRequest request) {
        NoteDePrix note = noteDePrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note de prix non trouvée"));

        if (request.getFicheAtelierId() != null) {
            note.setFicheAtelier(ficheAtelierRepository.findById(request.getFicheAtelierId()).orElse(note.getFicheAtelier()));
        }
        if (request.getKilometrage() != null) note.setKilometrage(request.getKilometrage());
        if (request.getRemarque() != null) note.setRemarque(request.getRemarque());

        note.getLignesFacturationPieces().clear();
        note.getLignesFacturationMainDoeuvres().clear();

        BigDecimal montantTotalHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null) {
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if(lp.getPieceId() != null) {
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
                if(lm.getMainDoeuvreId() != null) {
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
    public NoteDePrixResponse getNoteDePrix(Long id) {
        return mapToResponse(noteDePrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note de prix non trouvée")));
    }

    @Override
    public List<NoteDePrixResponse> getAllNotesDePrix() {
        return noteDePrixRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNoteDePrix(Long id) {
        noteDePrixRepository.deleteById(id);
    }

    private NoteDePrixResponse mapToResponse(NoteDePrix note) {
        return NoteDePrixResponse.builder()
                .id(note.getId())
                .numero(note.getNumero())
                .dateCreation(note.getDateCreation())
                .montantHT(note.getMontantHT())
                .montantTotal(note.getMontantTotal())
                .clientId(note.getFicheAtelier() != null && note.getFicheAtelier().getVehicule() != null && note.getFicheAtelier().getVehicule().getClient() != null ? note.getFicheAtelier().getVehicule().getClient().getId() : null)
                .clientNom(note.getFicheAtelier() != null && note.getFicheAtelier().getVehicule() != null && note.getFicheAtelier().getVehicule().getClient() != null ? note.getFicheAtelier().getVehicule().getClient().getFirstName() + " " + note.getFicheAtelier().getVehicule().getClient().getLastName() : null)
                .vehiculeId(note.getFicheAtelier() != null && note.getFicheAtelier().getVehicule() != null ? note.getFicheAtelier().getVehicule().getId() : null)
                .vehiculeImmatriculation(note.getFicheAtelier() != null && note.getFicheAtelier().getVehicule() != null ? note.getFicheAtelier().getVehicule().getImmatriculation() : null)
                .kilometrage(note.getKilometrage())
                .remarque(note.getRemarque())
                .statut(note.getStatut() != null ? note.getStatut().name() : null)
                .lignesPieces(note.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getReference() : "Pièce inconnue")
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getPrix() * lp.getQuantite())
                                .build())
                        .collect(Collectors.toList()))
                .lignesMainDoeuvres(note.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(lm.getId())
                                .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null ? lm.getMainDoeuvre().getCategorie().getNom() : "Main d'œuvre")
                                .nbreHeure(lm.getNbreHeure())
                                .tarifHoraire(lm.getTarifHoraire())
                                .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
