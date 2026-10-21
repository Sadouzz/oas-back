Auteur : Codex | Date : 2026-10-20 | Action : documenter les variantes de présentation des PDF agent

# Notifications push, transfert véhicule et modèles PDF

## Notifications push PWA

Le frontend Angular enregistre le service worker uniquement en production. Les notifications push sont activées à la demande depuis le bouton global visible pour une session authentifiée. Les navigateurs doivent accéder au site en HTTPS (localhost est accepté en développement).

Le backend lie chaque abonnement navigateur à l'utilisateur authentifié, qu'il s'agisse d'un client ou d'un agent. Les notifications existantes destinées à un client ou aux rôles agent, super agent et master déclenchent aussi une livraison push ciblée à leurs destinataires. Il n'y a pas de diffusion globale de chaque événement à tous les comptes.

Variables backend à configurer dans l'environnement de déploiement :

* `OAS_PUSH_VAPID_PUBLIC_KEY`
* `OAS_PUSH_VAPID_PRIVATE_KEY` — secret, ne pas versionner
* `OAS_PUSH_VAPID_SUBJECT` — identifiant `mailto:` ou URL de contact

Routes d'abonnement : `GET /api/push/public-key`, `PUT /api/push/subscription` et `DELETE /api/push/subscription?endpoint=...`. L'API n'accepte que les endpoints HTTPS des services push connus.

## Transfert de véhicule

Le véhicule conserve son identifiant et ses données. Un client qui soumet une immatriculation déjà enregistrée peut envoyer une demande ; le châssis fourni, lorsqu'il existe, doit correspondre au véhicule. Une seule demande `PENDING` est autorisée à la fois par véhicule, quel que soit le client demandeur. Une demande en attente est visible pour les rôles `AGENT`, `SUPER_AGENT` et `MASTER`. Seuls ces rôles peuvent l'approuver ou la refuser.

L'approbation verrouille le véhicule et enregistre les périodes de propriété. Les demandes retiennent le propriétaire en place au moment de leur création ; une demande devenue obsolète est refusée. Le propriétaire actuel n'a pas d'action de validation dans le flux actuel : son identité est affichée aux agents comme contexte et sert à refuser une demande si la propriété a changé depuis la soumission. Le changement direct du client associé via la mise à jour générique d'un véhicule est interdit.

Chaque ordre de réparation contient le client auquel il appartenait. Les ordres déjà présents sans ce client restent visibles au propriétaire actuel avant le premier transfert ; lors du transfert, les ordres historiques encore sans client sont attribués à l'ancien propriétaire dans la même transaction que le changement de propriétaire. Les factures gardent leur propre client ou héritent du client figé de l'ordre de réparation. L'historique d'interventions est filtré par ce client historique. Limitation connue : la liste des proformas inclut encore un fallback vers le propriétaire actuel du véhicule et peut donc afficher au nouveau propriétaire des éléments de l'ancien historique ; le détail reste protégé, mais la liste doit être corrigée.

Routes : `POST /api/vehicle-transfers`, `GET /api/vehicle-transfers/me`, `GET /api/vehicle-transfers/pending` et `POST /api/vehicle-transfers/{id}/decision`.

## Modèles PDF globaux

Les modèles sont globaux au système et n'ont pas de relation garage. Les types d'affectation sont conservés dans une table de liaison ; les modèles antérieurs à cette liaison retombent sur leur type historique unique. Les rôles `SUPER_AGENT` et `MASTER` peuvent créer des modèles et des versions. Plusieurs modèles actifs peuvent être proposés pour un même type. Les documents commerciaux téléchargeables par l’agent proposent les variantes `AVEC_ENTETE` et `SANS_ENTETE`.

Les types déjà branchés aux générateurs sont : facture, proforma, devis prévisionnel, bon de commande, bon de réception, avoir HT, avoir TTC et diagnostic. Un modèle peut être affecté à plusieurs types, voire à tous. Les blocs et les affectations peuvent être modifiés sur un modèle existant ; chaque enregistrement crée une nouvelle version, sans modifier les versions déjà utilisées. Pour une facture, le modèle sélectionné à l'émission reste le choix par défaut pour les réimpressions. L'agent peut choisir `AVEC_ENTETE` ou `SANS_ENTETE` au téléchargement d'une facture, d'un proforma, d'un devis prévisionnel, d'un bon de commande, d'un bon de réception ou d'un avoir ; le serveur utilise le modèle actif de cette présentation, ou la génération standard correspondante si aucun modèle n'est actif. Le client télécharge la facture dans la présentation enregistrée à son émission et ne choisit pas la variante dans son portail. La présentation retenue à l'émission est aussi enregistrée pour que le PDF joint automatiquement au courriel la respecte. La variante `AVEC_ENTETE` reprend l'identité visuelle OAS partagée avec le bon de réception ; la variante `SANS_ENTETE` conserve une page simple. Les lignes et montants historiques de la facture ne sont pas recalculés lors du changement de présentation. Les tableaux dynamiques de lignes de pièces/main-d’œuvre sont réservés aux modèles affectés uniquement aux factures, car les autres générateurs ne fournissent pas ces lignes. Les données sont interpolées par des variables comme `{{numero}}` et `{{clientNom}}`.

Routes : `GET /api/pdf-templates/active-invoice?layoutKey=AVEC_ENTETE|SANS_ENTETE` pour les choix d'émission ; `GET /api/factures/{id}/pdf?layoutKey=AVEC_ENTETE|SANS_ENTETE` pour le téléchargement agent ; `GET /api/factures/me/{id}/pdf` pour le téléchargement client dans la présentation associée à la facture ; Les endpoints de téléchargement PDF agent des documents commerciaux concernés acceptent `?layoutKey=AVEC_ENTETE|SANS_ENTETE`. Sans `layoutKey`, la réimpression agent conserve la présentation associée à la facture. Administration : `GET /api/pdf-templates`, `POST /api/pdf-templates`, `PUT /api/pdf-templates/{id}`, `POST /api/pdf-templates/{id}/activate`, `POST /api/pdf-templates/{id}/deactivate` et `POST /api/pdf-templates/preview` (PDF). Les blocs ordonnés peuvent être déplacés et retirés ; images PNG/JPEG sont téléversées via le flux Cloudinary existant et seules leurs URL sécurisées sont enregistrées. Les images peuvent suivre le flux du document ou être placées librement par coordonnées X/Y en pourcentage de la page. Les blocs permettent alignement, largeur, taille/couleur de texte, fond et remplissage. Les tableaux utilisent des rangées séparées par des retours à la ligne et des colonnes séparées par tabulation, avec réglage des largeurs, du style, de l'alignement vertical, du remplissage et de la première ligne d'en-tête. Les tableaux de factures peuvent lier les lignes de pièces ou de main-d'œuvre ; chaque ligne de données expose ses champs au modèle de ligne. La taille du modèle reste limitée à 8 Mo au total.
