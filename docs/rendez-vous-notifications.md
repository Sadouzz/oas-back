Auteur : Codex | Date : 2026-10-13 | Action : documenter la distribution, les événements et les canaux email/WhatsApp

# Notifications et création de rendez-vous

## Décisions de statut

- Le motif d’annulation est distinct du motif de visite et est persisté dans `rendez_vous.motif_annulation`.
- L’action unique des agents est **Annuler** ; elle exige ce motif, limité à 1 000 caractères.
- Les anciens rendez-vous au statut `REFUSE` restent lisibles en base, mais sont présentés comme `ANNULE`. Une ancienne requête utilisant `REFUSE` est normalisée vers le même processus d’annulation.
- Le client fournit également un motif lorsqu’il annule sa demande.
- Le client reçoit une notification in-app ; le service central gère aussi l’email et le push, sans envoi parallèle depuis le service de rendez-vous.
- À la création, les chefs d’atelier du garage choisi reçoivent une notification in-app ciblée sur ce garage ; les agents d’autres garages ne la reçoivent pas.
- Le texte d’annulation est construit sous la forme : « Votre rendez-vous prévu à la date du JJ/MM/AAAA à HH:mm a été annulé pour motif de … ».

## WhatsApp

Le dépôt ne contient pas de connecteur WhatsApp Business ni de configuration de fournisseur. Après validation ou annulation côté agent, l’interface ouvre un lien WhatsApp vers le numéro du client avec un texte prérempli. L’agent doit vérifier le texte puis appuyer sur **Envoyer** dans WhatsApp. Cette étape n’est pas un envoi serveur automatique ; elle pourra être remplacée par une API WhatsApp Business lorsque le fournisseur et ses identifiants seront configurés.

Le numéro public affiché sur le site est réservé aux demandes entrantes et centralisé dans la configuration frontend. Les actions agent de rendez-vous utilisent le numéro renvoyé avec le rendez-vous, c’est-à-dire celui du client concerné, sans relecture asynchrone du profil. Les numéros locaux sénégalais sont normalisés avec l’indicatif 221.

## Emails et notifications des documents

- Une demande ou une décision de rendez-vous passe par le service de notification client, qui persiste l’alerte in-app et tente aussi push, WebSocket et email après commit.
- Le proforma reste interne tant qu’il n’est pas publié. Sa première publication côté client déclenche une notification in-app et un email avec le PDF ; les publications suivantes ne renvoient pas le message. La conversion en facture envoie la facture au client et notifie l’agent du garage.
- Le récapitulatif de fiche atelier et les conditions de réparation sont envoyés par email après création ; le client reçoit aussi une notification in-app. L’ordre de réparation et ses étapes principales conservent leurs emails et ajoutent une alerte in-app au même client historique que celui enregistré sur l’ordre.
- La création d’une facture informe le client dans son espace et par email avec le PDF. Les agents concernés sont ciblés par garage.
- Le dépôt ne comporte pas de conversion d’un bon de livraison client en facture : la route « bons de livraison » correspond actuellement au flux de réception fournisseur. Aucun message de conversion ne peut donc y être ajouté sans introduire d’abord ce workflow métier.

## Création depuis le modal agent

Le modal de rendez-vous permet de créer puis sélectionner un client, puis de créer et sélectionner un véhicule pour ce client. Ces créations utilisent les endpoints client et véhicule existants ; elles sont donc enregistrées immédiatement, séparément de la création finale du rendez-vous.

## Distribution des notifications

Les notifications client et agent sont enregistrées avant leur publication. Les envois WebSocket, push et email associés à une notification client sont lancés après validation de la transaction ; l’échec d’un canal est journalisé et n’empêche pas les autres canaux. Les destinataires agent sont les comptes activés du rôle demandé. Le canal WebSocket utilisé par les portails agent et client est la file privée `/user/queue/notifications` ; les abonnements anonymes, les autres destinations et les publications depuis le navigateur sont refusés. Les origines admises viennent de `app.cors.allowed-origins` dans le profil actif.

Une notification agent est globale uniquement quand son appel utilise `notifyRole`. Les événements qui portent un garage utilisent `notifyRoleInGarage`, qui exige un garage enregistré et ne cible que les agents du rôle demandé dans ce garage. Les factures, proformas, bons, ordres, fiches atelier et rendez-vous transmettent leur garage métier. Les transferts de véhicule restent globaux, car la demande n'est pas rattachée à un garage.

L'API paginée `GET /api/notifications/page?page=0&size=50` retourne au plus 100 éléments par page, le total non lu et l'indicateur de page suivante. Les pages sont triées par date puis identifiant décroissants pour garder un ordre stable lorsque plusieurs notifications partagent le même horodatage. `GET /api/notifications` reste compatible mais ne retourne que les 100 notifications les plus récentes. Le marquage global comme lu est une mise à jour SQL directe.

Le navigateur utilise WebSocket en temps réel et relit la première page toutes les 60 secondes lorsque la connexion n'est pas active. Les rechargements conservent les notifications reçues pendant la requête et dédoublonnent par identifiant. Le toast et le son sont partagés entre espaces agent et client ; le son peut être désactivé depuis le menu Notifications. Les alertes reçues dans une fenêtre fixe de 300 ms sont regroupées et la file des toasts est plafonnée à cinq éléments visibles ou en attente. Le broker STOMP en mémoire n'est pas un journal durable et un callback après commit n'est pas une file persistante : la notification enregistrée reste consultable par relecture SQL, mais une panne du processus au moment de l'envoi peut faire perdre son push ou son email. Un outbox durable sera nécessaire si la garantie de livraison de ces canaux devient requise.

## Événements couverts

- Les clients sont informés des décisions de transfert de véhicule, des étapes principales de leur ordre de réparation et diagnostic, de chaque paiement enregistré et de la restitution du véhicule. Pour les événements ordre/diagnostic déjà envoyés avec un email détaillé, l'ajout déclenche les canaux in-app/push sans doubler l'email.
- Les agents du garage sont informés des décisions de refus de proforma et de devis prévisionnel. Les agents magasin du garage reçoivent les changements d'envoi/annulation des bons de commande et les alertes de franchissement vers un stock faible ou une rupture. Le chef d'atelier est informé lorsqu'une facture est soldée pour un ordre en cours.
- Les demandes Marketplace sont notifiées aux rôles internes de gestion de façon globale, car le catalogue produit et la requête client ne fournissent pas de garage de traitement. Le client reçoit chaque changement de statut ; l'annulation faite par le client informe aussi les rôles internes.
- Les alertes de stock sont émises au franchissement du seuil ou à la rupture, pas à chaque lecture de la page d'alertes. Elles sont limitées aux mouvements/ajustements gérés par les services de stock, bons de sortie, inventaire et édition de pièce.
