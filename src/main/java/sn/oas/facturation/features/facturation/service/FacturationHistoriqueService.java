package sn.oas.facturation.features.facturation.service;

import sn.oas.facturation.features.facturation.data.enums.TypeHistoriqueFacturation;
import sn.oas.facturation.features.facturation.dto.FacturationHistoriqueResponseDto;

import java.time.LocalDate;

public interface FacturationHistoriqueService {

    FacturationHistoriqueResponseDto getHistorique(
            TypeHistoriqueFacturation type,
            LocalDate date,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long clientId,
            String clientNumero,
            String keyword,
            Integer page,
            Integer size);
}
