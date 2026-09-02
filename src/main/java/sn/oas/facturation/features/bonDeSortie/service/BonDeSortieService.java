package sn.oas.facturation.features.bonDeSortie.service;

import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieRequest;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortieHistorique;

import java.util.List;

public interface BonDeSortieService {

    BonDeSortie creer(BonDeSortieRequest request);

    BonDeSortie valider(Long id);

    BonDeSortie getById(Long id);

    List<BonDeSortie> getAll();

    List<BonDeSortie> getByStatut(StatutBon statut);

    List<BonDeSortie> getByClient(Long clientId);

    List<BonDeSortie> getByVehicule(Long vehiculeId);

    BonDeSortie retournerPiece(Long id, Long pieceId);

    List<BonDeSortieHistorique> getHistorique(Long id);

    List<BonDeSortieHistorique> getAllHistorique();
}