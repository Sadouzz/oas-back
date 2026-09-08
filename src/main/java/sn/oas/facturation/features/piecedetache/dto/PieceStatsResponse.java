package sn.oas.facturation.features.piecedetache.dto;

public record PieceStatsResponse(
        long totalArticles,
        double valeurStock,
        long stockCritique,
        long ruptures) {
}
