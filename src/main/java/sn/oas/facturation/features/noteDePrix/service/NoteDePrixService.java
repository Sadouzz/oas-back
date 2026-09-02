package sn.oas.facturation.features.noteDePrix.service;

import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;

import java.util.List;

public interface NoteDePrixService {
    NoteDePrixResponse createNoteDePrix(NoteDePrixRequest request);
    NoteDePrixResponse updateNoteDePrix(Long id, NoteDePrixRequest request);
    NoteDePrixResponse getNoteDePrix(Long id);
    List<NoteDePrixResponse> getAllNotesDePrix();
    void deleteNoteDePrix(Long id);
}
