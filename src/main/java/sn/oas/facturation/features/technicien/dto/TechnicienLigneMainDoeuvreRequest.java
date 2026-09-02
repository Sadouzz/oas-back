package sn.oas.facturation.features.technicien.dto;

/**
 * Proposition d'une ligne main d'œuvre par un technicien depuis le portail technicien : pas de
 * champ prix (le technicien ne fixe jamais le prix, cf. TechnicienPortalService).
 */
public record TechnicienLigneMainDoeuvreRequest(Long mainDoeuvreId, Integer nbreHeure) {}
