package sn.oas.facturation.features.piecedetache.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.piecedetache.dto.AlerteStockResponse;

import java.util.List;

public interface AlerteService {

    List<AlerteStockResponse> getAlertes();
    Page<AlerteStockResponse> getAlertes(int page, int size);

    List<AlerteStockResponse> getRuptures();
    Page<AlerteStockResponse> getRuptures(int page, int size);

    List<AlerteStockResponse> getStocksFaibles();
    Page<AlerteStockResponse> getStocksFaibles(int page, int size);
}
