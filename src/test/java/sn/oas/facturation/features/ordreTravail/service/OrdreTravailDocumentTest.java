package sn.oas.facturation.features.ordreTravail.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import sn.oas.facturation.features.ordreTravail.data.entity.DocumentOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;
import sn.oas.facturation.features.ordreTravail.dto.DocumentOrdreTravailRequestDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.DocumentOrdreTravailResponseDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.StepReparationResponseDto;
import sn.oas.facturation.features.ordreTravail.dto.steps.StepReparationDto;
import sn.oas.facturation.features.ordreTravail.repository.DocumentOrdreTravailRepository;
import sn.oas.facturation.features.ordreTravail.repository.OrdreTravailRepository;
import sn.oas.facturation.features.technicien.repository.TechnicienRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdreTravailDocumentTest {

    @Mock
    private OrdreTravailRepository ordreTravailRepository;

    @Mock
    private DocumentOrdreTravailRepository documentOrdreTravailRepository;

    @Mock
    private TechnicienRepository technicienRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private OrdreTravailServiceImpl service;

    private OrdreTravail ordreTravail;

    @BeforeEach
    void setUp() {
        ordreTravail = OrdreTravail.builder()
                .id(1L)
                .numero("OT-2026-0001")
                .statut(StatutOrdreTravail.REPARATION)
                .build();
    }

    @Test
    void testAddDocumentWithDirectUrl() {
        when(ordreTravailRepository.findById(1L)).thenReturn(Optional.of(ordreTravail));
        when(documentOrdreTravailRepository.save(any(DocumentOrdreTravail.class)))
                .thenAnswer(inv -> {
                    DocumentOrdreTravail doc = inv.getArgument(0);
                    doc.setId(10L);
                    return doc;
                });

        DocumentOrdreTravailRequestDto request = DocumentOrdreTravailRequestDto.builder()
                .nomFichier("moteur_endommage.jpg")
                .url("https://res.cloudinary.com/oas/image/upload/v1/moteur.jpg")
                .type(TypeDocumentOt.PHOTO)
                .remarque("Photo avant début des réparations")
                .etape(4)
                .build();

        DocumentOrdreTravailResponseDto response = service.addDocument(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("moteur_endommage.jpg", response.getNomFichier());
        assertEquals(TypeDocumentOt.PHOTO, response.getType());
        assertEquals(4, response.getEtape());
        verify(documentOrdreTravailRepository, times(1)).save(any(DocumentOrdreTravail.class));
    }

    @Test
    void testGetStepReparationContainsDocuments() {
        when(ordreTravailRepository.findById(1L)).thenReturn(Optional.of(ordreTravail));

        DocumentOrdreTravail doc = DocumentOrdreTravail.builder()
                .id(15L)
                .ordreTravail(ordreTravail)
                .nomFichier("rapport_technique.pdf")
                .url("https://res.cloudinary.com/oas/raw/upload/v1/rapport.pdf")
                .type(TypeDocumentOt.PDF)
                .etape(4)
                .build();

        when(documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(doc));

        StepReparationResponseDto stepResp = service.getStepReparation(1L);

        assertNotNull(stepResp);
        assertEquals(1, stepResp.getDocuments().size());
        assertEquals(1, stepResp.getPiecesJointes().size());
        assertEquals("rapport_technique.pdf", stepResp.getDocuments().get(0).getNomFichier());
        assertEquals(TypeDocumentOt.PDF, stepResp.getDocuments().get(0).getType());
    }

    @Test
    void testUpdateStepReparationSavesDocuments() {
        when(ordreTravailRepository.findById(1L)).thenReturn(Optional.of(ordreTravail));
        when(documentOrdreTravailRepository.save(any(DocumentOrdreTravail.class)))
                .thenAnswer(inv -> {
                    DocumentOrdreTravail doc = inv.getArgument(0);
                    doc.setId(20L);
                    return doc;
                });
        when(documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        StepReparationDto stepDto = new StepReparationDto();
        stepDto.setRapportReparation("Changement de courroie effectué");
        stepDto.setDocuments(List.of(
                DocumentOrdreTravailRequestDto.builder()
                        .nomFichier("facture_piece.pdf")
                        .url("https://res.cloudinary.com/oas/raw/upload/v1/facture.pdf")
                        .type(TypeDocumentOt.PDF)
                        .build()
        ));

        StepReparationDto result = service.updateStepReparation(1L, stepDto);

        assertNotNull(result);
        assertEquals("Changement de courroie effectué", ordreTravail.getRapportReparation());
        verify(documentOrdreTravailRepository, times(1)).save(any(DocumentOrdreTravail.class));
        verify(ordreTravailRepository, times(1)).save(ordreTravail);
    }

    @Test
    void testUploadMultipartDocument() throws Exception {
        when(ordreTravailRepository.findById(1L)).thenReturn(Optional.of(ordreTravail));
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/oas/image/upload/v1/pic.png");
        uploadResult.put("public_id", "oas/ordres-travail/reparation/pic123");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);
        when(documentOrdreTravailRepository.save(any(DocumentOrdreTravail.class)))
                .thenAnswer(inv -> {
                    DocumentOrdreTravail doc = inv.getArgument(0);
                    doc.setId(30L);
                    return doc;
                });

        MockMultipartFile file = new MockMultipartFile("file", "piece_cassee.png", "image/png", "dummy".getBytes());

        DocumentOrdreTravailResponseDto response = service.uploadDocument(1L, file, null, "Photo pièce cassée");

        assertNotNull(response);
        assertEquals(30L, response.getId());
        assertEquals("https://res.cloudinary.com/oas/image/upload/v1/pic.png", response.getUrl());
        assertEquals(TypeDocumentOt.PHOTO, response.getType());
        assertEquals("piece_cassee.png", response.getNomFichier());
        assertEquals(4, response.getEtape());
    }

    @Test
    void testDeleteDocument() {
        DocumentOrdreTravail doc = DocumentOrdreTravail.builder()
                .id(40L)
                .publicId("oas/ordres-travail/reparation/doc40")
                .ordreTravail(ordreTravail)
                .build();

        when(documentOrdreTravailRepository.findById(40L)).thenReturn(Optional.of(doc));
        when(cloudinary.uploader()).thenReturn(uploader);

        service.deleteDocument(40L);

        verify(documentOrdreTravailRepository, times(1)).delete(doc);
    }

    @Test
    void testTypeDocumentOtFromString() {
        assertEquals(TypeDocumentOt.PHOTO, TypeDocumentOt.fromString("jpg"));
        assertEquals(TypeDocumentOt.PHOTO, TypeDocumentOt.fromString("image"));
        assertEquals(TypeDocumentOt.PHOTO, TypeDocumentOt.fromString("PHOTO"));
        assertEquals(TypeDocumentOt.PDF, TypeDocumentOt.fromString("pdf"));
        assertEquals(TypeDocumentOt.DOCUMENT, TypeDocumentOt.fromString("docx"));
        assertEquals(TypeDocumentOt.DOCUMENT, TypeDocumentOt.fromString("word"));
        assertEquals(TypeDocumentOt.AUTRE, TypeDocumentOt.fromString("zip"));
    }
}

