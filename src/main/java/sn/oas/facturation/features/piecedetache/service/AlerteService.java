package sn.oas.facturation.features.piecedetache.service;

import sn.oas.facturation.features.piecedetache.dto.AlerteStockResponse;

import java.util.List;

public interface AlerteService {

    List<AlerteStockResponse> getAlertes();

    List<AlerteStockResponse> getRuptures();

    List<AlerteStockResponse> getStocksFaibles();
}
