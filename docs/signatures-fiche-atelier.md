Auteur : Codex | Date : 2026-11-01 | Action : documenter les signatures et l'envoi de la fiche atelier

# Signatures et conditions de la fiche atelier

## Signature réutilisable de l'agent

Un agent peut enregistrer sa signature dans son profil ou au moment de créer une fiche atelier. L'API ne reçoit aucun identifiant d'agent : elle travaille exclusivement sur l'utilisateur authentifié. La signature n'est jamais ajoutée aux DTO de profil ou de liste d'utilisateurs.

Le backend chiffre le PNG avec AES-256-GCM et un nonce aléatoire avant de l'enregistrer dans `agents.signature_fiche_atelier_chiffree`. Seule la route personnelle `/api/users/me/signature-fiche-atelier` peut rendre la signature en clair à son propriétaire authentifié. La suppression du profil efface la valeur chiffrée.

Configurer `AGENT_SIGNATURE_ENCRYPTION_KEY` dans chaque environnement backend avec une clé Base64 représentant exactement 32 octets. Générer une clé avec :

```bash
openssl rand -base64 32
```

La même clé doit rester disponible pour toutes les instances qui partagent la base. La perdre rend les signatures enregistrées irrécupérables ; la remplacer nécessite une procédure de déchiffrement et rechiffrement préalable.

## Acceptation et signature du client

Le client consulte les conditions dans la fenêtre de lecture, coche explicitement la case d'acceptation, puis signe dans cette même fenêtre. À la validation de cette signature, elle est reportée dans le champ client existant de la fiche. La création transmet l'acceptation et les deux signatures. Le backend refuse une création sans acceptation et enregistre l'heure de cette acceptation côté serveur dans `fiches_atelier.conditions_acceptees_at`.

## Documents transmis

Après validation de la transaction de création, le client reçoit un courriel présentant le numéro de fiche et le véhicule, avec deux pièces jointes :

- la fiche atelier avec les données de réception et les signatures de réceptionnaire et du client ;
- les conditions générales, avec la déclaration d'acceptation, son horodatage et la signature du client.

Le téléchargement historique `/api/fiches-atelier/{id}/pdf` conserve le PDF combiné existant.

## Migration de base de données

Appliquer le script `oas-back/src/main/resources/db/manual/20261101_signature_agent_et_acceptation_conditions_fiche.sql` avant le déploiement du backend. Il ajoute une colonne de stockage chiffré nullable et les deux colonnes de preuve d'acceptation. Les fiches historiques ne sont pas modifiées ; leur état d'acceptation reste inconnu (`NULL`).
