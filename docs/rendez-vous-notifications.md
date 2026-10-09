Auteur : Codex | Date : 2026-10-09 | Action : documenter les motifs d’annulation et les notifications RDV

# Notifications et création de rendez-vous

## Décisions de statut

- Le motif d’annulation est distinct du motif de visite et est persisté dans `rendez_vous.motif_annulation`.
- L’action unique des agents est **Annuler** ; elle exige ce motif, limité à 1 000 caractères.
- Les anciens rendez-vous au statut `REFUSE` restent lisibles en base, mais sont présentés comme `ANNULE`. Une ancienne requête utilisant `REFUSE` est normalisée vers le même processus d’annulation.
- Le client fournit également un motif lorsqu’il annule sa demande.
- Le client reçoit une notification in-app et un email sur les décisions de statut concernées.
- Le texte d’annulation est construit sous la forme : « Votre rendez-vous prévu à la date du JJ/MM/AAAA à HH:mm a été annulé pour motif de … ».

## WhatsApp

Le dépôt ne contient pas de connecteur WhatsApp Business ni de configuration de fournisseur. Après validation ou annulation côté agent, l’interface ouvre un lien WhatsApp vers le numéro du client avec un texte prérempli. L’agent doit vérifier le texte puis appuyer sur **Envoyer** dans WhatsApp. Cette étape n’est pas un envoi serveur automatique ; elle pourra être remplacée par une API WhatsApp Business lorsque le fournisseur et ses identifiants seront configurés.

## Création depuis le modal agent

Le modal de rendez-vous permet de créer puis sélectionner un client, puis de créer et sélectionner un véhicule pour ce client. Ces créations utilisent les endpoints client et véhicule existants ; elles sont donc enregistrées immédiatement, séparément de la création finale du rendez-vous.
