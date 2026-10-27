package sn.oas.facturation.features.ordreReparation.service;

import org.junit.jupiter.api.Test;
import sn.oas.facturation.features.diagnostic.data.entity.Diagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.diagnostic.repository.DiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.RemarqueDiagnosticRepository;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepDiagnosticResponseDto;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestitutionVehiculeTest {
    @Mock OrdreReparationRepository ordreRepository;
    @Mock FicheAtelierRepository ficheRepository;
    @Mock BonDeLivraisonRepository bonDeLivraisonRepository;
    @Mock FactureRepository factureRepository;
    @Mock DiagnosticRepository diagnosticRepository;
    @Mock RemarqueDiagnosticRepository remarqueDiagnosticRepository;
    @Mock PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    @InjectMocks OrdreReparationServiceImpl service;

    private static final String SIGNATURE = "data:image/png;base64,iVBORw0KGgo=";

    @Test
    void livraisonEnregistreSignatureDateEtGarantieParDefaut() {
        FicheAtelier fiche = FicheAtelier.builder().id(9L).build();
        OrdreReparation ordre = OrdreReparation.builder().id(3L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).ficheAtelier(fiche).build();
        when(ordreRepository.findByIdForRestitution(3L)).thenReturn(Optional.of(ordre));
        when(ordreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        autoriserLivraison(3L);

        OrdreReparation resultat = service.restituerVehicule(3L, SIGNATURE, null);

        assertEquals(StatutOrdreReparation.LIVRE, resultat.getStatut());
        assertEquals(SIGNATURE, fiche.getSignatureSortieBase64());
        assertEquals(1, fiche.getGarantieMois());
        assertNotNull(resultat.getDateRestitution());
        assertEquals(resultat.getDateRestitution(), resultat.getDateSortie());
        assertEquals(SIGNATURE, resultat.getSignatureRestitutionBase64());
        verify(ficheRepository).save(fiche);
    }

    @Test
    void livraisonRefuseeAvantStatutPret() {
        OrdreReparation ordre = OrdreReparation.builder().id(3L)
                .statut(StatutOrdreReparation.ORDRE_DE_TRAVAIL).ficheAtelier(FicheAtelier.builder().build()).build();
        when(ordreRepository.findByIdForRestitution(3L)).thenReturn(Optional.of(ordre));

        assertThrows(IllegalArgumentException.class, () -> service.restituerVehicule(3L, SIGNATURE, 1));
        verify(ordreRepository, never()).save(any());
    }

    @Test
    void livraisonRefuseeSansSignature() {
        OrdreReparation ordre = OrdreReparation.builder().id(3L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).ficheAtelier(FicheAtelier.builder().build()).build();
        when(ordreRepository.findByIdForRestitution(3L)).thenReturn(Optional.of(ordre));
        autoriserLivraison(3L);

        assertThrows(IllegalArgumentException.class, () -> service.restituerVehicule(3L, "", 1));
        verify(ficheRepository, never()).save(any());
    }

    @Test
    void livraisonGeneriqueRefuseePourUneFicheAtelier() {
        OrdreReparation ordre = OrdreReparation.builder().id(3L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).ficheAtelier(FicheAtelier.builder().build()).build();
        when(ordreRepository.findByIdForFacturation(3L)).thenReturn(Optional.of(ordre));

        assertThrows(IllegalArgumentException.class, () -> service.updateStatut(3L, "LIVRE"));
        verify(ordreRepository, never()).save(any());
    }

    @Test
    void miseAJourEtapeNeContournePasLaSignature() {
        OrdreReparation ordre = OrdreReparation.builder().id(3L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).ficheAtelier(FicheAtelier.builder().build()).build();
        StepLivraisonDto demande = new StepLivraisonDto();
        demande.setStatut(StatutOrdreReparation.LIVRE);
        when(ordreRepository.findById(3L)).thenReturn(Optional.of(ordre));

        assertThrows(IllegalArgumentException.class, () -> service.updateStepLivraison(3L, demande));
        verify(ordreRepository, never()).save(any());
    }

    @Test
    void ordreSansFicheExigeAussiSignatureEtGarantie() {
        OrdreReparation ordre = OrdreReparation.builder().id(4L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).build();
        when(ordreRepository.findByIdForFacturation(4L)).thenReturn(Optional.of(ordre));
        when(ordreRepository.findByIdForRestitution(4L)).thenReturn(Optional.of(ordre));
        when(ordreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        autoriserLivraison(4L);

        assertThrows(IllegalArgumentException.class, () -> service.updateStatut(4L, "LIVRE"));
        OrdreReparation resultat = service.restituerVehicule(4L, SIGNATURE, 3);

        assertEquals(StatutOrdreReparation.LIVRE, resultat.getStatut());
        assertEquals(3, resultat.getGarantieMois());
        assertNotNull(resultat.getDateRestitution());
        verify(ficheRepository, never()).save(any());
    }
    @Test
    void livraisonRefuseeSansBonDeLivraison() {
        OrdreReparation ordre = OrdreReparation.builder().id(12L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).build();
        when(ordreRepository.findByIdForRestitution(12L)).thenReturn(Optional.of(ordre));
        when(bonDeLivraisonRepository.existsByOrdreReparationId(12L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.restituerVehicule(12L, SIGNATURE, 1));
        verify(factureRepository, never()).findFirstByOrdreReparationId(12L);
        verify(ordreRepository, never()).save(any());
    }

    @Test
    void livraisonRefuseeSansFacture() {
        OrdreReparation ordre = OrdreReparation.builder().id(13L)
                .statut(StatutOrdreReparation.PRET_A_LIVRER).build();
        when(ordreRepository.findByIdForRestitution(13L)).thenReturn(Optional.of(ordre));
        when(bonDeLivraisonRepository.existsByOrdreReparationId(13L)).thenReturn(true);
        when(factureRepository.findFirstByOrdreReparationId(13L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.restituerVehicule(13L, SIGNATURE, 1));
        verify(ordreRepository, never()).save(any());
    }

    private void autoriserLivraison(Long ordreId) {
        when(bonDeLivraisonRepository.existsByOrdreReparationId(ordreId)).thenReturn(true);
        when(factureRepository.findFirstByOrdreReparationId(ordreId)).thenReturn(Optional.of(Facture.builder().build()));
    }

    @Test
    void testStepDiagnosticContientDiagnosticEtTechniciens() {
        Technicien tech = Technicien.builder().id(5L).firstName("Moussa").lastName("Diop").phone("771234567").build();
        Diagnostic diag = Diagnostic.builder()
                .pannesDetectees("Plaquettes de frein usées")
                .observations("Disques intacts")
                .technicien(tech)
                .statut(StatutDiagnostic.TERMINE)
                .build();
        diag.setId(10L);
        OrdreReparation ordre = OrdreReparation.builder().id(67L).numero("DK-OR-2026-0059").statut(StatutOrdreReparation.ORDRE_DE_TRAVAIL).listeDefauts("Freinage").build();

        when(ordreRepository.findById(67L)).thenReturn(Optional.of(ordre));
        when(diagnosticRepository.findByOrdreReparationId(67L)).thenReturn(Optional.of(diag));

        StepDiagnosticResponseDto res = service.getStepDiagnostic(67L);

        assertNotNull(res);
        assertNotNull(res.getDiagnostic());
        assertEquals(10L, res.getDiagnostic().getId());
        assertEquals("Plaquettes de frein usées", res.getDiagnostic().getPannesDetectees());
        assertEquals("Moussa Diop", res.getDiagnostic().getTechnicienNom());
        assertNotNull(res.getTechniciensAssocies());
        assertEquals(1, res.getTechniciensAssocies().size());
        assertEquals("Diop", res.getTechniciensAssocies().get(0).getNom());
    }
}
