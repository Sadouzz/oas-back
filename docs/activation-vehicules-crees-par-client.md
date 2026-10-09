Auteur : Codex | Date : 2026-10-09 | Action : Documenter l'activation des véhicules déclarés par un client

# Activation des véhicules déclarés par un client

Un véhicule enregistré depuis l'espace client est persisté avec `actif = false`. Il apparaît dans ses véhicules et reste sélectionnable pour demander un rendez-vous. La création par un agent reste active immédiatement.

Tant qu'il est inactif, la création de la demande de rendez-vous est la seule action autorisée sur le véhicule. Le backend refuse sa validation, son report et son annulation, ainsi que les autres opérations métier : proforma, devis prévisionnel, note de prix, ordre de réparation, fiche atelier, facture, bon de sortie, bon de commande et avoirs HT/TTC. Les modifications et suppressions directes du véhicule sont également refusées. La consultation de l'historique reste en lecture seule.

Les agents voient l'indication « Nouveau véhicule · activation requise » sur le rendez-vous et peuvent activer le véhicule depuis cette ligne. L'activation est réservée aux rôles agent, super-agent, master et chef d'atelier.

Les véhicules déjà présents lors de l'ajout de la colonne sont considérés actifs par défaut afin de préserver leur fonctionnement. L'activation n'est pas le transfert de propriété : le parcours de transfert existant reste séparé.
