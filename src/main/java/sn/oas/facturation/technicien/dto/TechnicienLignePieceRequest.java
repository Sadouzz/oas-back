package sn.oas.facturation.technicien.dto;

/**
 * Proposition d'une ligne pièce par un technicien depuis le portail technicien : pas de champ
 * prix (le technicien ne fixe jamais le prix, cf. TechnicienPortalService).
 */
public record TechnicienLignePieceRequest(Long pieceId, Integer quantite) {}
