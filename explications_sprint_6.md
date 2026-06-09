# Récapitulatif et Explications des Développements — Sprint 6

Ce document explique en détail tout ce qui a été réalisé et configuré pour le **Portail Client (Sprint 6)**. 

---

## 1. Inscription et Profil Client
* **Fonctionnement** : Le client s'inscrit via l'API `/api/auth/signup`. Si le `matricule` n'est pas fourni, le backend le génère automatiquement sous la forme séquentielle incrémentielle `CLT-xxxxx` (ex: `CLT-00001`, `CLT-00002`, etc.) en cherchant le matricule maximum en base de données.
* **Session** : Ajout d'une méthode `getClientConnecte()` dans le service client pour obtenir l'utilisateur connecté via son jeton de sécurité JWT.

---

## 2. Prise de Rendez-vous
* **Création de l'entité** : Création de la table [RendezVous.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/data/entity/RendezVous.java) pour enregistrer les demandes de rendez-vous (date, motif, lien avec le client connecté, lien avec son véhicule, statut, et commentaires de l'atelier).
* **Statuts** : Définition des états possibles dans [RendezVousStatus.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/rendezvous/data/enums/RendezVousStatus.java) (`EN_ATTENTE`, `CONFIRME`, `REFUSE`, `ANNULE`, `TERMINE`).
* **Services & APIs** :
  * Le client connecté peut planifier un rendez-vous et lister son historique.
  * Les agents de l'atelier disposent d'APIs pour lister toutes les demandes de rendez-vous, les confirmer ou les refuser avec un commentaire. Chaque changement de statut notifie le client connecté.

---

## 3. Suivi de l'Avancement des Réparations
* **Problématique** : La fiche d'atelier (`FicheAtelier`) ne possédait pas d'état permettant de suivre l'avancement d'un véhicule en réparation.
* **Solution** :
  * Création d'un enum [StatutReparation.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/data/enums/StatutReparation.java) avec les statuts `A_FAIRE`, `EN_COURS`, `TERMINE`, `LIVRE`.
  * Ajout du champ `statut` dans l'entité [FicheAtelier.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/ficheAtelier/data/entity/FicheAtelier.java).
  * Les clients connectés peuvent ainsi suivre la progression de leurs véhicules en direct via `GET /api/client/interventions`.

---

## 4. Historique de Facturation & Règlements Partiels
* **Problématique** : Pouvoir consulter l'historique des factures et suivre précisément les versements (ce qui a été payé, non payé, ou partiellement payé).
* **Solution** :
  * Ajout du statut `PARTIELLEMENT_PAYEE` au modèle [StatutFacturation.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/facturation/data/enums/StatutFacturation.java).
  * Ajout du champ `montantPaye` sur l'entité [Facture.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/facture/data/entity/Facture.java) pour suivre le total des versements déjà effectués.
  * Lors d'un paiement enregistré par l'atelier, le montant payé s'additionne. Si la somme couvre totalement la facture, elle passe à `PAYEE`, sinon elle passe à `PARTIELLEMENT_PAYEE`. Le client a accès à ces informations dans son historique.

---

## 5. Reçus de Paiement
* **Création de l'entité** : Création de la table [Recu.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/recu/data/entity/Recu.java) pour stocker les détails de chaque paiement (numéro de reçu unique, référence de la facture, montant versé, date et méthode de paiement).
* **Flux** : L'enregistrement d'un versement crée instantanément un reçu et met à jour la facture correspondante. Le client peut lister tous ses reçus via l'API.

---

## 6. Notifications In-App
* **Création de l'entité** : Création de la table [Notification.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/notification/data/entity/Notification.java) pour historiser les messages in-app envoyés au client.
* **Fonctionnement** : Des notifications sont créées automatiquement à chaque événement clé (confirmation de rendez-vous, paiement de facture, avancement de réparation). Le client peut les marquer comme lues individuellement ou toutes en même temps.

---

## 7. Messagerie Client (Chat REST)
* **Création de l'entité** : Création de la table [Message.java](file:///d:/JBA/BACK/oas-back/oas-back/src/main/java/sn/oas/facturation/messagerie/data/entity/Message.java) pour stocker l'historique des discussions entre le client connecté et le support de l'atelier.
* **Fonctionnalités** :
  * Le client peut envoyer des messages et lire la conversation.
  * L'agent dispose d'un tableau de bord de messagerie listant les clients et affichant le nombre de messages non lus pour chaque conversation active.
  * Les messages non lus sont automatiquement marqués comme lus dès que le destinataire ouvre la discussion.

---

## 8. Exportation de Swagger pour Postman
* **Spécification OpenAPI** : Création d'un test d'intégration automatisé [OpenApiSpecGeneratorTest.java](file:///d:/JBA/BACK/oas-back/oas-back/src/test/java/sn/oas/facturation/OpenApiSpecGeneratorTest.java) qui s'exécute avec une base H2 en mémoire.
* **Fichier exporté** : Le test génère le fichier **[swagger.json](file:///d:/JBA/BACK/oas-back/oas-back/swagger.json)** à la racine du projet, qui contient la spécification OpenAPI brute de toutes les routes de l'application. Vous pouvez importer directement ce fichier dans Postman pour configurer vos tests en un clic.
