package sn.oas.facturation.ordreReparation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;
import sn.oas.facturation.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.ordreReparation.dto.LigneOrdreReparationPieceRequest;
import sn.oas.facturation.ordreReparation.dto.LigneOrdreReparationMainDoeuvreRequest;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.notification.service.AgentNotificationService;

import java.util.List;
import java.util.Optional;
import sn.oas.facturation.proforma.service.ProformaService;
import sn.oas.facturation.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.ordreReparation.dto.VehiculeLightDTO;
import sn.oas.facturation.ordreReparation.dto.ClientLightDTO;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdreReparationServiceImpl implements OrdreReparationService {

    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final MecanicienRepository mecanicienRepository;
    private final sn.oas.facturation.proforma.repository.ProformaRepository proformaRepository;
    private final sn.oas.facturation.piecedetache.repository.PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Autowired
    @Lazy
    private ProformaService proformaService;

    @Override
    public OrdreReparation createOrdreReparation(OrdreReparationRequest request) {
        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        } else {
            throw new RuntimeException("L'ID du véhicule est obligatoire");
        }

        String numero = request.getNumero();
        if (numero == null || numero.trim().isEmpty()) {
            numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.OR);
        }

        OrdreReparation ordreReparation = OrdreReparation.builder()
                .numero(numero)
                .descriptionTravaux(request.getDescriptionTravaux())
                .listeReception(request.getListeReception())
                .listeDefauts(request.getListeDefauts())
                .dateSortie(request.getDateSortie())
                .vehicule(vehicule)
                .statut(request.getStatut() != null ? request.getStatut() : StatutOrdreReparation.A_FAIRE)
                .build();

        if (request.getLignesPieces() != null) {
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                PDP pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (pdp.getPrix() != null ? pdp.getPrix().intValue() : 0))
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        return ordreReparationRepository.save(ordreReparation);
    }

    @Override
    public List<OrdreReparationLightDTO> getAllOrdresReparation() {
        return ordreReparationRepository.findAllWithVehiculeAndClient().stream().map(f -> {
            ClientLightDTO clientDTO = null;
            if (f.getVehicule() != null && f.getVehicule().getClient() != null) {
                clientDTO = ClientLightDTO.builder()
                        .id(f.getVehicule().getClient().getId())
                        .firstName(f.getVehicule().getClient().getFirstName())
                        .lastName(f.getVehicule().getClient().getLastName())
                        .phone(f.getVehicule().getClient().getPhone())
                        .build();
            }

            VehiculeLightDTO vehiculeDTO = null;
            if (f.getVehicule() != null) {
                vehiculeDTO = VehiculeLightDTO.builder()
                        .id(f.getVehicule().getId())
                        .immatriculation(f.getVehicule().getImmatriculation())
                        .marque(f.getVehicule().getMarque())
                        .modele(f.getVehicule().getModele())
                        .client(clientDTO)
                        .build();
            }

            return OrdreReparationLightDTO.builder()
                    .id(f.getId())
                    .numero(f.getNumero())
                    .descriptionTravaux(f.getDescriptionTravaux())
                    .dateCreation(f.getDateCreation())
                    .dateSortie(f.getDateSortie())
                    .statut(f.getStatut())
                    .vehicule(vehiculeDTO)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<OrdreReparation> getOrdreReparationById(Long id) {
        return ordreReparationRepository.findById(id);
    }

    @Override
    public OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

        if (request.getNumero() != null)
            ordreReparation.setNumero(request.getNumero());
        if (request.getDescriptionTravaux() != null)
            ordreReparation.setDescriptionTravaux(request.getDescriptionTravaux());
        if (request.getListeReception() != null)
            ordreReparation.setListeReception(request.getListeReception());
        if (request.getListeDefauts() != null)
            ordreReparation.setListeDefauts(request.getListeDefauts());
        if (request.getDateSortie() != null)
            ordreReparation.setDateSortie(request.getDateSortie());
        if (request.getStatut() != null)
            ordreReparation.setStatut(request.getStatut());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ordreReparation.setVehicule(vehicule);
        }

        if (request.getLignesPieces() != null) {
            ordreReparation.getLignesOrdreReparationPieces().clear();
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                PDP pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (pdp.getPrix() != null ? pdp.getPrix().intValue() : 0))
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            ordreReparation.getLignesOrdreReparationMainDoeuvres().clear();
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        ordreReparation = ordreReparationRepository.save(ordreReparation);

        // Auto-create proforma if pieces or MO are added and it doesn't exist yet
        if ((request.getLignesPieces() != null && !request.getLignesPieces().isEmpty()) ||
                (request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty())) {

            if (proformaRepository.findByOrdreReparationId(ordreReparation.getId()).isEmpty()) {
                ProformaCreateRequest pcr = new ProformaCreateRequest();
                pcr.setOrdreReparationId(ordreReparation.getId());
                pcr.setClientId(
                        ordreReparation.getVehicule().getClient() != null ? ordreReparation.getVehicule().getClient().getId()
                                : null);
                pcr.setVehiculeId(ordreReparation.getVehicule().getId());
                pcr.setKilometrage(ordreReparation.getVehicule().getKilometrage() != null
                        ? ordreReparation.getVehicule().getKilometrage()
                        : 0.0);

                if (request.getLignesPieces() != null) {
                    pcr.setLignesPieces(request.getLignesPieces().stream().map(lp -> {
                        LigneFacturationPieceRequest lr = new LigneFacturationPieceRequest();
                        lr.setPieceId(lp.pieceId());
                        lr.setQuantite(lp.quantite());
                        lr.setPrix(lp.prix());
                        return lr;
                    }).collect(Collectors.toList()));
                }

                if (request.getLignesMainDoeuvres() != null) {
                    pcr.setLignesMainDoeuvres(request.getLignesMainDoeuvres().stream().map(lm -> {
                        LigneFacturationMainDoeuvreRequest lmr = new LigneFacturationMainDoeuvreRequest();
                        lmr.setMainDoeuvreId(lm.mainDoeuvreId());
                        lmr.setNbreHeure(lm.nbreHeure());
                        lmr.setTarifHoraire(lm.prix());
                        return lmr;
                    }).collect(Collectors.toList()));
                }

                proformaService.create(pcr);
                // proformaService.create already sets OrdreReparation status to
                // EN_ATTENTE_PROFORMA
            }
        }

        return ordreReparation;
    }

    @Override
    public void deleteOrdreReparation(Long id) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        ordreReparationRepository.delete(ordreReparation);
    }

    @Transactional
    @Override
    public void assignMecanicien(Long ficheId, Long mecanicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        if (!fiche.getMecaniciens().contains(mecanicien)) {
            fiche.getMecaniciens().add(mecanicien);
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeMecanicien(Long ficheId, Long mecanicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        fiche.getMecaniciens().remove(mecanicien);
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    public void assignMecanicienReparation(Long ficheId, Long mecanicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        if (!fiche.getMecaniciensReparation().contains(mecanicien)) {
            fiche.getMecaniciensReparation().add(mecanicien);
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeMecanicienReparation(Long ficheId, Long mecanicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        fiche.getMecaniciensReparation().remove(mecanicien);
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    public OrdreReparation updateStatut(Long id, String statut) {
        OrdreReparation fiche = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StatutOrdreReparation newStatut;
        try {
            newStatut = StatutOrdreReparation.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + statut);
        }

        // Si la réparation commence (EN_COURS), on déduit les pièces
        // du proforma du stock de l'atelier
        if (newStatut == StatutOrdreReparation.EN_COURS && fiche.getStatut() != StatutOrdreReparation.EN_COURS) {
            proformaRepository.findByOrdreReparationId(id).ifPresent(proforma -> {
                for (sn.oas.facturation.facturation.data.entity.LigneFacturationPiece lp : proforma
                        .getLignesFacturationPieces()) {
                    sn.oas.facturation.piecedetache.data.entity.PieceDetache piece = pieceDetacheRepository
                            .findById(lp.getPiece().getId()).orElse(null);
                    if (piece != null && piece instanceof sn.oas.facturation.piecedetache.data.entity.PDP pdp) {
                        int currentAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0;
                        int quantiteUtilisee = lp.getQuantite();
                        pdp.setStockAtelier(Math.max(0, currentAtelier - quantiteUtilisee));
                        pdp.setQteReelle(
                                (pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + pdp.getStockAtelier());
                        pieceDetacheRepository.save(pdp);
                    }
                }
            });
        }

        fiche.setStatut(newStatut);
        OrdreReparation savedFiche = ordreReparationRepository.save(fiche);

        if (newStatut == StatutOrdreReparation.EN_ATTENTE_COMMANDE || newStatut == StatutOrdreReparation.EN_ATTENTE_SORTIE) {
            agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                    "Pièces en attente pour " + savedFiche.getNumero(),
                    "La fiche " + savedFiche.getNumero() + " est passée en " + newStatut + ".");
        }

        return savedFiche;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation> statuts) {
        return ordreReparationRepository.existsByVehiculeIdAndStatutNotIn(vehiculeId, statuts);
    }
}
