package sn.oas.facturation.shared.documentNumber;

public enum DocumentType {
    FA,  // Fiche Atelier
    FC,  // Facture
    PF,  // Proforma
    BS,  // Bon de Sortie
    BC,  // Bon de Commande
    BR,  // Bon de Réception
    BL,  // Bon de Réception (alias)
    RC,  // Reçu
    AH,  // Avoir HT
    AT,  // Avoir TTC
    NP,  // Note de Prix
    DP,  // Devis Prévisionnel
    FO,  // Fournisseur
    PC,  // Pièce Détachée
    MEC, // Mécanicien
    RDV, // Rendez-Vous
    MSG, // Message
    DMD, // Demande Produit
    AG,  // Agent (matricule)
    OR   // Ordre de Réparation
}
