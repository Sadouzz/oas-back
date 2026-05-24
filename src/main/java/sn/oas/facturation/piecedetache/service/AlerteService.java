package sn.oas.facturation.piecedetache.service;

import sn.oas.facturation.piecedetache.dto.AlerteStockResponse;

import java.util.List;

public interface AlerteService {

    List<AlerteStockResponse> getAlertes();

    List<AlerteStockResponse> getRuptures();

    List<AlerteStockResponse> getStocksFaibles();
}
