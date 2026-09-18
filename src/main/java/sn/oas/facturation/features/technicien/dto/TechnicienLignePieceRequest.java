package sn.oas.facturation.features.technicien.dto;

/**
 * Proposition d'une ligne pièce par un technicien depuis le portail technicien : pas de champ
 * prix (le technicien ne fixe jamais le prix, cf. TechnicienPortalService).
 * Pour les pièces hors catalogue (PDS), pieceId est null et isCustom est true avec designationPds.
 */
public record TechnicienLignePieceRequest(
        Long pieceId,
        Integer quantite,
        Boolean isCustom,
        String designationPds
) {}
