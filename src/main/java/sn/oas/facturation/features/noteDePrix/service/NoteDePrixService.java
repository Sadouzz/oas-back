package sn.oas.facturation.features.noteDePrix.service;

import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;

import java.util.List;

public interface NoteDePrixService {
    NoteDePrix createNoteDePrix(NoteDePrixRequest request);
    NoteDePrix updateNoteDePrix(Long id, NoteDePrixRequest request);
    NoteDePrix getNoteDePrix(Long id);
    List<NoteDePrix> getAllNotesDePrix();
    void deleteNoteDePrix(Long id);
}
