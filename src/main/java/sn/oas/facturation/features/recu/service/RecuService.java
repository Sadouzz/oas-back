package sn.oas.facturation.features.recu.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.recu.dto.RecuDetailResponse;
import sn.oas.facturation.features.recu.dto.RecuListItemResponse;
import sn.oas.facturation.features.recu.dto.VersementClientRequest;

public interface RecuService {

    /** Ouvre le reçu en cours du client, ou le met à jour s'il existe déjà. */
    RecuDetailResponse ouvrirRecu(Long clientId);

    RecuDetailResponse getRecu(Long id);

    /** Enregistre un versement et le répartit sur les documents impayés, du plus ancien au plus récent. */
    RecuDetailResponse enregistrerVersement(Long id, VersementClientRequest request);

    /**
     * Annule un versement : ses parts sont retirées des factures et notes de prix, ses reçus RC sont marqués annulés,
     * le solde prépayé est recrédité, et un reçu payé non validé repasse en cours.
     */
    RecuDetailResponse annulerVersement(Long id, Long versementId);

    /** PDF du reçu, avec ou sans en-tête (voir PdfLayout). */
    byte[] genererPdf(Long id, String layoutKey);

    Page<RecuListItemResponse> getRecusEnCours(String numeroRecu, String numeroClient, int page, int size);

    Page<RecuListItemResponse> getRecusPayes(String numeroRecu, String numeroClient, int page, int size);

    /** Valide un reçu payé. */
    RecuDetailResponse valider(Long id);

    /** Supprime un reçu en cours qui n'a reçu aucun versement. */
    void supprimer(Long id);
}
