Auteur : Codex | Date : 2026-11-09 | Action : documenter le workflow du diagnostic et ses affectations multiples

# Documents et photos du diagnostic dans l'OR

Les agents consultent et gèrent les photos et documents PDF du diagnostic à l'étape OR (anciennement « Pièces & MO »), plutôt que dans l'étape Chef Atelier. La zone accepte la sélection de plusieurs fichiers et les envoie successivement vers Cloudinary.

Les fichiers restent rattachés au diagnostic de l'ordre de réparation. Le frontend réutilise les routes `GET`, `POST` et `DELETE /api/ordres-reparation/{id}/diagnostic/pieces-jointes`; l'ajout crée le diagnostic lié à l'OR s'il n'existe pas encore. Cette évolution ne change ni le schéma ni le rattachement des pièces jointes.

## Affectation des techniciens et clôture

Un diagnostic peut avoir plusieurs techniciens affectés. La relation est persistée dans `diagnostic_techniciens`; l'ancien champ `diagnostics.technicien_id` reste synchronisé avec un technicien référent pour les consommateurs historiques. Exécuter une fois après déploiement la migration manuelle `src/main/resources/db/manual/20261109_diagnostic_plusieurs_techniciens.sql` pour copier les affectations existantes; elle peut être rejouée sans créer de doublons.

Affecter au moins un technicien fait passer le diagnostic à `EN_COURS`. Un nouvel OR commence obligatoirement au statut `RECEPTION`. Depuis l'étape Diagnostic, le bouton « Passer à l'OR » enregistre en une transaction les données de l'étape, le statut `TERMINE` du diagnostic et le statut `PIECES_MO` de l'OR. La clôture n'accepte que l'équipe déjà persistée et n'est possible que lorsque l'OR est au statut `DIAGNOSTIC`; elle ne peut donc pas affecter une équipe ou sauter l'étape depuis la réception. Sans technicien, le backend refuse la clôture. Une fois l'OR passé à `PIECES_MO`, les routes de mise à jour, suppression, pièces jointes et remarques verrouillent le diagnostic; le statut ne peut plus revenir avant `PIECES_MO`. Les routes de changement de statut et les parcours de création/validation du proforma contrôlent aussi cette progression pour empêcher un contournement par appel API direct.
