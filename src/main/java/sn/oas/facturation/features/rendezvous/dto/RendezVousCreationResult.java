package sn.oas.facturation.features.rendezvous.dto;

public record RendezVousCreationResult(
        Type type,
        RendezVousResponse rendezVous,
        DemandeDerogationPlafondResponse demandeDerogation,
        String message
) {
    public enum Type { RENDEZ_VOUS_CREE, DEROGATION_EN_ATTENTE }

    public static RendezVousCreationResult cree(RendezVousResponse rendezVous) {
        return new RendezVousCreationResult(Type.RENDEZ_VOUS_CREE, rendezVous, null,
                "Le rendez-vous a été créé.");
    }

    public static RendezVousCreationResult enAttente(DemandeDerogationPlafondResponse demande) {
        return new RendezVousCreationResult(Type.DEROGATION_EN_ATTENTE, null, demande,
                "Le plafond du client est atteint. La demande a été transmise à un Super Agent ; aucun rendez-vous ne sera créé avant son approbation.");
    }

    public static RendezVousCreationResult dejaEnAttente(DemandeDerogationPlafondResponse demande) {
        return new RendezVousCreationResult(Type.DEROGATION_EN_ATTENTE, null, demande,
                "Une demande de dérogation est déjà en attente. Cette nouvelle tentative n'a créé ni demande supplémentaire ni rendez-vous.");
    }
}
