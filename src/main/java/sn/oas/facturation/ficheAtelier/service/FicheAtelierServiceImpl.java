package sn.oas.facturation.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;
import sn.oas.facturation.ficheAtelier.data.entity.LigneFicheAtelierPiece;
import sn.oas.facturation.ficheAtelier.data.entity.LigneFicheAtelierMainDoeuvre;
import sn.oas.facturation.ficheAtelier.dto.LigneFicheAtelierPieceRequest;
import sn.oas.facturation.ficheAtelier.dto.LigneFicheAtelierMainDoeuvreRequest;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FicheAtelierServiceImpl implements FicheAtelierService {

    private final FicheAtelierRepository ficheAtelierRepository;
    private final VehiculeRepository vehiculeRepository;
    private final MecanicienRepository mecanicienRepository;
    private final sn.oas.facturation.proforma.repository.ProformaRepository proformaRepository;
    private final sn.oas.facturation.piecedetache.repository.PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Override
    public FicheAtelier createFicheAtelier(FicheAtelierRequest request) {
        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        } else {
            throw new RuntimeException("L'ID du véhicule est obligatoire");
        }

        String numero = request.getNumero();
        if (numero == null || numero.trim().isEmpty()) {
            String prefix = String.format("FA-%d-", java.time.Year.now().getValue());
            FicheAtelier derniereFiche = ficheAtelierRepository.findTopByNumeroStartingWithOrderByNumeroDesc(prefix);
            long nextId = 1;
            if (derniereFiche != null && derniereFiche.getNumero() != null && derniereFiche.getNumero().startsWith(prefix)) {
                try {
                    String lastSeq = derniereFiche.getNumero().substring(prefix.length());
                    nextId = Long.parseLong(lastSeq) + 1;
                } catch (NumberFormatException e) {
                    nextId = derniereFiche.getId() + 1;
                }
            }
            numero = String.format("%s%04d", prefix, nextId);
        }

        FicheAtelier ficheAtelier = FicheAtelier.builder()
                .numero(numero)
                .descriptionTravaux(request.getDescriptionTravaux())
                .listeReception(request.getListeReception())
                .listeDefauts(request.getListeDefauts())
                .dateSortie(request.getDateSortie())
                .vehicule(vehicule)
                .statut(request.getStatut() != null ? request.getStatut() : StatutReparation.A_FAIRE)
                .build();

        if (request.getLignesPieces() != null) {
            for (LigneFicheAtelierPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                PDP pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                ficheAtelier.getLignesFicheAtelierPieces().add(LigneFicheAtelierPiece.builder()
                        .ficheAtelier(ficheAtelier)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(ligneReq.prix() != null ? ligneReq.prix() : (pdp.getPrix() != null ? pdp.getPrix().intValue() : 0))
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneFicheAtelierMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ficheAtelier.getLignesFicheAtelierMainDoeuvres().add(LigneFicheAtelierMainDoeuvre.builder()
                        .ficheAtelier(ficheAtelier)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix() : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        return ficheAtelierRepository.save(ficheAtelier);
    }

    @Override
    public List<FicheAtelier> getAllFichesAtelier() {
        return ficheAtelierRepository.findAll();
    }

    @Override
    public Optional<FicheAtelier> getFicheAtelierById(Long id) {
        return ficheAtelierRepository.findById(id);
    }

    @Override
    public FicheAtelier updateFicheAtelier(Long id, FicheAtelierRequest request) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        
        if (request.getNumero() != null) ficheAtelier.setNumero(request.getNumero());
        if (request.getDescriptionTravaux() != null) ficheAtelier.setDescriptionTravaux(request.getDescriptionTravaux());
        if (request.getListeReception() != null) ficheAtelier.setListeReception(request.getListeReception());
        if (request.getListeDefauts() != null) ficheAtelier.setListeDefauts(request.getListeDefauts());
        if (request.getDateSortie() != null) ficheAtelier.setDateSortie(request.getDateSortie());
        if (request.getStatut() != null) ficheAtelier.setStatut(request.getStatut());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ficheAtelier.setVehicule(vehicule);
        }

        if (request.getLignesPieces() != null) {
            ficheAtelier.getLignesFicheAtelierPieces().clear();
            for (LigneFicheAtelierPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                PDP pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                ficheAtelier.getLignesFicheAtelierPieces().add(LigneFicheAtelierPiece.builder()
                        .ficheAtelier(ficheAtelier)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(ligneReq.prix() != null ? ligneReq.prix() : (pdp.getPrix() != null ? pdp.getPrix().intValue() : 0))
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            ficheAtelier.getLignesFicheAtelierMainDoeuvres().clear();
            for (LigneFicheAtelierMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ficheAtelier.getLignesFicheAtelierMainDoeuvres().add(LigneFicheAtelierMainDoeuvre.builder()
                        .ficheAtelier(ficheAtelier)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix() : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }
        
        return ficheAtelierRepository.save(ficheAtelier);
    }

    @Override
    public void deleteFicheAtelier(Long id) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        ficheAtelierRepository.delete(ficheAtelier);
    }

    @Transactional
    @Override
    public void assignMecanicien(Long ficheId, Long mecanicienId) {
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        if (!fiche.getMecaniciens().contains(mecanicien)) {
            fiche.getMecaniciens().add(mecanicien);
            ficheAtelierRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeMecanicien(Long ficheId, Long mecanicienId) {
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        fiche.getMecaniciens().remove(mecanicien);
        ficheAtelierRepository.save(fiche);
    }

    @Transactional
    @Override
    public FicheAtelier updateStatut(Long id, String statut) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StatutReparation newStatut;
        try {
            newStatut = StatutReparation.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + statut);
        }

        // Si la réparation se termine ou est livrée directement, on déduit les pièces du proforma du stock de l'atelier
        if ((newStatut == StatutReparation.TERMINE || newStatut == StatutReparation.LIVRE)
                && fiche.getStatut() != StatutReparation.TERMINE
                && fiche.getStatut() != StatutReparation.LIVRE) {
            proformaRepository.findByFicheAtelierId(id).ifPresent(proforma -> {
                for (sn.oas.facturation.facturation.data.entity.LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
                    sn.oas.facturation.piecedetache.data.entity.PieceDetache piece = pieceDetacheRepository.findById(lp.getPiece().getId()).orElse(null);
                    if (piece != null && piece instanceof sn.oas.facturation.piecedetache.data.entity.PDP pdp) {
                        int currentAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0;
                        int quantiteUtilisee = lp.getQuantite();
                        pdp.setStockAtelier(Math.max(0, currentAtelier - quantiteUtilisee));
                        pdp.setQteReelle((pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + pdp.getStockAtelier());
                        pieceDetacheRepository.save(pdp);
                    }
                }
            });
        }

        fiche.setStatut(newStatut);
        return ficheAtelierRepository.save(fiche);
    }
}
