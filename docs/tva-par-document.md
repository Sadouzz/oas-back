Auteur : Codex | Date : 2026-10-29 | Action : préciser le timbre espèces sur le paiement des documents

# TVA par document

La configuration fiscale du garage reste la source du taux de TVA. Chaque document concerné mémorise si la TVA s'applique au moment de sa création. Une modification future du taux fiscal ne recalcule donc pas un document déjà émis.

## Documents concernés

- Bon de sortie : choix à la création; le montant historique `montantTotal` reste HT, avec `montantTVA` et `montantTTC` dédiés.
- Proforma : choix à la création et à la modification; la conversion en facture reprend le choix et les montants du proforma.
- Facture : choix explicite à la création manuelle; la création automatique depuis un proforma reprend son choix.
- Note de prix : choix à la création; le total à payer inclut la TVA si elle est activée. Le timbre espèces est calculé sur chaque part réglée en espèces et reste hors du total de la note.
- Avoir TTC : le contrôle préexistant est persisté explicitement et affiché dans le détail et le PDF. Les nouveaux avoirs ne reçoivent pas de timbre à l'émission.
- Bon de commande : le contrôle préexistant reste la source de l'applicabilité pour le bon de réception fournisseur.
- Bon de réception fournisseur : il reprend l'état TVA du bon de commande source; il n'a pas de choix distinct.

Les détails des documents, le portail client pour factures et proformas, les récapitulatifs de proforma et les PDFs affichent l'état TVA enregistré. Le proforma indique aussi que le taux de timbre espèces ne s'applique qu'à la part payée en espèces et qu'il est hors total. Les interfaces d'encaissement calculent le timbre pour les paiements en espèces et affichent le montant à remettre. L'avoir HT demeure un document hors TVA.

## Documents hors périmètre

Il n'existe pas de document autonome de livraison client dans le code actuel. La route `/api/bons-de-livraison` est un alias historique du bon de réception fournisseur; elle ne constitue pas un BL client. Le devis prévisionnel ne contient qu'un montant estimatif saisi manuellement, sans lignes HT/TVA ni définition du montant comme HT ou TTC. Il reste donc inchangé afin de ne pas réinterpréter les estimations déjà saisies.

## Compatibilité des données

Les colonnes de compatibilité sont ajoutées par `ddl-auto: update` en déploiement. Le script manuel `db/manual/20261026_tva_par_document.sql` peut ensuite être exécuté pour renseigner l'état des documents historiques à partir de leur montant de TVA. Les anciens bons de sortie sont repris comme HT sans TVA.
