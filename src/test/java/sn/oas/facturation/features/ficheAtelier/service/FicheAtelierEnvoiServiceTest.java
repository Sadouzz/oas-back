package sn.oas.facturation.features.ficheAtelier.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.notification.service.EmailService;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FicheAtelierEnvoiServiceTest {
    @Mock FicheAtelierRepository repository;
    @Mock FicheAtelierPdfService pdfService;
    @Mock EmailService emailService;
    @InjectMocks FicheAtelierEnvoiService service;

    @Test
    void emailContientLePdfSigne() {
        FicheAtelier fiche = FicheAtelier.builder().id(7L).numero("FA-7")
                .client(Client.builder().email("client@example.test").build()).build();
        byte[] pdf = "%PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(repository.findById(7L)).thenReturn(Optional.of(fiche));
        when(pdfService.generer(7L)).thenReturn(pdf);

        service.envoyerApresCreation(new FicheAtelierCreee(7L));

        verify(emailService).sendEmailWithAttachment(eq("client@example.test"),
                contains("FA-7"), anyString(), eq("Fiche-Atelier-FA-7.pdf"), same(pdf), eq("application/pdf"));
    }

    @Test
    void absenceEmailNeProvoquePasUnEnvoi() {
        FicheAtelier fiche = FicheAtelier.builder().id(7L).numero("FA-7")
                .client(Client.builder().build()).build();
        when(repository.findById(7L)).thenReturn(Optional.of(fiche));

        service.envoyerApresCreation(new FicheAtelierCreee(7L));

        verifyNoInteractions(pdfService, emailService);
    }
}
