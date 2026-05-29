package sn.oas.facturation.bonDeSortie.service;

import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest;

import java.util.List;

public interface BonDeSortieService {

    BonDeSortie creer(BonDeSortieRequest request);

    BonDeSortie valider(Long id);

    BonDeSortie getById(Long id);

    List<BonDeSortie> getAll();

    List<BonDeSortie> getByStatut(StatutBon statut);

    List<BonDeSortie> getByClient(Long clientId);

    List<BonDeSortie> getByVehicule(Long vehiculeId);
}