# Sprint 6 — Portail Client (Backend Support)

Ce plan d'implémentation détaille la conception et la structure du backend pour le Portail Client (Sprint 6). Les fonctionnalités du portail sont destinées à être consommées par l'application mobile Flutter de manière sécurisée (s'appuyant sur le jeton JWT).

---

## User Review Required

> [!IMPORTANT]
> **Génération de Matricule Client automatique** : Le champ `matricule` étant requis par la base de données, nous le générons automatiquement sous la forme incrémentale `CLT-xxxxx` (ex: `CLT-00001`, `CLT-00002`, ...) si le client s'inscrit sans le fournir.
>
> **Suivi d'avancement des réparations** : Introduction d'un statut de progression (`A_FAIRE`, `EN_COURS`, `TERMINE`, `LIVRE`) directement sur les fiches d'atelier (`FicheAtelier`) pour permettre le suivi en temps réel de la réparation par le client.
>
> **Gestion des paiements partiels** : Ajout de l'état `PARTIELLEMENT_PAYEE` et d'un champ `montantPaye` sur les factures, permettant aux agents de saisir des règlements partiels et de générer les reçus associés.

---

## Proposed Changes

### 1. Authentification & Profil

#### [MODIFY] [AuthServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/auth/service/AuthServiceImpl.java)
- Génération automatique du `matricule` client sous le format incrémentiel `CLT-xxxxx` (ex: `CLT-00001`) si absent lors de l'inscription.

#### [MODIFY] [ClientRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/client/repository/ClientRepository.java)
- Ajout de la requête `findMaxClientMatricule()` pour récupérer le matricule client maximum existant.

#### [MODIFY] [ClientService.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/client/service/ClientService.java)
- Déclaration de la méthode utilitaire `getClientConnecte()`.

#### [MODIFY] [ClientServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/client/service/ClientServiceImpl.java)
- Implémentation de `getClientConnecte()` à l'aide de l'authentification Spring Security pour récupérer le client connecté.

---

### 2. Module Prise de Rendez-vous

#### [NEW] [RendezVousStatus.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/data/enums/RendezVousStatus.java)
- Enumération contenant les différents statuts du rendez-vous : `EN_ATTENTE`, `CONFIRME`, `REFUSE`, `ANNULE`, `TERMINE`.

#### [NEW] [RendezVous.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/data/entity/RendezVous.java)
- Entité JPA mappant un rendez-vous avec un `Client` (obligatoire), un `Vehicule` (optionnel), une date, un motif, un statut et un commentaire d'atelier.

#### [NEW] [RendezVousRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/repository/RendezVousRepository.java)
- Interface repository de gestion des requêtes SQL d'accès aux rendez-vous.

#### [NEW] [RendezVousRequest.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/dto/RendezVousRequest.java)
- DTO Record pour l'envoi d'une demande de rendez-vous.

#### [NEW] [RendezVousResponse.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/dto/RendezVousResponse.java)
- DTO Record de retour des rendez-vous avec méthode statique de mapping `of(RendezVous)`.

#### [NEW] [RendezVousService.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/service/RendezVousService.java)
- Interface métier de gestion des rendez-vous.

#### [NEW] [RendezVousServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/service/RendezVousServiceImpl.java)
- Implémentation des fonctionnalités de réservation, d'annulation et de validation des rendez-vous, avec envoi automatique de notifications.

---

### 3. Module Notifications In-App

#### [NEW] [Notification.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/data/entity/Notification.java)
- Entité JPA stockant les notifications in-app pour chaque client (titre, message, état de lecture, date de création).

#### [NEW] [NotificationRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/repository/NotificationRepository.java)
- Repository JPA pour rechercher les notifications d'un client par date décroissante.

#### [NEW] [NotificationResponse.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/dto/NotificationResponse.java)
- DTO Record de retour des notifications.

#### [NEW] [NotificationService.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/service/NotificationService.java)
- Interface de définition des services de notifications.

#### [NEW] [NotificationServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/service/NotificationServiceImpl.java)
- Logique métier pour envoyer, lister et marquer les notifications comme lues.

---

### 4. Module Suivi des Réparations

#### [NEW] [StatutFiche.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/data/enums/StatutFiche.java)
- Enumération pour la progression des réparations : `A_FAIRE`, `EN_COURS`, `TERMINE`, `LIVRE`.

#### [MODIFY] [FicheAtelier.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/data/entity/FicheAtelier.java)
- Ajout du champ `statut` de type `StatutFiche` (par défaut `A_FAIRE`) et gestion dans le cycle de vie JPA (`PrePersist`).

#### [MODIFY] [FicheAtelierRequest.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/dto/FicheAtelierRequest.java)
- Ajout du champ `statut` pour permettre sa modification lors de la création ou mise à jour.

#### [MODIFY] [FicheAtelierServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/service/FicheAtelierServiceImpl.java)
- Mise à jour de la logique de création et de modification pour intégrer le statut de réparation.

#### [MODIFY] [FicheAtelierRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/repository/FicheAtelierRepository.java)
- Ajout de la méthode `findByVehiculeClientIdOrderByDateCreationDesc` pour lister l'historique des réparations d'un client.

---

### 5. Module Factures & Reçus (Paiements)

#### [MODIFY] [StatutFacturation.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/facturation/data/enums/StatutFacturation.java)
- Ajout de l'état de facturation `PARTIELLEMENT_PAYEE`.

#### [MODIFY] [Facture.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/facture/data/entity/Facture.java)
- Ajout du champ `montantPaye` (initialisé à zéro) pour suivre l'historique financier des factures.

#### [MODIFY] [FactureRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/facture/repository/FactureRepository.java)
- Ajout de la méthode `findByClientIdOrderByDateCreationDesc` pour lister l'historique de facturation du client.

#### [NEW] [Recu.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/data/entity/Recu.java)
- Entité JPA mappant un reçu de paiement (numéro unique, lien facture, montant payé, date de paiement et méthode).

#### [NEW] [RecuRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/repository/RecuRepository.java)
- Repository JPA pour récupérer les reçus de paiement d'un client.

#### [NEW] [RecuResponse.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/dto/RecuResponse.java)
- DTO Record retournant le détail des reçus.

#### [NEW] [RecuService.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/service/RecuService.java)
- Interface métier de gestion des paiements et reçus.

#### [NEW] [RecuServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/service/RecuServiceImpl.java)
- Logique de versement (total ou partiel) : met à jour le statut de la facture, incrémente son `montantPaye`, crée l'entité `Recu` et notifie le client.

---

### 6. Module Messagerie Client

#### [NEW] [Message.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/data/entity/Message.java)
- Entité JPA stockant chaque message (client, expéditeur, destinataire, contenu, date et état lu/non lu).

#### [NEW] [MessageRepository.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/repository/MessageRepository.java)
- Repository JPA pour récupérer l'historique de chat et compter les messages non lus.

#### [NEW] [MessageRequest.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/dto/MessageRequest.java)
- DTO de transmission des nouveaux messages.

#### [NEW] [MessageResponse.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/dto/MessageResponse.java)
- DTO de retour des messages avec formateur.

#### [NEW] [ClientConversationResponse.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/dto/ClientConversationResponse.java)
- DTO retournant l'état d'une conversation (dernier message, compteur non lus pour les agents).

#### [NEW] [MessageService.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/service/MessageService.java)
- Interface métier pour la gestion du chat.

#### [NEW] [MessageServiceImpl.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/service/MessageServiceImpl.java)
- Logique d'envoi et de récupération des messages, marquant automatiquement les messages reçus comme lus lors de la consultation.

---

### 7. Contrôleurs Web REST API

#### [NEW] [ClientPortalController.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/client/controller/ClientPortalController.java)
- Contrôleur regroupant l'ensemble des requêtes du client connecté sous `/api/client` :
  - **Profil** : `GET /api/client/me`
  - **Véhicules** : `GET /api/client/vehicules`, `POST /api/client/vehicules`
  - **Rendez-vous** : `GET /api/client/rendezvous`, `POST /api/client/rendezvous`, `PUT /api/client/rendezvous/{id}/annuler`
  - **Suivi Réparations** : `GET /api/client/interventions`, `GET /api/client/interventions/{id}`
  - **Facturation** : `GET /api/client/factures`, `GET /api/client/factures/{id}`
  - **Reçus de paiement** : `GET /api/client/recus`
  - **Notifications** : `GET /api/client/notifications`, `PUT /api/client/notifications/{id}/lu`, `PUT /api/client/notifications/lu-tout`
  - **Messagerie Chat** : `GET /api/client/messages`, `POST /api/client/messages`

#### [NEW] [AdminPortalController.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/client/controller/AdminPortalController.java)
- Contrôleur regroupant la gestion du portail client par les agents sous `/api/admin/portal` :
  - **Rendez-vous** : `GET /api/admin/portal/rendezvous`, `PUT /api/admin/portal/rendezvous/{id}/statut` (validation)
  - **Paiements** : `POST /api/admin/portal/factures/{id}/payer` (enregistrement d'un versement)
  - **Messagerie** : `GET /api/admin/portal/messages/clients` (liste des chats), `GET /api/admin/portal/messages/clients/{clientId}` (discussion), `POST /api/admin/portal/messages/clients/{clientId}` (répondre)

---

## Verification Plan

### Automated Tests
- Lancement du build Maven pour valider l'intégrité de la compilation et le bon démarrage du contexte JPA (avec la création des nouvelles tables) :
  `mvn clean compile`

### Manual Verification
- Test des nouveaux endpoints via un client HTTP ou Swagger UI (`http://localhost:9090/swagger-ui.html`) :
  - Inscription d'un nouveau client sans renseigner de matricule.
  - Connexion du client et récupération du jeton JWT.
  - Création de véhicules et demande de rendez-vous avec ce jeton.
  - Simulation du paiement d'une facture par un Agent (générant un Reçu et une Notification pour le client).
  - Envoi et réception de messages entre le client et l'agent.
