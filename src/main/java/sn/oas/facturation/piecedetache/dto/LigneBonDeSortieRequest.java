package sn.oas.facturation.piecedetache.dto;

public record LigneBonDeSortieRequest(
        Long pieceId,
        Integer quantite
) {}
