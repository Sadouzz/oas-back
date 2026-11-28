Auteur : Codex | Date : 2026-11-25 | Action : documenter l'envoi et la décision client par e-mail

# Décision d'un proforma par e-mail

## Parcours

- Le bouton **Email** de l'étape Proforma appelle `POST /api/proformas/{id}/email-decision`. Le backend envoie le PDF et deux liens : accepter ou refuser.
- L'API ne confirme l'envoi qu'après acceptation du message par le serveur SMTP; la génération du PDF et les erreurs SMTP remontent à l'interface.
- Le bouton **Valider les prix et envoyer au client** conserve son parcours existant via `POST /api/proformas/{id}/valider-envoi`.
- Chaque lien ouvre `/proforma-decision?token=…`. L'ouverture affiche le proforma et le choix visé, sans modifier les données. Le client confirme ensuite explicitement son choix; seul le `POST /api/proformas/email-decision/{token}` change le statut.
- À l'acceptation, le proforma passe à `ACCEPTE`, les contrôles métier existants s'appliquent et le client reçoit un e-mail de confirmation. Au refus, le statut passe à `REJETE`, l'équipe est notifiée et le client reçoit également une confirmation.

## Sécurité et expiration

- Les liens sont des JWT signés avec un type dédié, liés au client, à son adresse e-mail, au proforma et à l'action. Ils expirent après sept jours.
- La lecture du lien n'effectue aucune action : les scanners de liens des messageries ne peuvent donc pas accepter ou refuser à la place du client.
- Le backend n'envoie le lien qu'à l'adresse e-mail déjà enregistrée sur le client. Une modification du proforma invalide les liens précédents. Un nouvel envoi invalide les liens du précédent e-mail.
- Le proforma doit être visible et en attente au moment de l'affichage et de la confirmation du choix.

## Configuration

`APP_FRONTEND_URL` définit l'origine utilisée pour construire les liens d'e-mail. La valeur locale par défaut est `http://localhost:4200`; la valeur de production par défaut est `https://oas-senegal.com`.
