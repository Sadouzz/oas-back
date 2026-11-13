package sn.oas.facturation.features.noteDePrix.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;

import java.util.List;

public interface NoteDePrixService {
    NoteDePrix createNoteDePrix(NoteDePrixRequest request);
    NoteDePrix updateNoteDePrix(Long id, NoteDePrixRequest request);
    NoteDePrix getNoteDePrix(Long id);
    List<NoteDePrix> getAllNotesDePrix();
    Page<NoteDePrix> getAllNotesDePrix(int page, int size);
    Page<NoteDePrix> getAllFiltered(
            String keyword,
            String numero,
            String immatriculation,
            Long clientId,
            String clientNumero,
            String clientMatricule,
            String clientKeyword,
            String statut,
            java.time.LocalDate dateDebut,
            java.time.LocalDate dateFin,
            Long ordreReparationId,
            int page,
            int size);
    Page<NoteDePrix> searchNotesDePrix(String keyword, int page, int size);
    List<NoteDePrix> searchNotesDePrix(String keyword);
    void deleteNoteDePrix(Long id);
}
