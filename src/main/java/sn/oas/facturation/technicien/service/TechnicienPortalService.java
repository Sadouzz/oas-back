package sn.oas.facturation.technicien.service;

import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.technicien.dto.PannesRequest;
import sn.oas.facturation.technicien.dto.TechnicienLigneMainDoeuvreRequest;
import sn.oas.facturation.technicien.dto.TechnicienLignePieceRequest;

import java.util.List;

/**
 * Portail technicien (self-service). Chaque méthode qui touche à un ordre de réparation
 * vérifie explicitement que le technicien connecté fait partie des techniciens assignés à
 * cet ordre (pool diagnostic OU pool réparation) avant toute lecture/écriture — contrôle
 * nouveau, sans précédent direct dans le projet, cf. rapport de la tâche.
 */
public interface TechnicienPortalService {
    List<OrdreReparation> getMesOrdresReparation(Technicien technicien);
    OrdreReparation getMonOrdreReparation(Technicien technicien, Long ordreReparationId);

    List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Technicien technicien, Long ordreReparationId, TypePieceJointe type);
    PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Technicien technicien, Long ordreReparationId, PieceJointeDiagnosticRequest request);
    void deletePieceJointeDiagnostic(Technicien technicien, Long ordreReparationId, Long pieceJointeId);

    OrdreReparation updatePannesDetectees(Technicien technicien, Long ordreReparationId, PannesRequest request);

    void proposerPiece(Technicien technicien, Long ordreReparationId, TechnicienLignePieceRequest request);
    void proposerMainDoeuvre(Technicien technicien, Long ordreReparationId, TechnicienLigneMainDoeuvreRequest request);
}
