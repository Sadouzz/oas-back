Auteur : Codex | Date : 2026-11-01 | Action : documenter l'accès aux pièces jointes du diagnostic depuis l'OR

# Documents et photos du diagnostic dans l'OR

Les agents consultent et gèrent les photos et documents PDF du diagnostic à l'étape OR (anciennement « Pièces & MO »), plutôt que dans l'étape Chef Atelier. La zone accepte la sélection de plusieurs fichiers et les envoie successivement vers Cloudinary.

Les fichiers restent rattachés au diagnostic de l'ordre de réparation. Le frontend réutilise les routes `GET`, `POST` et `DELETE /api/ordres-reparation/{id}/diagnostic/pieces-jointes`; l'ajout crée le diagnostic lié à l'OR s'il n'existe pas encore. Cette évolution ne change ni le schéma ni le rattachement des pièces jointes.
