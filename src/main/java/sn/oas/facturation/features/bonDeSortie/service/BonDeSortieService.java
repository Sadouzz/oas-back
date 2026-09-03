package sn.oas.facturation.features.bonDeSortie.service;

import org.springframework.data.domain.Page;
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
    Page<BonDeSortie> getAll(int page, int size);

    List<BonDeSortie> getByStatut(StatutBon statut);
    Page<BonDeSortie> getByStatut(StatutBon statut, int page, int size);

    List<BonDeSortie> getByClient(Long clientId);
    Page<BonDeSortie> getByClient(Long clientId, int page, int size);

    List<BonDeSortie> getByVehicule(Long vehiculeId);
    Page<BonDeSortie> getByVehicule(Long vehiculeId, int page, int size);

    List<BonDeSortie> search(String keyword);
    Page<BonDeSortie> search(String keyword, int page, int size);

    BonDeSortie retournerPiece(Long id, Long pieceId);

    List<BonDeSortieHistorique> getHistorique(Long id);
    Page<BonDeSortieHistorique> getHistorique(Long id, int page, int size);

    List<BonDeSortieHistorique> getAllHistorique();
    Page<BonDeSortieHistorique> getAllHistorique(int page, int size);

    List<BonDeSortieHistorique> searchHistorique(String keyword);
    Page<BonDeSortieHistorique> searchHistorique(String keyword, int page, int size);
}