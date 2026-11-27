package sn.oas.facturation.features.recu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.recu.data.entity.LigneVersementClient;
import sn.oas.facturation.features.recu.data.entity.RecuFacture;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.data.entity.VersementClient;
import sn.oas.facturation.features.recu.dto.RecuDetailResponse;
import sn.oas.facturation.features.recu.dto.RecuListItemResponse;
import sn.oas.facturation.features.recu.dto.VersementClientRequest;
import sn.oas.facturation.features.recu.repository.RecuRepository;
import sn.oas.facturation.features.recu.repository.RecuFactureRepository;
import sn.oas.facturation.features.recu.repository.VersementClientRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuServiceImplTest {

    @Mock ClientRepository clientRepository;
    @Mock FactureRepository factureRepository;
    @Mock NoteDePrixRepository noteDePrixRepository;
    @Mock RecuFactureRepository recuFactureRepository;
    @Mock RecuRepository recuRepository;
    @Mock VersementClientRepository versementClientRepository;
    @Mock OrdreReparationRepository ordreReparationRepository;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock NotificationService notificationService;
    @Mock AgentNotificationService agentNotificationService;
    @Mock PortefeuilleClientService portefeuilleClientService;

    @InjectMocks RecuServiceImpl service;

    Client client;
    Facture ancienneFacture;
    NoteDePrix note;
    Facture recenteFacture;
    Recu recuEnCours;

    @BeforeEach
    void setUp() {
        client = Client.builder().firstName("Ousman").lastName("SADJO").phone("772684569").build();
        client.setMatricule("CL-0042");
        client.setId(1667L);
        ancienneFacture = facture(1L, "FC-1", "2026-05-27T20:58", "119775");
        note = NoteDePrix.builder().numero("NP-1").montantTotal(new BigDecimal("12001"))
                .montantPaye(BigDecimal.ZERO).resteAPayer(new BigDecimal("12001"))
                .dateCreation(LocalDateTime.parse("2026-05-27T21:16")).build();
        note.setId(7800L);
        recenteFacture = facture(2L, "FC-2", "2026-11-13T00:49", "295");
        recuEnCours = Recu.builder().id(117L).numero("OAS-RE-2026-0117").client(client)
                .dateDebut(LocalDateTime.parse("2026-11-25T23:19")).build();

        lenient().when(clientRepository.findById(1667L)).thenReturn(Optional.of(client));
        lenient().when(recuRepository.findByIdForUpdate(117L)).thenReturn(Optional.of(recuEnCours));
        lenient().when(recuRepository.findById(117L)).thenReturn(Optional.of(recuEnCours));
        lenient().when(recuRepository.save(any(Recu.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(documentNumberGeneratorService.generateNextNumber(DocumentType.RC)).thenReturn("RC-1", "RC-2");
        lenient().when(recuFactureRepository.save(any(RecuFacture.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(factureRepository.findByIdForPaiement(1L)).thenReturn(Optional.of(ancienneFacture));
        lenient().when(factureRepository.findByIdForPaiement(2L)).thenReturn(Optional.of(recenteFacture));
        lenient().when(noteDePrixRepository.findByIdForPaiement(7800L)).thenReturn(Optional.of(note));
        lenient().when(noteDePrixRepository.save(any(NoteDePrix.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(versementClientRepository.save(any(VersementClient.class))).thenAnswer(inv -> {
            VersementClient v = inv.getArgument(0);
            v.setId(500L + recuEnCours.getVersements().size());
            return v;
        });
        // Les requêtes d'impayés suivent l'état des documents, comme en base.
        lenient().when(factureRepository.findFacturesImpayeesByClientId(1667L)).thenAnswer(inv ->
                new ArrayList<>(List.of(ancienneFacture, recenteFacture).stream().filter(this::impayee).toList()));
        lenient().when(noteDePrixRepository.findNotesImpayeesByClientId(1667L)).thenAnswer(inv ->
                note.getResteAPayer().signum() > 0 ? List.of(note) : List.of());
    }

    @Test
    void ouvreUnReçuEnCoursAvecLesDocumentsImpayesDuClient() {
        when(recuRepository.findFirstByClientIdAndDateFinIsNullOrderByIdDesc(1667L)).thenReturn(Optional.empty());

        when(documentNumberGeneratorService.generateNextNumber(DocumentType.RE)).thenReturn("OAS-RE-2026-0001");

        RecuDetailResponse recu = service.ouvrirRecu(1667L);

        assertEquals("OAS-RE-2026-0001", recu.numeroRecu());

        assertTrue(recu.enCours());
        assertEquals("Ousman SADJO", recu.nomClient());
        assertEquals(new BigDecimal("132071"), recu.montant());
        assertEquals(0, recu.montantPaye().signum());
        assertEquals(new BigDecimal("120070"), recu.totalFactures());
        assertEquals("FC-2", recu.factures().get(0).numero()); // plus récente d'abord, comme l'ancien écran
        assertEquals(1, recu.notesDePrix().size());
    }

    @Test
    void refuseDOuvrirUnReçuSansDocumentAPayer() {
        ancienneFacture.setResteAPayer(BigDecimal.ZERO);
        recenteFacture.setResteAPayer(BigDecimal.ZERO);
        note.setResteAPayer(BigDecimal.ZERO);
        when(recuRepository.findFirstByClientIdAndDateFinIsNullOrderByIdDesc(1667L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.ouvrirRecu(1667L));
        verify(recuRepository, never()).save(any());
    }

    @Test
    void versementPartielRepartiDuPlusAncienAuPlusRecentEtLaisseLeReçuEnCours() {
        // 119775 (facture ancienne) + 12001 (note) + 100 sur la facture récente
        RecuDetailResponse recu = service.enregistrerVersement(117L,
                new VersementClientRequest(new BigDecimal("131876"), " espece ", null));

        assertEquals(StatutPaiement.PAYE, ancienneFacture.getStatutPaiement());
        assertEquals(StatutPaiement.PAYE, note.getStatutPaiement());
        assertEquals(StatutPaiement.PARTIEL, recenteFacture.getStatutPaiement());
        assertEquals(new BigDecimal("195"), recenteFacture.getResteAPayer());

        ArgumentCaptor<VersementClient> versement = ArgumentCaptor.forClass(VersementClient.class);
        verify(versementClientRepository).save(versement.capture());
        assertSame(recuEnCours, versement.getValue().getRecu());
        List<LigneVersementClient> lignes = versement.getValue().getLignes();
        assertEquals(3, lignes.size());
        assertEquals("RC-1", lignes.get(0).getRecuFacture().getNumero());
        assertEquals("ESPECE", versement.getValue().getModePaiement());
        assertSame(note, lignes.get(1).getNoteDePrix());
        assertEquals(new BigDecimal("100"), lignes.get(2).getMontant());

        assertTrue(recu.enCours());
        assertEquals(new BigDecimal("131876"), recu.montantPaye());
        assertEquals(new BigDecimal("132071"), recu.montant());
        assertEquals(new BigDecimal("195"), recu.resteAPayer());
        assertEquals(3, recu.factures().size() + recu.notesDePrix().size());
        verify(portefeuilleClientService, never()).debiterPourFacture(any(), any(), any());
    }

    @Test
    void versementQuiSoldeToutClotureLeReçuPuisValidation() {
        RecuDetailResponse recu = service.enregistrerVersement(117L,
                new VersementClientRequest(new BigDecimal("132071"), null, null));

        assertFalse(recu.enCours());
        assertNotNull(recuEnCours.getDateFin());
        assertEquals(new BigDecimal("132071"), recu.montant());
        assertEquals(0, recu.resteAPayer().signum());
        // Le reçu payé garde ses documents, retrouvés par ses versements.
        assertEquals(2, recu.factures().size());

        assertThrows(BadRequestException.class, () -> service.enregistrerVersement(117L,
                new VersementClientRequest(BigDecimal.ONE, null, null)));
        assertThrows(BadRequestException.class, () -> service.supprimer(117L));

        RecuDetailResponse valide = service.valider(117L);
        assertTrue(valide.valide());
        assertThrows(BadRequestException.class, () -> service.valider(117L));
    }

    @Test
    void factureSoldeeSansSecondTimbreEtFicheAtelierPreteALivrer() {
        OrdreReparation fiche = new OrdreReparation();
        fiche.setStatut(StatutOrdreReparation.PAIEMENT);
        ancienneFacture.setOrdreReparation(fiche);

        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("119775"), null, null));

        ArgumentCaptor<RecuFacture> recu = ArgumentCaptor.forClass(RecuFacture.class);
        verify(recuFactureRepository).save(recu.capture());
        assertEquals(BigDecimal.ZERO, recu.getValue().getMontantTimbre());
        assertEquals(StatutOrdreReparation.FACTURATION, fiche.getStatut());
        verify(ordreReparationRepository).save(fiche);
    }

    @Test
    void soldePrepayeNeRegleQueLesFacturesEtDebiteLePortefeuille() {
        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("120070"), "solde_prepaye", null));

        verify(noteDePrixRepository, never()).findByIdForPaiement(any());
        verify(portefeuilleClientService, times(2)).debiterPourFacture(any(), any(), any());
        assertTrue(recuEnCours.isEnCours()); // la note de prix reste due
        assertThrows(BadRequestException.class, () -> service.enregistrerVersement(117L,
                new VersementClientRequest(BigDecimal.ONE, "SOLDE_PREPAYE", null)));
    }

    @Test
    void refuseUnVersementSuperieurAuRestantOuInvalide() {
        assertThrows(BadRequestException.class, () -> service.enregistrerVersement(117L,
                new VersementClientRequest(new BigDecimal("132072"), null, null)));
        assertThrows(BadRequestException.class, () -> service.enregistrerVersement(117L,
                new VersementClientRequest(BigDecimal.ZERO, null, null)));
        assertThrows(BadRequestException.class, () -> service.enregistrerVersement(117L,
                new VersementClientRequest(new BigDecimal("1.005"), null, null)));
        verify(recuFactureRepository, never()).save(any());
        verify(versementClientRepository, never()).save(any());
    }

    @Test
    void supprimeUnReçuEnCoursSansVersementSeulement() {
        service.supprimer(117L);
        verify(recuRepository).delete(recuEnCours);

        recuEnCours.setMontantPaye(BigDecimal.TEN);
        assertThrows(BadRequestException.class, () -> service.supprimer(117L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listeDesReçusEnCoursRecalculeLeTotal() {
        recuEnCours.setMontantPaye(new BigDecimal("1000"));
        when(recuRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recuEnCours)));

        RecuListItemResponse ligne = service.getRecusEnCours(null, "cl-0042", 0, 10).getContent().get(0);

        assertEquals(117L, ligne.id());
        assertEquals("OAS-RE-2026-0117", ligne.numeroRecu());
        assertEquals("CL-0042", ligne.numeroClient());
        assertEquals(new BigDecimal("133071"), ligne.montant());
        assertEquals(new BigDecimal("1000"), ligne.montantPaye());
    }

    @Test
    void annuleUnVersementEtRemetLesDocumentsAPayer() {
        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("131876"), null, null));
        Long versementId = recuEnCours.getVersements().get(0).getId();
        ArgumentCaptor<RecuFacture> recusRc = ArgumentCaptor.forClass(RecuFacture.class);
        verify(recuFactureRepository, times(2)).save(recusRc.capture());

        RecuDetailResponse recu = service.annulerVersement(117L, versementId);

        assertEquals(StatutPaiement.NON_PAYE, ancienneFacture.getStatutPaiement());
        assertEquals(new BigDecimal("119775"), ancienneFacture.getResteAPayer());
        assertEquals(StatutPaiement.NON_PAYE, recenteFacture.getStatutPaiement());
        assertEquals(new BigDecimal("295"), recenteFacture.getResteAPayer());
        assertEquals(StatutPaiement.NON_PAYE, note.getStatutPaiement());
        assertEquals(new BigDecimal("12001"), note.getResteAPayer());
        // Les reçus RC restent en base, marqués annulés.
        assertTrue(recusRc.getAllValues().stream().allMatch(RecuFacture::isAnnule));
        verify(versementClientRepository).delete(any(VersementClient.class));
        verify(portefeuilleClientService, never()).recrediterPaiementAnnule(any(), any(), any());

        assertTrue(recu.enCours());
        assertEquals(0, recu.montantPaye().signum());
        assertEquals(new BigDecimal("132071"), recu.montant());
        assertTrue(recu.versements().isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> service.annulerVersement(117L, versementId));
    }

    @Test
    void annulerLeVersementDUnReçuPayeLeRouvreSaufSIlEstValideOuRemplace() {
        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("132071"), null, null));
        Long versementId = recuEnCours.getVersements().get(0).getId();
        assertFalse(recuEnCours.isEnCours());

        Recu autre = Recu.builder().id(118L).client(client).build();
        when(recuRepository.findFirstByClientIdAndDateFinIsNullOrderByIdDesc(1667L))
                .thenReturn(Optional.of(autre), Optional.empty());
        assertThrows(BadRequestException.class, () -> service.annulerVersement(117L, versementId));

        recuEnCours.setValide(true);
        assertThrows(BadRequestException.class, () -> service.annulerVersement(117L, versementId));
        recuEnCours.setValide(false);

        RecuDetailResponse recu = service.annulerVersement(117L, versementId);
        assertTrue(recu.enCours());
        assertNull(recuEnCours.getDateFin());
        assertEquals(new BigDecimal("132071"), recu.resteAPayer());
    }

    @Test
    void annulerUnVersementParSoldePrepayeRecreditLePortefeuille() {
        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("120070"), "SOLDE_PREPAYE", null));
        Long versementId = recuEnCours.getVersements().get(0).getId();

        service.annulerVersement(117L, versementId);

        verify(portefeuilleClientService).recrediterPaiementAnnule(eq(ancienneFacture), any(RecuFacture.class), eq(new BigDecimal("119775")));
        verify(portefeuilleClientService).recrediterPaiementAnnule(eq(recenteFacture), any(RecuFacture.class), eq(new BigDecimal("295")));
    }

    @Test
    void genereLePdfDuReçu() {
        service.enregistrerVersement(117L, new VersementClientRequest(new BigDecimal("1000"), "cheque", "acompte"));

        byte[] avecEntete = service.genererPdf(117L, null);
        byte[] sansEntete = service.genererPdf(117L, "sans_entete");

        assertEquals("%PDF", new String(avecEntete, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        assertEquals("%PDF", new String(sansEntete, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        assertThrows(BadRequestException.class, () -> service.genererPdf(117L, "AUTRE"));
        assertEquals("1.250.000 F CFA", RecuPdf.montant(new BigDecimal("1250000")));
    }

    private boolean impayee(Facture f) {
        BigDecimal reste = f.getResteAPayer() != null ? f.getResteAPayer() : f.getMontantTotal().subtract(f.getMontantPaye());
        return reste.signum() > 0;
    }

    private static Facture facture(Long id, String numero, String date, String total) {
        Facture f = Facture.builder().numero(numero).montantTotal(new BigDecimal(total))
                .montantPaye(BigDecimal.ZERO).statutPaiement(StatutPaiement.NON_PAYE)
                .dateCreation(LocalDateTime.parse(date)).build();
        f.setId(id);
        return f;
    }
}
