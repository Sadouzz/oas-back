package sn.oas.facturation.features.bonDeLivraison.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonFactureRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonOrdreContextResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;

import java.util.List;

public interface BonDeLivraisonService {

    Page<BonDeLivraisonResponse> list(int page, int size, String keyword);

    BonDeLivraisonResponse get(Long id);

    byte[] generatePdf(Long id);

    byte[] generatePdf(Long id, String layoutKey);

    List<BonDeSortieDisponibleResponse> bonsDeSortieDisponibles(Long ordreId);

    BonDeLivraisonOrdreContextResponse contexteOrdre(Long ordreId);

    BonDeLivraisonResponse create(BonDeLivraisonCreateRequest req);

    BonDeLivraison creerOuMettreAJourDepuisBonDeSortie(BonDeSortie bs, OrdreTravail ot);

    Facture creerFactureDepuisOrdre(Long ordreId, BonDeLivraisonFactureRequest request);

    Facture convertirEnFacture(Long id);

    Facture convertirEnFacture(Long id, BonDeLivraisonFactureRequest request);

    NoteDePrix convertirEnNoteDePrix(Long id);
}
