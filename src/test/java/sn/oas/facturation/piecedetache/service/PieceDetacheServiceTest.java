package sn.oas.facturation.piecedetache.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.piecedetache.data.entity.PDG;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PieceDetacheServiceTest {

    @Mock
    private PieceDetacheRepository pieceDetacheRepository;

    @InjectMocks
    private PieceDetacheServiceImpl pieceDetacheService;

    @Test
    void create_pdg_savesSuccessfully() {
        PieceDetacheRequest request = new PieceDetacheRequest(
                TypePiece.PDG,
                "SN-PDG-001",
                "REF-001",
                "Moteur",
                10.0,
                StatutPiece.ACTIF,
                null, null, null
        );

        when(pieceDetacheRepository.existsByNumeroDeSerie("SN-PDG-001")).thenReturn(false);
        when(pieceDetacheRepository.save(any(PDG.class))).thenAnswer(invocation -> {
            PDG saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var result = pieceDetacheService.create(request);

        assertNotNull(result);
        assertEquals("SN-PDG-001", result.getNumeroDeSerie());
        verify(pieceDetacheRepository).save(any(PDG.class));
    }

    @Test
    void create_duplicateNumeroDeSerie_throwsIllegalArgumentException() {
        PieceDetacheRequest request = new PieceDetacheRequest(
                TypePiece.PDS,
                "SN-DUP",
                "REF-002",
                "Carrosserie",
                5.0,
                StatutPiece.ACTIF,
                null, null, null
        );

        when(pieceDetacheRepository.existsByNumeroDeSerie("SN-DUP")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> pieceDetacheService.create(request));
        verify(pieceDetacheRepository, never()).save(any());
    }

    @Test
    void getById_notFound_throwsRuntimeException() {
        when(pieceDetacheRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pieceDetacheService.getById(99L));
    }
}
