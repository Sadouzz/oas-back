package sn.oas.facturation.features.tva.service;

import sn.oas.facturation.features.tva.data.entity.RapportTvaMensuel;
import sn.oas.facturation.features.tva.dto.TvaDeclarationResponseDto;
import sn.oas.facturation.features.tva.dto.TvaRapportUpdateRequest;

import java.time.YearMonth;

public interface TvaDeclarationService {

    TvaDeclarationResponseDto getDeclaration(
            YearMonth month,
            int pageVendu,
            int pageAvoirVendu,
            int pageAchete,
            int pageAvoirAchete,
            int size);

    byte[] generateDeclarationPdf(YearMonth month);

    RapportTvaMensuel updateRapport(TvaRapportUpdateRequest request);
}
