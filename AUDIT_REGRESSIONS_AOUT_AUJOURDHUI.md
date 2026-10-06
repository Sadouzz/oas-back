# Audit des régressions OAS — depuis août 2026

**État :** en cours, revue incrémentale par domaine
**Dernière mise à jour :** 2026-10-04
**Périmètre :** `oas-front` et `oas-back`, historique Git du 2026-08-01 au 2026-10-03, comparaison comportementale avant/après.
**Règle de preuve :** une différence de code n'est appelée « régression » que si l'ancienne logique/comportement est identifiable et que l'état courant la casse, la supprime ou la dégrade. Les déplacements de fichiers/packages sont consignés comme réorganisations tant qu'une perte de comportement n'est pas démontrée.

## Synthèse de progression

- Historique repéré : **120 commits front** et **98 commits backend** dans la période (commits non-merge accessibles depuis les refs locales au 3 octobre).
- Le journal contient **54 constats numérotés** (dont deux explicitement hérités/hors période); les identifiants vont jusqu'à OAS-055 avec OAS-014 absent.
- Comparaison baseline front : commit de fin juillet `92748003624b68615be1434a5ce71997abbe0e31` ; backend : `53fef15b0c4f23d4b002e72d9c4887781ddf4854`.
- Volumétrie baseline → HEAD : **511 fichiers front** et **584 fichiers backend** touchés, avec de nombreux déplacements/refactors de packages qui expliquent une part importante des suppressions apparentes.
- Branches locales/distantes disponibles examinées comme éléments d'historique, mais l'état courant reste la référence fonctionnelle.
- Ne pas confondre les modifications utilisateur déjà présentes dans le working tree avec l'historique validé : elles ont été préservées et ne sont pas incluses comme régressions.

## Anomalies confirmées / fortes

### OAS-001 — Flux « sortie atelier » et son historique incohérents (backend)

**Sévérité :** élevée — stock physique et traçabilité
**État :** régression confirmée pour `qteReelle`; le nouveau cycle en deux étapes mérite une validation métier avant de conclure qu'il constitue lui-même une régression.
**Zone :** bon de sortie, stock et historique.

**Comportement actuel observé dans le code :**

1. À la création du bon, le backend retire `quantite` du magasin et l'ajoute au stock atelier (`BonDeSortieServiceImpl`, lignes ~106–145). Le bon est toutefois créé en `EN_ATTENTE` (`StatutBon`), avec une notification indiquant qu'il attend validation.
2. À validation du même bon, le backend retire de nouveau `quantite` du stock atelier, avec `Math.max(0, atelierAvant - quantite)` (~214–245), puis écrit un second mouvement `SORTIE_ATELIER`.
3. Le mouvement de sortie atelier n'échoue pas si le stock atelier est inférieur à la quantité : `Math.max(0, ...)` masque le déficit et le bon est validé. Le magasin est débité à la création ; si le stock atelier a changé avant validation, le bon peut tout de même être validé avec une sortie partielle sans signaler le manque.
4. L'historique de sortie magasin est inscrit dès la création avec le statut `SORTIE` (~150–175), même si le bon est encore en attente. L'historique de validation ajoute ensuite `SORTIE ATELIER`.
5. **Régression confirmée sur l'invariant du stock réel :** le transfert à la création diminue le magasin et augmente l'atelier de la même quantité, donc le total `stockMagasin + stockAtelier` ne change pas à cette étape. En revanche, à la validation, le code diminue le stock atelier (pièce considérée comme sortie vers le véhicule), sans recalculer `qteReelle`; le total physique diminue alors mais le total enregistré peut rester inchangé. Le callback `PDP.calculateQteReelle()` ne recalcule que si `qteReelle == null`; pour une pièce persistée, la quantité ne se corrige donc pas automatiquement. Le mouvement et son historique enregistrent ensuite `getQteReelle()` tel quel. C'est un écart démontré dans le code ; la capture seule ne permet pas de confirmer que ses lignes précises en sont un exemple.
6. La réponse de l'API historique global des bons expose les stocks `stockMagasin` et `stockAtelier` des snapshots persistés, donc les valeurs affichées proviennent de données d'historique et non d'une recomposition frontend. L'écran de l'historique des bons affiche directement `qteReelle` si elle est présente ; il ne recalcule pas la valeur persistée.

**Référence à la capture utilisateur (02/10) :** les mouvements `DK-BS-2026-0005`/`0004` montrent une ligne `SORTIE ATELIER` avec `0/0/0`, puis `SORTIE` avec `49/1/50`. Ces exemples sont arithmétiquement cohérents pris isolément (0+0=0 et 49+1=50). Les deux lignes peuvent représenter deux étapes distinctes (transfert magasin→atelier puis consommation atelier→véhicule) ; la capture seule ne prouve ni double prélèvement, ni divergence du total réel. Elle a servi de piste pour le traçage du code, qui confirme séparément l'absence de recalcul de `qteReelle` sur le flux courant.

**Preuve historique :** la baseline pré-août (`53fef15b0c4f23d4b002e72d9c4887781ddf4854`) mettait explicitement à jour `qteReelle` après le mouvement de stock validé. Le code courant répartit le cycle entre transfert à `creer` (total neutre) et sortie de l'atelier à `valider` (total réduit), mais ne met pas à jour `qteReelle` après cette sortie. Le transfert anticipé peut être un changement métier intentionnel ; l'absence de recalcul après la consommation courante brise l'invariant somme des stocks / stock réel.

**Fichiers de référence :**
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/service/BonDeSortieServiceImpl.java` (~106–175, ~206–275)
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/data/entity/BonDeSortie.java` (statut initial `EN_ATTENTE`)
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/data/enums/StatutBon.java`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/controller/BonDeSortieController.java` (création/validation/history global)
- ancienne version avant refactor : `src/main/java/sn/oas/facturation/bonDeSortie/service/BonDeSortieServiceImpl.java`

**Point à vérifier ensuite :** confirmer si « SORTIE » et « SORTIE ATELIER » sont deux étapes métier voulues et préciser le stock réel attendu après chaque étape. Revoir également retours/annulations. Aucune correction n'a été appliquée pendant l'audit.

### OAS-006 — Les filtres de l'historique des bons de sortie ne couvrent plus toutes les pages

**Sévérité :** moyenne — recherche et export incomplets
**État :** régression confirmée depuis la refonte de pagination du 2026-09-07 (`5d888f9`).
**Zone :** écran frontend Historique des Bons de Sortie.

Avant cette refonte, l'écran chargeait l'historique global sans pagination serveur, appliquait recherche, action, date et pièce à toute la collection, puis paginait localement les résultats filtrés. Depuis le commit `5d888f9`, il demande une page de 15 lignes à l'API (`getHistoriqueGlobal(getPageParams())`), puis continue d'appliquer les filtres action/date/pièce uniquement sur ces 15 lignes reçues. Le backend ne prend pour cet endpoint que `keyword`, `page` et `size`; il n'accepte pas les filtres d'action/date/pièce.

Conséquences observables dans le code :
- un filtre action/date/pièce masque des lignes de la page courante seulement ; les lignes correspondantes sur les autres pages ne sont jamais ramenées ;
- la pagination conserve `totalElements` et `totalPages` de l'ensemble non filtré, donc l'indication de résultats/pagination ne représente pas le résultat filtré ;
- `Export CSV` exporte `filtered`, qui ne contient que les résultats présents dans la page courante, alors que précédemment il exportait tous les éléments correspondants chargés.

La recherche texte est partiellement différente : `BasePaginatedComponent.onSearch()` relance bien une requête avec `keyword`, et le backend applique ce keyword; les filtres action/date/pièce, eux, restent côté client et sont limités à la page courante. Le point de rupture exact est la conversion de pagination locale à pagination serveur sans déplacer/répliquer les filtres ni adapter l'export.

**Preuve historique :** comparer `historique-bs.component.ts` avant `5d888f9` (appel sans paramètres, filtrage de la collection et export local) à la version introduite par `5d888f9` (chargement d'une page serveur, filtres toujours locaux). Vérification du endpoint courant dans `BonDeSortieController.getAllHistorique`: seuls `keyword`, `page`, `size` sont déclarés.

**Fichiers :**
- `oas-front/src/app/agent/bons-de-sortie/historique-bs/historique-bs.component.ts`
- `oas-front/src/app/agent/bons-de-sortie/bon-de-sortie.service.ts`
- `oas-front/src/app/shared/components/base-paginated.component.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/controller/BonDeSortieController.java`

**Aucune correction applicative n'a été appliquée pendant l'audit.**

### OAS-007 — Les pièces du bon de sortie peuvent être déduites deux fois avant/au début de la réparation

**Sévérité :** élevée — stock atelier diminué à tort
**État :** régression confirmée sur le parcours combinant bon de sortie et ordre de réparation.
**Zone :** validation du bon de sortie puis transition de l'ordre vers `REPARATION`.

Dans la baseline pré-août, la validation du bon de sortie déplaçait la quantité du magasin vers l'atelier (`stockMagasin -= q`, `stockAtelier += q`), puis le passage de la fiche à `REPARATION` retirait la quantité de l'atelier. La pièce était ainsi consommée une seule fois, au démarrage de la réparation.

Le cycle courant change le premier débit : `BonDeSortieServiceImpl.creer` effectue le transfert magasin→atelier, puis `valider` retire déjà la quantité du stock atelier. Après validation, le bon fait passer l'ordre à `ASSIGN_TECHNICIEN`. Quand le statut passe ensuite à `REPARATION`, `OrdreReparationServiceImpl.updateStatut` exécute encore le prélèvement historique sur les lignes de pièces du proforma. Le parcours OR frontend (`step-bon-sortie.component.ts`) envoie d'ailleurs toutes les lignes proforma et leurs quantités complètes au bon, même celles qui sont déjà disponibles en atelier. Si le stock atelier contenait déjà des unités avant ce transfert, le bon retire les quantités transférées et la transition `REPARATION` consomme ensuite à nouveau les quantités du proforma, entamant le stock préexistant. Le `Math.max(0, ...)` masque le déficit au lieu de le signaler. La première étape du même parcours peut aussi échouer si le magasin ne contient pas la quantité complète, même lorsque le stock atelier couvre déjà une partie du besoin.

**Preuve historique :** avant août, le service BDS ne débitait pas le stock atelier à la validation et `FicheAtelierServiceImpl` le débitait au statut `REPARATION`. Le nouveau cycle BDS en deux étapes est introduit dans l'implémentation courante de septembre (historique autour de `69a1d14`), mais le prélèvement à `REPARATION` est toujours présent dans la logique OR courante. Le cumul est particulièrement visible avec un stock atelier initial non nul; la forme exacte de la sortie BDS doit être mise en cohérence avec le besoin restant, comme l'était le flux de référence.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/service/BonDeSortieServiceImpl.java` (`creer`, `valider`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/service/OrdreReparationServiceImpl.java` (`updateStatut`)
- ancienne version : `src/main/java/sn/oas/facturation/bonDeSortie/service/BonDeSortieServiceImpl.java` et `src/main/java/sn/oas/facturation/ficheAtelier/service/FicheAtelierServiceImpl.java`

### OAS-008 — Les pièces personnalisées d'un proforma font échouer les transitions métier

**Sévérité :** élevée — réparation ou réception automatique bloquée
**État :** défaut de parcours confirmé dans l'état courant, introduit avec la prise en charge des lignes personnalisées.
**Zone :** lignes de proforma marquées `isCustom` / PDS.

Le backend proforma accepte explicitement une ligne personnalisée avec `isCustom=true`, `designationPds` et `piece=null`; le formulaire frontend offre également ce mode. Plusieurs traitements ultérieurs supposent pourtant systématiquement `lp.getPiece() != null` :

- lors du passage de l'ordre à `REPARATION`, `OrdreReparationServiceImpl.updateStatut` appelle `lp.getPiece().getId()` pour chaque ligne avant de vérifier son type; une ligne personnalisée provoque donc une `NullPointerException` et empêche le changement de statut ;
- lors de la réception d'un bon de commande liée à un ordre, les chemins de génération automatique du bon de sortie font également `lp.getPiece().getId()` sans ignorer les lignes personnalisées. Une ligne PDS peut faire échouer la réception complète et, selon le chemin transactionnel, annuler la réception en cours.
- à l'étape frontend « Bon de sortie » de l'OR, `step-bon-sortie.component.ts` envoie toutes les lignes, dont les personnalisées (`pieceId: null`), alors que `BonDeSortieServiceImpl.creer` appelle immédiatement `getPDP(pieceId)` et que le DTO BDS ne sait pas représenter une ligne PDS. Cette étape échoue également pour un ordre contenant une pièce personnalisée.
- lors de la conversion explicite d'un proforma en facture, `ProformaServiceImpl.convertToFacture` ne copie que `piece`, quantité et prix; il omet `isCustom` et `designationPds`. Une ligne personnalisée convertie conserve donc `piece=null` mais perd son nom, et le DTO de facture courant n'expose pas non plus le champ de désignation personnalisée.

Le flux d'approvisionnement frontend exclut explicitement les lignes `isCustom` du stock à approvisionner, ce qui confirme qu'elles ne doivent pas être traitées comme des PDP inventoriées. Les parcours backend ne respectent pas cette distinction.

**Preuve historique :** les services proforma et OR introduisent le stockage des lignes `isCustom` avec `piece` nullable dans les commits de refonte autour du 2026-09-02 (`69a1d14`), sans garde correspondante dans les traitements de démarrage réparation et réception/BS automatique.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/proforma/service/ProformaServiceImpl.java`
- `oas-back/src/main/java/sn/oas/facturation/features/proforma/controller/ProformaController.java` (`convertToFacture`)
- `oas-back/src/main/java/sn/oas/facturation/features/facture/dto/FactureResponse.java`
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/service/OrdreReparationServiceImpl.java` (`updateStatut`)
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/service/BonDeCommandeServiceImpl.java` (`receptionner`, `receptionnerAvecQuantites`)
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-proforma/step-proforma.component.ts`
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-approvisionnement/step-approvisionnement.component.ts`

**Aucune correction applicative n'a été appliquée pendant l'audit.**

### OAS-009 — L'étape Bon de sortie avance l'OR avant validation et saute les effets de validation

**Sévérité :** élevée — transition atelier et génération de facture
**État :** régression de séquencement confirmée dans le parcours OR ajouté en septembre.
**Zone :** étape frontend BDS → validation backend du BDS.

Après la création du bon de sortie, `step-bon-sortie.component.ts` appelle immédiatement `updateStatut(..., 'ASSIGN_TECHNICIEN')` et redirige vers l'assignation, alors que le backend vient de créer le bon en `EN_ATTENTE` et attend encore sa validation magasin. Or, lors de la validation du BDS, `BonDeSortieServiceImpl.valider` ne fait avancer la fiche et ne lance la notification du chef d'atelier ainsi que `factureService.createFactureAuto(fiche)` que si son statut est alors `BON_DE_SORTIE`, `BON_DE_COMMANDE` ou `PROFORMA`. Le frontend l'a déjà placé en `ASSIGN_TECHNICIEN`, donc la condition devient fausse : l'étape de validation du BDS saute ces effets.

**Preuve historique :** le parcours multi-étape est ajouté dans le frontend le 2026-09-18 (`3f2799d`/`0c54129`). Le service backend conserve le garde de transition autour de l'envoi notification + création automatique de facture; le nouveau composant avance lui-même le statut trop tôt.

**Fichiers :**
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-bon-sortie/step-bon-sortie.component.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/service/BonDeSortieServiceImpl.java` (`valider`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/service/OrdreReparationServiceImpl.java` (`updateStatut`)

**Aucune correction applicative n'a été appliquée pendant l'audit.**

### OAS-010 — La création d'un bon de commande depuis l'approvisionnement OR envoie un payload incompatible

**Sévérité :** élevée — approvisionnement des pièces manquantes bloqué
**État :** incompatibilité de contrat confirmée entre le frontend et le backend courant.
**Zone :** étape Approvisionnement d'un ordre de réparation.

Quand une pièce est manquante, `StepApprovisionnementComponent.createBonDeCommande()` construit des lignes avec les propriétés `pieceId` et `quantiteCommandee`, puis envoie l'objet via le service BDC. Le backend attend un champ `lignes` dont chaque ligne fournit `pieceDetacheeId` (ou les données de création d'une nouvelle pièce) et surtout `quantite`; `quantite` est annotée `@NotNull` et `@Min(1)`. La requête contient bien le tableau `lignes`, mais les identifiants/quantités du frontend ne remplissent pas les propriétés attendues par Jackson/Bean Validation. L'endpoint `POST /api/bons-de-commande` prend le DTO avec `@Valid`, donc le flux standard d'approvisionnement OR est rejeté avant création du bon. Le frontend masque en outre l'erreur du changement de statut qui suit l'appel de création; toutefois l'étape ne peut pas passer par la voie de succès si la création est rejetée.

**Preuve historique :** l'étape OR a été ajoutée dans le flux frontend le 2026-09-18 (`3f2799d`, `0c54129`). Son modèle BDC partagé déclare déjà `pieceDetacheeId` et `quantite`, tandis que le composant d'approvisionnement construit des propriétés différentes et force le type avec `as any`, contournant la vérification TypeScript.

**Fichiers :**
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-approvisionnement/step-approvisionnement.component.ts`
- `oas-front/src/app/agent/bons-commande/models/bon-de-commande.model.ts`
- `oas-front/src/app/agent/bons-commande/bon-de-commande.service.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/dto/LigneBonDeCommandeRequest.java`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/controller/BonDeCommandeController.java`

**Aucune correction applicative n'a été appliquée pendant l'audit.**

### OAS-011 — Créer une fiche atelier peut supprimer un ordre de réparation direct

**Sévérité :** élevée — suppression de données métier en cascade
**État :** risque destructif confirmé par le code et introduit le 2026-09-24 (`1f55c6b`).
**Zone :** création d'une fiche atelier pour un véhicule ayant déjà un OR actif.

`FicheAtelierServiceImpl.create()` recherche un ordre non livré pour le véhicule. Si l'ordre n'a pas de lien `ficheAtelier`, le service le supprime automatiquement puis poursuit la création de la fiche. Or le contrôleur OR expose toujours la création directe d'un ordre sans fiche atelier, et l'entité `OrdreReparation` documente explicitement ce cas comme valide (`fiche_atelier_id` nullable). La condition « sans fiche » ne suffit donc pas à distinguer une donnée obsolète d'un ordre direct encore actif.

La suppression peut aussi emporter les données enfants reliées en cascade à l'ordre, notamment son bon de sortie, son diagnostic, ses facturations et ses lignes. Pour un ordre direct encore utilisé, créer une fiche atelier pour le même véhicule peut donc supprimer l'ordre et une partie de son historique métier sans avertissement.

**Preuve historique :** le commit `1f55c6b` remplace le rejet de création en présence d'un OR actif par la suppression automatique de tout OR actif sans `ficheAtelier`. Le contrôleur courant garde l'endpoint de création directe; le modèle courant garde le lien vers fiche atelier nullable et le décrit comme optionnel.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/ficheAtelier/service/FicheAtelierServiceImpl.java` (`create`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/controller/OrdreReparationController.java` (`createOrdreReparation`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/data/entity/OrdreReparation.java` (lien nullable et cascades)

**Aucune correction applicative n'a été appliquée pendant l'audit.**

### OAS-012 — Les routes fournisseurs et partenaires sont ouvertes sans authentification, y compris en écriture

**Sévérité :** critique (sécurité et données multi-garage)
**État :** confirmé statiquement dans le code courant et dans l'historique Git.

Le commit `47f5d01d` du 2026-08-11 ajoute `/api/fournisseurs/**` et `/api/partenaires/**` à `permitAll()`. Le motif couvre toutes les méthodes HTTP. Les contrôleurs correspondants exposent sans garde de méthode `POST`, `PUT`, `DELETE` et `PATCH` d'archivage/désarchivage, en plus des routes de lecture. Il est donc possible d'appeler ces opérations sans authentification.

Pour les fournisseurs, l'impact dépasse la simple modification du référentiel : l'entité est `TenantAware` et porte un filtre Hibernate par `garage_id`, mais `GarageFilterAspect` n'active ce filtre que si le principal est un `Agent` ou un `Technicien`. Une requête anonyme autorisée sur ces routes contourne donc le filtre et les lectures repository peuvent retourner des fournisseurs de plusieurs garages. À la création anonyme, `TenantListener` ne trouve pas d'Agent et ne renseigne pas le garage ; la ligne peut être créée sans garage si la contrainte SQL effective l'accepte. Ce dernier point reste à vérifier sur le schéma déployé.

Le contrôleur partenaire permet également les écritures anonymes, mais l'entité partenaire n'est pas rattachée à un garage ; je classe cela comme exposition/modification publique du référentiel, séparément du défaut d'isolation fournisseurs.

**Preuve historique :** l'ajout des chemins publics apparaît dans `47f5d01d` (11 août). Le défaut courant est certain. La borne exacte de l'état avant cette modification reste à confirmer sur la véritable révision racine du dépôt, car la révision `0b0879b` contient déjà les routes ouvertes.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`
- `oas-back/src/main/java/sn/oas/facturation/features/fournisseur/controller/FournisseurController.java`
- `oas-back/src/main/java/sn/oas/facturation/features/fournisseur/data/entity/Fournisseur.java`
- `oas-back/src/main/java/sn/oas/facturation/features/partenaire/controller/PartenaireController.java`
- `oas-back/src/main/java/sn/oas/facturation/config/GarageFilterAspect.java`
- `oas-back/src/main/java/sn/oas/facturation/shared/tenant/TenantListener.java`

### OAS-013 — Un client peut créer, modifier, archiver et restaurer les garages

**Sévérité :** critique (autorisation)
**État :** confirmé par les règles de sécurité et les contrôleurs courants.

Depuis `dde9fc0` (14 août), le matcher `/api/admin/garages` et son wildcard acceptent `CLIENT` et `ROLE_CLIENT`. Le contrôleur expose sous ce même préfixe les opérations de lecture, mais aussi `POST` création, `PUT` modification, `DELETE` archivage et `POST /{id}/restore`; aucune méthode n'a de `@PreAuthorize` plus restrictive. Un utilisateur client authentifié satisfait donc la même règle pour les opérations administratives d'écriture.

Le commit `fe50469` (1er septembre) a momentanément ajouté `/api/admin/garages/**` à `permitAll()`, ce qui ouvrait toutes ces opérations aux anonymes dans cette version. Le commit `69a1d14` (2 septembre) a retiré cette ouverture et rétabli la règle avec CLIENT/ROLE_CLIENT : il a corrigé l'accès anonyme, mais conservé l'accès d'écriture des clients.

Le contrôleur garages semble aussi fournir la liste nécessaire au parcours de rendez-vous côté client. Cela peut expliquer le besoin de lecture côté client, mais ne justifie pas que la même autorisation s'applique aux méthodes de gestion. L'accès d'écriture est prouvé statiquement ; aucune requête en environnement réel n'a été envoyée pendant cet audit.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`
- `oas-back/src/main/java/sn/oas/facturation/features/garage/controller/GarageController.java`
- `oas-back/src/main/java/sn/oas/facturation/features/garage/service/GarageServiceImpl.java`

### OAS-015 — Le regroupement Marketplace expose les demandes de tous les clients et leurs transitions aux comptes clients

**Sévérité :** critique (confidentialité interclients et intégrité des commandes)
**État :** confirmé dans le code courant; introduit lors de la consolidation marketplace du 6 septembre (`b776b01`).

Le portail client courant appelle `GET /api/marketplace/demandes` pour afficher « mes demandes ». Or le contrôleur appelle `demandeProduitService.getAll()`, qui renvoie `demandeProduitRepository.findAll()` sans filtrer par propriétaire. L’endpoint renvoie les lignes `DemandeProduit` complètes. Le client peut donc obtenir les demandes des autres comptes avec leurs quantités, messages, statuts, produits et références.

La même famille de contrôleur expose `GET /demandes/{id}` par ID sans contrôle du client propriétaire et les routes `accepter`, `refuser`, `en-cours`, `cloturer`, `commander` et `livrer`. Elles appellent directement `updateStatus`; ni le contrôleur ni le service ne vérifient que l’appelant a un rôle administratif. `WebSecurityConfig` ne comporte aucune règle spécifique pour `/api/marketplace/**`, donc ces routes tombent sous `.anyRequest().authenticated()`: tout client connecté peut lire une demande par ID et invoquer les transitions d’administration.

Le service `cancel()` utilise au contraire `findByIdAndClient` et limite l’annulation du client à ses propres demandes en attente. Cela montre qu’un contrôle de propriété existe pour cette action, mais n’a pas été appliqué aux lectures générales/par ID.

**Preuve historique :** les routes consolidées `/api/marketplace/**` apparaissent dans `b776b01` (6 septembre). Auparavant, le portail utilisait `/api/client/marketplace/...` pour le catalogue public et ses opérations de demande; la consolidation a changé l’URL front sans ajouter de règle d’accès pour le nouveau préfixe et a raccordé l’UI « mes demandes » au handler global `getAllDemandes()`.

**Fichiers :**
- `oas-front/src/app/client/marketplace/client-marketplace.service.ts` (`getMesDemandes`)
- `oas-back/src/main/java/sn/oas/facturation/features/marketplace/controller/MarketplaceController.java`
- `oas-back/src/main/java/sn/oas/facturation/features/marketplace/service/DemandeProduitServiceImpl.java`
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`

### OAS-016 — Le catalogue Marketplace public appelle une ancienne route backend supprimée

**Sévérité :** élevée (fonctionnalité publique masquée par les données de secours)
**État :** confirmé par comparaison historique frontend/backend.

La page publique `/marketplace` utilise `MarketplaceService` et appelle toujours `/api/client/marketplace/produits` ainsi que `/api/client/marketplace/produits/populaires`. Ces routes correspondaient à l’ancien `ProduitClientController`, ajouté en août (`177ca3b`). Le 6 septembre, le backend a remplacé les contrôleurs séparés par le contrôleur unifié `MarketplaceController` sous `/api/marketplace`, supprimant l’ancien préfixe. La règle Spring Security conserve pourtant `/api/client/marketplace/produits/**` dans `permitAll`, ce qui ne rétablit pas un handler HTTP; aucune route backend courante n'est mappée sur ce préfixe.

La page publique attrape l’échec HTTP et affiche `MOCK_PRODUCTS`. Cela masque le défaut en donnant l’impression que le catalogue fonctionne, mais les données montrées sont statiques et les vrais produits/données édités dans l’admin ne sont plus ceux affichés. Le client connecté a, lui, migré son service vers `/api/marketplace`; le service public n’a pas suivi.

**Preuve historique :** l’ancien backend `/api/client/marketplace` est créé dans `177ca3b`; son remplacement/suppression se produit dans `b776b01` du 6 septembre, qui ajoute `@RequestMapping("/api/marketplace")`. Le service public frontend utilise toujours le préfixe historique aujourd’hui, alors que le service du portail client bascule vers `/api/marketplace` dans le snapshot frontend du 18 septembre (`88053a5`).

**Fichiers :**
- `oas-front/src/app/services/marketplace.service.ts`
- `oas-front/src/app/public/marketplace/marketplace.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/marketplace/controller/MarketplaceController.java`
- historique `oas-back/src/main/java/sn/oas/facturation/marketplace/controller/ProduitClientController.java` (supprimé par réorganisation)

### OAS-017 — Les endpoints technicien renvoient l'entité User complète, avec son hash de mot de passe

**Sévérité :** critique (exposition de secrets d’authentification)
**État :** confirmé par sérialisation statique et périmètre d’accès; introduit avec le nouveau type Technicien le 17 août (`15e0ea2`).

`Technicien` hérite de `User`, dont `password` est un champ avec getter Lombok généré et sans `@JsonIgnore`/masquage constaté. Or `GET /api/techniciens/{id}` et `PUT /api/techniciens/{id}` renvoient directement l’entité `Technicien`. La réponse transporte donc la propriété `password` (hash stocké, pas le mot de passe en clair), en plus des champs du compte et des relations. La liste courante utilise un DTO, mais ne protège pas la route de détail ni la réponse de mise à jour.

`WebSecurityConfig` ne définit pas de règle spécifique pour `/api/techniciens/**`; ces routes tombent sur `.anyRequest().authenticated()`. Un compte client ou autre compte authentifié peut ainsi appeler ces endpoints et lire le hash d’un technicien par ID; il peut également modifier un compte technicien via PUT et reçoit le hash dans la réponse. Le service ne masque pas le champ et ne vérifie pas le rôle sur ces opérations.

**Preuve historique :** le commit `15e0ea2` transforme le mécanicien (entité métier simple) en `Technicien extends User`, ajoute son CRUD de compte puis ne restreint par backend que `/api/technicien/**` (portail); `/api/techniciens/**` reste sous l’accès générique authentifié. Dans l’ancien modèle `Mecanicien`, il n’y avait pas de champ d’authentification `password`; la migration a donc introduit la donnée secrète dans les entités sérialisées par le CRUD.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/user/data/entity/User.java` (`password`)
- `oas-back/src/main/java/sn/oas/facturation/features/technicien/data/entity/Technicien.java`
- `oas-back/src/main/java/sn/oas/facturation/features/technicien/controller/TechnicienController.java`
- `oas-back/src/main/java/sn/oas/facturation/features/technicien/service/TechnicienServiceImpl.java`
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`

**Surface supplémentaire constatée depuis le premier relevé :** `GET /api/technicien/portal/me` renvoie également directement `Technicien`, donc hérite du même risque de sérialisation du champ `password`. Cette route est limitée aux techniciens connectés; elle n'élargit pas l'accès aux autres rôles, mais montre que le DTO léger n'est pas appliqué à toutes les réponses.

**Surface supplémentaire constatée depuis le premier relevé :** le portail ajouté le 2 septembre expose aussi `GET /api/technicien/portal/me` comme `ResponseEntity<Technicien>`; comme Technicien hérite de User, cette réponse inclut également le champ `password` (hash). Cette route est limitée aux autorités technicien, donc elle n'élargit pas l'accès à un autre rôle, mais confirme que le DTO de liste n'a pas été appliqué à toutes les réponses.

### OAS-018 — L’archivage client d’un véhicule ayant un ordre terminé est toujours refusé

**Sévérité :** moyenne (fonctionnalité bloquée après réparation)
**État :** confirmé par comparaison du prédicat ajouté le 18 septembre (`a9cd202`) avec l’enum de statuts courant.

Le service d’archivage considère un ordre comme actif sauf si son statut s’appelle `TERMINE` ou `ANNULE`. Or l’enum `StatutOrdreReparation` courant n’a aucun de ces deux états : il utilise notamment `PAIEMENT`, `PRET_A_LIVRER` et `LIVRE` comme états de fin, et ces mêmes valeurs sont explicitement classées terminales par le BFF client. En conséquence, un véhicule qui conserve un historique d’OR, même livré, déclenche toujours `hasActiveRepairs=true` et l’archivage est rejeté.

Le portail masque la fiche en cours une fois l’intervention terminée, puis rend le bouton d’archivage lorsque `ficheEnCours` est absent. L’utilisateur peut donc voir et déclencher « Archiver ce véhicule », mais l’API répond « Impossible d’archiver un véhicule avec des réparations en cours » même si son dernier OR est `LIVRE`.

**Preuve historique :** le service d’archivage est ajouté dans `a9cd202` le 18 septembre. Le BFF client, introduit après, utilise `PAIEMENT`, `PRET_A_LIVRER`, `LIVRE` comme états terminaux, ce qui confirme le vocabulaire métier attendu.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/vehicule/service/VehiculeServiceImpl.java` (`archiveVehiculeByClient`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/data/enums/StatutOrdreReparation.java`
- `oas-back/src/main/java/sn/oas/facturation/features/clientportal/service/ClientPortalServiceImpl.java` (`TERMINATED_STATUSES`)
- `oas-front/src/app/client/vehicules/client-vehicules.component.html` (condition d’affichage du bouton)

### OAS-019 — Les écritures d’articles de blog sont exposées publiquement

**Sévérité :** élevée (intégrité du contenu public)
**État :** confirmé; le `permitAll` a été introduit lors de l’ajout du blog le 3 août (`c2a55ed`).

`WebSecurityConfig` place `/api/blog/**` dans `permitAll()`. Le contrôleur blog comporte les lectures publiques attendues, les commentaires/réactions de visiteurs, mais également `POST /api/blog`, `PUT /api/blog/{id}` et `DELETE /api/blog/{id}`, pourtant décrits comme opérations d’administration. Aucune annotation de sécurité au niveau contrôleur ou méthode ne les restreint. Les endpoints de contenu permettent donc à un appelant anonyme de créer, remplacer et supprimer des articles publiés.

**Preuve historique :** le commit `c2a55ed` du 3 août ajoute simultanément le module blog et le wildcard `/api/blog/**` à `permitAll`; les opérations administratives sont dans le contrôleur du module courant sans garde de méthode.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`
- `oas-back/src/main/java/sn/oas/facturation/features/blog/controller/BlogController.java`
- `oas-back/src/main/java/sn/oas/facturation/features/blog/service/BlogPostServiceImpl.java`

### OAS-020 — Le remplacement de Mécanicien par Technicien ne migre pas les associations existantes

**Sévérité :** élevée si la base contient déjà des mécaniciens/affectations (risque de données et de démarrage)
**État :** risque de migration confirmé dans les changements; impact sur une base réelle à vérifier selon son contenu et les contraintes générées.

Le commit `15e0ea2` remplace le type Java des deux associations d’OR (`mecaniciens` et `mecaniciensReparation`) par `Technicien`, mais conserve explicitement les tables de jointure `fiche_mecaniciens`, `fiche_mecaniciens_reparation` et la colonne `mecanicien_id`. Avant ce changement, ces tables référençaient les IDs de l’entité/table `Mecanicien`; après, le mapping les interprète comme des IDs de `Technicien`, lequel est un compte utilisateur joint dans `techniciens`/`users`.

Je n’ai trouvé aucune migration Flyway/Liquibase ni script de conversion des anciennes lignes ou des identifiants. Le seeder SQL garde aussi les anciennes associations avec les IDs 14, 15 et 18, mais cela ne constitue pas une migration des données existantes. Avec `ddl-auto: update`, l’application peut tenter d’adapter les contraintes de clés étrangères, mais cette option ne crée pas les nouveaux comptes technicien correspondants et ne convertit pas les IDs historiques. Selon l’état de la base, les associations existantes peuvent devenir orphelines, pointer vers le mauvais technicien si les IDs coïncident, ou faire échouer l’ajout de la contrainte/le démarrage.

Je classe ceci comme risque de régression de données introduit par le remplacement du modèle, et non comme perte de production constatée : la base réelle n’a pas été inspectée et aucune migration n’a été exécutée pendant l’audit.

**Fichiers :**
- historique `oas-back/src/main/java/sn/oas/facturation/mecanicien/data/entity/Mecanicien.java` (table source supprimée)
- historique `oas-back/src/main/java/sn/oas/facturation/ordreReparation/data/entity/OrdreReparation.java` (associations avant migration)
- `oas-back/src/main/java/sn/oas/facturation/features/technicien/data/entity/Technicien.java`
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/data/entity/OrdreReparation.java`
- `oas-back/src/main/resources/application-prod.yml` (`ddl-auto: update`)
- `oas-back/src/main/resources/seed_all_entities.sql` (associations de démonstration conservées)

### OAS-002 — Le bouton « ouvrir l'ordre » d'une fiche atelier n'ouvre pas l'ordre lié (frontend)

**Sévérité :** moyenne
**État :** confirmé dans l'état actuel.

La méthode `ouvrirOrdreReparation()` navigue vers `/app/ordres-reparation?ficheAtelierId=...`, mais la liste OR ne consomme pas ce query param et n'identifie pas l'ordre correspondant. L'utilisateur atterrit sur la liste générique au lieu d'ouvrir le détail associé.

**Fichiers :** `oas-front/src/app/agent/fiches-atelier/fiche-atelier-details/fiche-atelier-details.ts` (~117–120), liste et routes OR.

### OAS-003 — Filtres/recherche de la liste OR transmis mais ignorés par le backend

**Sévérité :** moyenne
**État :** confirmé dans l'état actuel.

Le frontend transmet `keyword`, `statut`, `dateDebut` et `dateFin` avec la pagination ; `OrdreReparationController.getAllOrdresReparation` ne déclare que `page` et `size`, puis appelle `findAll(pageable)`. La liste n'applique donc pas ses filtres/recherche serveur ; les pages suivantes sont paginées sans recherche filtrée.

**Fichiers :**
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation.service.ts` (~12–20)
- `oas-front/src/app/agent/ordres-reparation/ordres-reparation.component.ts` (~88–96)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/controller/OrdreReparationController.java` (~37–44)
- Le repository a une méthode de recherche mais elle n'est pas raccordée au endpoint liste.

### OAS-004 — Risque d'exposition du hash de mot de passe via des endpoints OR

**Sévérité :** critique (sécurité), **confiance :** forte, vérification runtime encore nécessaire.

`GET /api/ordres-reparation/me` et `GET /api/ordres-reparation/{id}` renvoient des entités `OrdreReparation`. La sérialisation peut traverser `ordre.vehicule.client` ; `Vehicule` n'ignore que la collection `vehicules`, alors que `User.password` est une propriété getter Lombok sans `@JsonIgnore`. Aucun filtre/mix-in global n'a été trouvé dans la revue ciblée. Il faut vérifier la charge JSON dans un environnement maîtrisé sans exposer d'information réelle, puis préférer un DTO minimal.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/controller/OrdreReparationController.java` (~55–75)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/data/entity/OrdreReparation.java` (véhicule)
- `oas-back/src/main/java/sn/oas/facturation/features/vehicule/data/entity/Vehicule.java` (~46–49)
- `oas-back/src/main/java/sn/oas/facturation/features/user/data/entity/User.java` (~51)

## Fonctionnalité supprimée confirmée

### OAS-005 — Inscription client « entreprise » retirée lors du refactor d'authentification

**Sévérité :** élevée si la clientèle entreprise est attendue
**État :** régression historique confirmée ; restauration front/back réalisée le 2026-10-04, build et tests ciblés backend réussis.

Le formulaire client précédent gérait les types `PARTICULIER` et `ENTREPRISE`, `raisonSociale`, `numeroEntreprise`, email/téléphone/adresse professionnels et génération d'un login technique. La refonte du 2026-09-02 (`68ec6d3`, `refactor: login with signal`) a supprimé `client-register`/`client-login` dédiés et redirige maintenant les anciennes routes client vers `/register` et `/login`. Le formulaire générique ne proposait que prénom, nom, téléphone, email et mot de passe et envoyait `type: 'CLIENT'`. Le DTO backend ne contenait ni `typeClient`, ni raison sociale, ni numéro d'entreprise, ni contact entreprise. C'était une perte de capacité end-to-end, pas seulement un changement de formulaire.

**Restauration du 2026-10-04 :** le formulaire public et le formulaire de création agent proposent à nouveau `PARTICULIER`/`ENTREPRISE`; les formulaires agent peuvent aussi modifier le type et les informations d'entreprise d'un client existant. Le contrat d'inscription accepte les données légales et l'adresse de l'entreprise, et les contacts entreprise alimentent l'email et le téléphone du compte `Client` (qui hérite de `User`). Le NINEA légal est stocké séparément de `Client.ninea`, champ existant qui conserve son usage de lien de justificatif du compte fidèle. Les réponses de liste/création exposent le type et les champs entreprise. Les clients/créations historiques sans type restent `PARTICULIER` par défaut.

**Limites assumées :** cette restauration retrouve le compte client entreprise avec accès utilisateur, comme dans le flux historique. Elle ne réalise pas le chantier distinct du compte financier entreprise (crédit, remise, plafond, échéance, blocage de proforma et rendez-vous). Lorsqu'un agent crée un client sans mot de passe explicite, le flux existant génère toujours un mot de passe temporaire; l'inscription publique conserve un mot de passe choisi par l'utilisateur.

**Vérifications :** `oas-back`: compilation Maven réussie et `AuthServiceEnterpriseRegistrationTest` (3 tests) réussi, couvrant entreprise, NINEA obligatoire et compatibilité de l'inscription particulier historique. `oas-front`: `npm run build` réussi; avertissement de budget bundle dépassé de 17,63 kB. Les tests Karma ne sont pas confirmés dans cette restauration.

**Fichiers :**
- ancien `oas-front/src/app/client-portal/auth/client-register/client-register.component.ts` et HTML (présents avant `68ec6d3`)
- `oas-front/src/app/client/client-portal.routes.ts` (~5–7)
- `oas-front/src/app/auth/register/register.component.ts` (~21–75)
- `oas-back/src/main/java/sn/oas/facturation/features/auth/dto/request/RegisterRequest.java` (~7–22)
- `oas-back/src/main/java/sn/oas/facturation/features/auth/service/AuthServiceImpl.java` (branche CLIENT)

## Autres changements déjà parcourus

- **Réorganisation majeure Angular** : `gestion` → `agent`, portail client déplacé sous `client`, routage centralisé. Le gros des suppressions Git de chemins historiques correspond à des déplacements ; routes/modules fonctionnels visibles encore présents pour les fonctionnalités listées. Audit des gardes/roles à poursuivre.
- **Réorganisation majeure backend** : packages historiques déplacés sous `features`. Les suppressions de fichiers/classes bruts dans les diffs sont en grande partie des déplacements, évolutions DTO et nettoyage de fichiers temporaires. Leur présence dans `--diff-filter=D` ne prouve pas une perte métier.
- **Routage du formulaire public de devis** : `/devis` est commenté aujourd'hui et des boutons Services pointent encore vers `/devis`. Cependant, la page historique était statique (pas de formulaire Angular fonctionnel ni de soumission) et avait été désactivée avant la baseline août ; ce constat est donc exclu de la liste des régressions de la période.
- Le flux détaillé « bon de sortie » ci-dessus est prioritaire pour examiner le stock ; les données de la capture sont cohérentes avec les snapshots du code actuel, mais on doit confirmer la définition métier exacte de « SORTIE » vs « SORTIE ATELIER » avant de qualifier toute ligne supplémentaire de doublon physique.

## Journal des étapes

### Étape 1 — cadrage et flux sortie atelier

- Baselines et branches identifiées dans les deux dépôts.
- État de travail utilisateur détecté dans les deux repos ; préservé et non assimilé à l'historique de référence.
- Revu le flux backend bon de sortie : création, mouvement, validation, historisation, retour et DTO de l'historique global.
- Confirmé OAS-001 à OAS-005 ci-dessus.

### Étape 2 — historique stock et pagination des bons

- Suivi séparé des tables d'historique des bons de sortie et des mouvements généraux de pièces détachées.
- Confirmé que les snapshots stock de l'historique BS viennent du backend et sont affichés tels quels. Correction de la formulation précédente : les valeurs visibles dans la capture (0/0/0 et 49/1/50) sont cohérentes entre elles et ne constituent pas à elles seules la preuve d'un écart de stock.
- Comparé le code avant/après le commit front `5d888f9` : découverte et preuve de la régression OAS-006 sur filtres, pagination et export CSV.
- L'inventaire physique et les mouvements stock génériques recalculent explicitement `qteReelle` dans leur chemin courant ; le problème de recalcul identifié reste localisé au flux bons de sortie.

### Étape 3 — ordre de réparation, proforma, commande et réception

- Comparé le prélèvement de stock au statut `REPARATION` avec la baseline pré-août et le nouveau cycle BDS ; consigné le risque confirmé de double débit quand les mêmes quantités sont présentes dans le bon de sortie et le proforma (OAS-007).
- Suivi les lignes personnalisées du formulaire proforma jusqu'au modèle backend puis aux transitions de réparation, à la création du BDS et à sa génération automatique après réception ; constaté les déréférencements de pièce nulle (OAS-008).
- Comparé le payload créé par l'approvisionnement frontend au DTO et aux contraintes de validation backend ; confirmé le mauvais mapping des champs et le rejet de la demande (OAS-010).
- Suivi l'étape BDS frontend jusqu'aux effets secondaires backend de validation; confirmé que le statut est avancé avant l'approbation, ce qui neutralise la condition de notification et de facture auto à la validation (OAS-009).
- Revue initiale de la réception partielle/complète : les quantités reçues incrémentent les lignes et les mouvements d'entrée stock; poursuite nécessaire sur édition, annulation et documents générés.

### Étape 4 — fiche atelier et rendez-vous

- Comparé l'évolution de `FicheAtelierServiceImpl.create()` avec sa version du 2026-09-24 : l'ancien garde qui refusait la création en présence d'un OR actif a été remplacé par une suppression de l'ordre sans fiche liée (OAS-011).
- Vérifié que les OR directs restent créables via le contrôleur courant et sont permis par le modèle; identifié un risque de suppression en cascade pour ces ordres valides.
- La séparation entre confirmation de rendez-vous et création de fiche atelier paraît intentionnelle dans le flux courant; la validation RDV ne crée plus directement un OR et l'écran courant se limite à confirmer le rendez-vous. Pas classée comme régression à ce stade.

### Étape 5 — permissions et isolation par garage

- Suivi des règles `WebSecurityConfig` vers les routes des contrôleurs. Confirmé que `permitAll` fournisseur/partenaire couvre les méthodes d'écriture et qu'aucune annotation de méthode ne la restreint (OAS-012).
- Suivi du filtre multi-garage des fournisseurs : il est activé pour Agent/Technicien seulement; les requêtes anonymes autorisées sur ces routes ne reçoivent pas ce filtre.
- Comparé les règles garage des commits du 14 août, 1er septembre et 2 septembre. Confirmé qu'un client peut toujours appeler les méthodes d'écriture du contrôleur garage (OAS-013); l'ouverture anonyme intermédiaire du 1er septembre a été retirée le lendemain.
- Parcouru le portail client et les routes de profil. Vérifié que le formulaire de coordonnées les garde désactivées et que son texte explique leur caractère non modifiable; ce comportement est cohérent avec son implémentation et n’est pas retenu comme régression. Le service backend d’édition par ID ne vérifie toujours pas la propriété, mais ce défaut est préexistant à août.
- Contrôlé les endpoints de changement de mot de passe : le client fournit username et ancien mot de passe à l’endpoint historique; ce n’est pas une suppression de l’endpoint connecté, qui est toujours utilisé dans l’espace agent. Pas retenu comme régression.
- Suivi du client Marketplace après la bascule de route du 6 septembre. Confirmé que `getMesDemandes()` appelle un handler `findAll()` non filtré et que les endpoints détail/transitions ne restreignent pas le rôle client (OAS-015).
- Comparé l’ancien contrôleur public Marketplace avec la route unifiée. Confirmé que la page publique garde son URL obsolète et retombe sur les produits fictifs de secours depuis la suppression de l’ancien handler (OAS-016).
- Comparé le CRUD technicien ajouté lors du remplacement de Mecanicien. L’entité nouvellement convertie en compte User est renvoyée directement pour détail/mise à jour, sans exclusion du hash; les routes CRUD restent accessibles à tout utilisateur authentifié (OAS-017).
- Comparé le prédicat d’archivage véhicule introduit le 18 septembre à l’enum de statuts et au BFF client. Confirmé que le garde compare des états (`TERMINE`/`ANNULE`) inexistants et refuse ainsi tout véhicule avec un OR historique, même `LIVRE` (OAS-018).
- Audité le wildcard public du blog jusqu’aux handlers. Confirmé qu’il couvre les opérations d’écriture et de suppression d’articles, pourtant présentées comme admin, sans contrôle de rôle; l’ouverture date de la création du module (OAS-019).
- Comparé les associations Mécanicien d’avant le 17 août au mapping Technicien courant. Le type de cible change mais les noms des tables/colonnes restent; aucune migration des données d’association ni conversion des IDs n’a été trouvée (OAS-020; impact réel conditionné au contenu de la base).

### OAS-021 — Le PDF proforma part par email avant la validation des prix

**Constat confirmé — régression introduite le 2 octobre 2026 (`52f6238`, puis répétée dans `f0a09b8`).**

Dans `ProformaServiceImpl.create()`, le commit `52f6238` ajoute l'envoi du PDF au client immédiatement après la création. Pourtant, l'écran agent demande encore explicitement de vérifier/ajuster les prix puis d'appuyer sur « Valider les prix et envoyer au client ». Le flag `visibleClient` reste bien à `false` jusqu'à cette validation et le portail filtre sur ce flag : **l'accès portail n'est donc pas prématuré**, mais le document est déjà divulgué par email avant l'étape de contrôle. Le commit `f0a09b8` ajoute ensuite un deuxième email dans `validerEnvoi()`.

La régression effectivement confirmée est différente et observable: le client reçoit un proforma par email dès sa création, tandis que l'écran agent conserve l'étape explicite « Ajustez les prix si besoin … puis validez pour l'envoyer » et son bouton « Valider les prix et envoyer au client ». L'email contient déjà le PDF avant cette validation. `validerEnvoi()` renvoie aussi un second email une fois l'agent valide l'envoi; le client peut ainsi recevoir deux messages, le premier avec un document encore en préparation puis celui marqué prêt à valider. La formulation de `52f6238` (« vient d'être créé ») et le PDF joint ne respectent pas le garde affiché par l'écran.

**Impact :** divulgation au client de prix/lignes avant leur revue agent, confusion sur la version approuvée et deux emails pour le même proforma si l'adresse est présente. Le flag portail continue, lui, de respecter le contrôle de visibilité.

**Fichiers concernés :**

- `oas-back/src/main/java/sn/oas/facturation/features/proforma/service/ProformaServiceImpl.java` (`create()`, nouvel envoi du PDF dans `52f6238`; second envoi dans `validerEnvoi()` au commit `f0a09b8`).
- `oas-front/src/app/agent/proforma/proforma.component.html` (instruction de revue et bouton conditionné à `!visibleClient`).
- `oas-back/src/main/java/sn/oas/facturation/features/proforma/repository/ProformaRepository.java` (le portail client filtre `visibleClient=true`, ce qui borne précisément l'impact).

### Étape 6 — devis, proformas, factures et paiements

- Revu les transitions de reçus : le changement du 18 septembre remplace `EN_ATTENTE_PAIEMENT -> TERMINE` par `PAIEMENT -> PRET_A_LIVRER`. Les deux états de l'ancien code ne figurent plus dans l'enum courant; le nouveau couple correspond aux statuts métier disponibles. Pas de régression établie sur ce changement.
- Comparé l'API reçu après le refactor de DTO au modèle consommé par l'écran agent : le contrôleur mappe `Recu` vers `RecuResponse` et conserve numéro/client/facture/OR/montant/mode/date. Le retrait du mapping dans le service n'est donc pas, seul, une perte de données affichées.
- Comparé les chemins de création manuelle et automatique de facture. L'automatique copie les champs des pièces personnalisées, tandis que le manuel ne copie que pièce/quantité/prix. L'écart existait déjà dans la baseline avant la migration du 12 août et relève d'une faiblesse préexistante, non d'une régression datée de cette période; le cas des conversions proforma demeure couvert par OAS-008.
- Vérifié les courriels transactionnels ajoutés le 2 octobre sur facture : ils interviennent après sauvegarde et génération PDF dans la même méthode transactionnelle. Le service email est `@Async` et intercepte les erreurs d'envoi; je ne classe pas l'échec SMTP comme rollback métier confirmé. Risque résiduel à valider en environnement (configuration SMTP / exécuteur async), sans reproduction demandée ni service lancé.
- Confirmé OAS-021 : le nouvel email de création de proforma transmet le PDF avant le garde d'approbation explicite dans l'écran agent; l'email de `validerEnvoi()` peut ensuite renvoyer le document approuvé.
- Inspecté `EmailServiceImpl`: son envoi avec pièce jointe appelle `MimeMessageHelper.setText(text)` en mode texte brut. Les mails facture et le mail `validerEnvoi()` de proforma lui passent des balises HTML; ces balises seront donc présentées comme texte littéral dans le corps, au lieu d'être rendues. Le mail de création proforma est, lui, du texte brut et ne souffre pas de ce défaut.
- Les permissions d'autres familles d'endpoints et les contrôles tenant sur les flux client/technicien restent à parcourir.

### OAS-022 — Les courriels facture/proforma affichent les balises HTML comme du texte

**Constat confirmé dans le code introduit le 2 octobre 2026 (`f0a09b8`).**

`EmailServiceImpl.sendEmailWithAttachment(..., byte[], ...)` crée `MimeMessageHelper` en mode multipart, mais appelle `helper.setText(text)` sans l'option HTML. Les deux chemins de création de facture et `ProformaServiceImpl.validerEnvoi()` fournissent des corps contenant `<p>` et `<b>`. Le helper les envoie donc comme texte brut : les balises sont affichées au destinataire au lieu de former des paragraphes et du texte en gras. Le message de création proforma ajouté dans `52f6238` utilise `\n` et n'est pas concerné.

**Impact :** présentation incorrecte de messages envoyés aux clients à chaque facture avec email renseigné, et à l'envoi d'un proforma; le PDF attaché n'est pas touché.

**Fichiers :**

- `oas-back/src/main/java/sn/oas/facturation/features/notification/service/EmailServiceImpl.java` (`sendEmailWithAttachment` avec `byte[]`).
- `oas-back/src/main/java/sn/oas/facturation/features/facture/service/FactureServiceImpl.java` (création manuelle et automatique, corps HTML).
- `oas-back/src/main/java/sn/oas/facturation/features/proforma/service/ProformaServiceImpl.java` (`validerEnvoi()`, corps HTML).

### OAS-023 — Une commande envoyée ou reçue peut être réécrite, y compris l'identité d'une pièce déjà réceptionnée

**Régression confirmée — garde élargie le 31 août 2026 (`e7f8512`).**

La version antérieure n'autorisait la modification d'un bon de commande qu'au statut `EN_ATTENTE`. Le changement du 31 août remplace ce garde par un refus uniquement lorsque la commande est `ANNULE`. En conséquence, une commande `ENVOYE`, `INCOMPLET` ou `RECU` reste éditable, et les champs fournisseur, véhicule, observation, quantités et prix sont réécrits.

Le nouveau contrôle protège la suppression d'une ligne avec réception et empêche de réduire sa quantité sous le nombre déjà reçu, mais il ne protège pas l'identité de la pièce. Pour une ligne ayant `quantiteRecue > 0`, le code d'update peut remplacer `pieceDetachee` par une autre pièce catalogue ou la mettre à `null` au profit d'une désignation PDS. Or les unités déjà réceptionnées ont été ajoutées au stock de l'ancienne PDP. La ligne de commande présente alors la nouvelle pièce avec le compteur reçu historique, sans annuler le mouvement de stock initial. L'écran expose le bouton « Modifier le bon de commande » pour tout statut sauf `ANNULE`, et le sélecteur catalogue reste actif même quand la ligne porte un compteur reçu; seule la bascule explicite catalogue/PDS est bloquée.

**Impact :** perte de traçabilité entre commande, fournisseur/véhicule, réception et stock; une correction légitime de commande peut faire apparaître des pièces déjà reçues sur le mauvais article, avec quantité déjà reçue héritée. Le chemin est exposé à la fois par le backend et par le bouton d'édition de l'écran agent.

**Fichiers :**

- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/service/BonDeCommandeServiceImpl.java` (`update`, garde et remapping des lignes ajoutés/modifiés le 31 août).
- `oas-front/src/app/agent/bons-commande/bons-commande.component.html` (bouton actif pour tout statut non annulé; select catalogue actif sur lignes reçues).
- `oas-front/src/app/agent/bons-commande/bons-commande.component.ts` (`openEdit`, `save`, contrôle reçu qui ne compare pas l'ID de pièce).

### OAS-024 — L'historique des mouvements antérieur au 3 septembre est laissé dans l'ancienne table

**Risque de perte historique confirmé dans le code de migration; impact réel dépend de la base utilisée.**

Au 12 août, `StockMouvement` persistait dans `stock_mouvements`. La classe est enrichie le 29 août, puis renommée `PieceMouvement` et basculée vers `@Table(name = "piece_mouvements")` le 3 septembre (`acc6b32`). Dans ce même refactor, `StockServiceImpl`, `InventaireServiceImpl`, les repositories et les contrôleurs basculent leurs lectures vers `PieceMouvementRepository`, lequel ne lit que `piece_mouvements`.

Une migration applicative `PieceMouvementTableMigrator` avait été ajoutée le 3 septembre, mais le commit `b776b01` l'a supprimée le 6 septembre. Elle n'est pas présente dans la version courante. En outre, même la version supprimée ne copiait les lignes que si la nouvelle table était entièrement vide, utilisait `INSERT ... SELECT *` (donc supposait un schéma identique) et avalait toute exception en journalisant seulement un avertissement. `ddl-auto: update` ne migre pas les lignes. Le seul SQL courant qui mentionne les deux tables est le seed de démonstration : il insère des exemples dans `piece_mouvements`, puis copie dans le sens inverse vers `stock_mouvements`; il ne reprend pas l'historique de production. De plus, `TypeMouvement.SORTIE` présent dans l'enum antérieur a disparu de l'enum courant (remplacé fonctionnellement par `SORTIE_MAGASIN_VERS_ATELIER`), donc une copie brute de cette valeur ancienne demanderait une conversion.

**Impact :** après déploiement du changement sur une base qui avait déjà des mouvements, l'historique visible par les endpoints courants peut repartir de la nouvelle table sans les mouvements écrits avant le refactor. Les rapports et recherches par pièce ne retrouvent alors qu'une partie de l'historique. Le contenu de la base de production n'ayant pas été inspecté, je ne déclare pas que les données d'une instance réelle ont été perdues; je confirme l'absence de migration dans le dépôt.

**Fichiers et changements :**

- baseline `oas-back/src/main/java/sn/oas/facturation/piecedetache/data/entity/StockMouvement.java` (`stock_mouvements`).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/data/entity/PieceMouvement.java` (table courante `piece_mouvements`).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/repository/PieceMouvementRepository.java` et `StockServiceImpl.java` (lectures courantes).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/data/enums/TypeMouvement.java` (`SORTIE` absent de l'enum courant).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/config/PieceMouvementTableMigrator.java` (ajoutée par `acc6b32`, supprimée par `b776b01`; sa logique avait des limites même avant suppression).
- `oas-back/src/main/resources/seed_all_entities.sql` (copie de seed du nouveau vers l'ancien nom seulement; pas de migration des données existantes).

### Étape 7 — bons de commande et réception

- Comparé `BonDeCommandeServiceImpl.update()` à la baseline antérieure au 31 août : le garde « uniquement EN_ATTENTE » est devenu « tous les statuts sauf ANNULE » (OAS-023).
- Suivi la réception partielle jusqu'à `StockService.entree()` et le compteur `quantiteRecue`; les quantités déjà réceptionnées sont protégées contre la suppression de ligne et la baisse de quantité, mais pas contre le remplacement de la pièce, du fournisseur ou du véhicule (OAS-023).
- Vérifié l'écran agent : édition proposée sur toute commande non annulée et select pièce encore actif pour les lignes reçues. Le backend permet également l'appel direct.
- Le flux « réception totale » historique et l'auto-création du bon de sortie existaient avant août. Les écarts structurels plus anciens (catch silencieux lors d'un BDS automatique et pas de document de réception dans cette branche) ne sont pas classés comme régressions de la période sans preuve d'une nouvelle introduction.

### Étape 8 — modèles de mouvements et historique de stock

- Comparé l'entité et l'enum de la baseline du 12 août à leur version courante : table `stock_mouvements` remplacée par `piece_mouvements` et ancienne valeur `SORTIE` retirée lors du refactor du 3 septembre (`acc6b32`).
- Vérifié le repository courant : toutes les lectures d'historique interrogent désormais `piece_mouvements`.
- Recherché les mécanismes de migration courants : aucun Flyway/Liquibase ou transfert `stock_mouvements` vers `piece_mouvements` n'est présent. L'historique révèle un `PieceMouvementTableMigrator` éphémère, supprimé avant l'état courant; ses conditions et sa suppression sont détaillées en OAS-024. Le seed démo ne migre pas les lignes préexistantes.
- Inspecté les opérations actuelles `ENTREE`, transfert magasin→atelier, ajustement et inventaire : chaque chemin avec modification réécrit `qteReelle = stockMagasin + stockAtelier`; le comptage sans écart ne modifie rien par conception. Pas d'autre régression nouvelle prouvée dans ces méthodes au cours de cet échantillon; poursuivre sur le CRUD pièce, l'archivage et les consommateurs transverses.

### OAS-025 — Réécriture de stock par l'édition catalogue et désynchronisation de `qteReelle`

**Régression introduite le 26 août; le contrat documenté et le comportement effectif du CRUD divergent.**

Le contrôleur documente explicitement que `PUT /api/pieces-detachees/{id}` ne modifie pas les stocks. Pourtant, `PieceDetacheServiceImpl.update()` accepte `request.stockMagasin` pour une PDP et l'écrit directement (`6bdd743`, 26 août). L'écran d'édition reprend le stock affiché dans le formulaire (`openEdit`) et l'envoie dans le payload pour une PDP; seul le cas PDG le supprime. Cela permet à une simple édition de désignation/prix de réécrire un stock ancien capturé à l'ouverture, notamment si une réception ou un autre mouvement a eu lieu entre-temps.

Cette mise à jour ne passe ni par `StockServiceImpl` ni par la création d'un `PieceMouvement`. Elle ne recalcule pas non plus `qteReelle`; le callback `PDP.calculateQteReelle()` ne la calcule que si la valeur est `null`. Ainsi, l'édition peut écraser le stock magasin sans trace d'audit et laisser le total réel incohérent avec `stockMagasin + stockAtelier`. Le endpoint d'ajustement de stock existe déjà pour effectuer une correction traçable.

**Portée :** perte silencieuse possible de mouvements de stock intervenus depuis le chargement de la fiche, historique incomplet et total réel divergent. L'incidence dépend des mises à jour catalogue réellement effectuées et de la présence d'un mouvement concurrent; elle est reproductible par le chemin de code.

**Fichiers et changements :**

- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/service/PieceDetacheServiceImpl.java` (`6bdd743`, affectation du stock dans `update`).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/controller/PieceDetacheController.java` (contrat du `PUT`).
- `oas-front/src/app/agent/pieces-detachees/pieces-detachees.component.ts` (`openEdit` et `save`).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/data/entity/PDP.java` (recalcul conditionnel uniquement lorsque `qteReelle` est `null`).

### OAS-026 — Refactor du schéma des pièces sans migration des identités et catégories existantes

**Risque de rupture de données confirmé dans le dépôt; effet conditionnel à une base antérieure au refactor.**

La baseline du 12 août mappe `numeroDeSerie` sur `numero_serie`, `reference` sur une colonne `reference` distincte et `categorie` comme texte obligatoire. Le modèle actuel, introduit lors du refactor du 26 août puis réorganisé le 2 septembre, mappe `reference` sur `numero_serie`, `legacyReference` sur l'ancienne colonne `reference`, et remplace la catégorie texte par une relation vers `categorie_id`. Il ajoute aussi une colonne `designation` obligatoire. Le seed actuel connaît ces nouveaux champs, mais il s'agit d'un jeu de démonstration, pas d'une migration des lignes existantes. La recherche dans les migrations/scripts du dépôt n'a trouvé aucun transfert des anciennes valeurs de numéro de série, référence et catégorie vers les nouveaux champs/clé étrangère.

Les DTO et filtres courants lisent désormais `p.categorie` comme relation; si un déploiement conserve les lignes d'avant le changement sans conversion, `categorie_id` peut rester nul et l'ancienne valeur textuelle ne nourrit plus les listes/filtrages par catégorie. Le déplacement des anciennes valeurs de `numero_serie` et `reference` vers des propriétés inversées peut également rendre les références affichées ou uniques différentes, voire empêcher l'évolution automatique du schéma si les contraintes uniques et `nullable` ne peuvent pas être satisfaites par les anciennes données.

**Portée :** risque de perte d'information fonctionnelle ou d'échec de mise à jour du schéma pour une base ayant déjà des pièces. La base réelle n'a pas été inspectée; je ne conclus donc pas que des lignes de production ont été perdues. Une migration explicite et idempotente doit être vérifiée avant de considérer le refactor sûr pour une base existante.

**Fichiers et changements :**

- Baseline `oas-back/src/main/java/sn/oas/facturation/piecedetache/data/entity/PieceDetache.java` (12 août) et modèle courant `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/data/entity/PieceDetache.java` (refactor du 26 août, réorganisation du 2 septembre).
- `oas-back/src/main/resources/seed_all_entities.sql` (insertion avec le nouveau schéma uniquement).
- `oas-back/src/main/java/sn/oas/facturation/features/piecedetache/dto/PieceDetacheListResponse.java` et repository pièces (lecture/filtrage de la catégorie liée).

### Étape 9 — CRUD des pièces et évolution du modèle catalogue

- Comparé l'entité de la baseline du 12 août à celle du modèle courant et retracé les changements de colonnes/associations dans l'historique Git (OAS-026).
- Vérifié l'absence de script de conversion dans les ressources et configurations du dépôt; le seed ne constitue pas une migration des données déjà stockées.
- Suivi le formulaire d'édition jusqu'au `PUT` et au service, confronté à la documentation du contrôleur; le stock PDP est renvoyé même si le champ n'est pas éditable à l'écran, puis accepté par le service sans mouvement ni recalcul du total (OAS-025).
- L'édition d'une pièce ne propose pas de restauration d'une pièce supprimée; ce comportement seul ne prouve pas la perte de la fonction « restaurer », car la route de restauration vise les pièces archivées (statut métier) non supprimées. Pas de nouveau constat classé sur ce point.

### Étape 10 — fournisseurs et rattachement aux bons de commande

- Parcouru le flux catalogue agent → API → service → repository et l'affectation depuis création, édition et action dédiée du bon de commande.
- Confirmé que la recherche paginée filtre les fournisseurs archivés, mais que la liste sans mot-clé les inclut. L'écran fournisseur sait afficher/désarchiver les fournisseurs archivés; je ne classe donc pas cette différence isolément comme régression.
- Constaté que la création, la modification et `assignerFournisseur` d'un bon acceptent un fournisseur archivé; le sélecteur agent de BDC ne charge plus de fournisseurs (le `forkJoin` est actuellement vide/commenté dans le composant). Ce contrat est incohérent, mais l'historique ne permet pas ici d'établir que le comportement a été perdu pendant la période plutôt que laissé incomplet; conservé comme piste et pas comme régression datée.
- Reconfirmé le constat déjà enregistré OAS-012 : `/api/fournisseurs/**` est dans `permitAll`, couvrant GET, POST, PUT, DELETE et archivage. Les endpoints par ID retournent directement l'entité et le service utilise `findById`/`findAll` sans filtre garage explicite; dans un contexte multi-garage, c'est un problème d'autorisation/exposition, sans nouvelle entrée en doublon.
- Comparé l'affectation fournisseur au BDC à la version historique : la garde de statut `EN_ATTENTE` est toujours présente sur l'action dédiée. Pas de régression métier distincte confirmée dans ce sous-flux.

### OAS-027 — Les routes génériques de rendez-vous donnent des droits d'administration à tout compte authentifié

**Défaut d'autorisation introduit le 11 septembre (`f9a4cc2a`).**

`POST /api/rendez-vous` porte seulement `@PreAuthorize("isAuthenticated()")`. Le contrôleur traite un compte `ROLE_CLIENT` comme client, mais dirige toute autre identité authentifiée vers `createRendezVousByAdmin(request)`. Ce service accepte un `clientId` et un `vehiculeId`, puis crée le rendez-vous et notifie le client concerné. Les comptes technicien et autres rôles authentifiés peuvent donc appeler ce chemin, alors qu'une route `/api/rendez-vous/admin` distincte vérifie explicitement les rôles de backoffice.

`GET /api/rendez-vous/{id}`, ajouté le 2 septembre, exige lui aussi seulement une authentification et ne vérifie ni propriété client, ni rôle agent. La réponse expose le nom du client, son immatriculation, la date, le motif et le commentaire. Un compte authentifié peut lire ces informations en connaissant l'identifiant.

**Impact :** création de rendez-vous au nom d'autrui par les comptes non clients et lecture des coordonnées opérationnelles d'un rendez-vous par tout rôle authentifié. Constat établi sur les gardes et appels contrôleur/service; aucune requête vers une instance active n'a été exécutée.

**Fichiers :**

- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/controller/RendezVousController.java` (POST et GET par ID).
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/service/RendezVousServiceImpl.java` (`createRendezVousByAdmin`).
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/dto/RendezVousResponse.java` (champs renvoyés).
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java` (`/api/technicien/**` autorise les techniciens et le repli général exige seulement l'authentification).

### Étape 11 — portail client/technicien, rendez-vous et fiches atelier

- Suivi le BFF client : l'historique véhicule vérifie l'appartenance du véhicule au client connecté; les autres lectures client interrogent les véhicules/OR à partir de l'ID du client extrait de la session. Pas de contournement d'ID client repéré dans ces méthodes.
- Vérifié le portail technicien : détail et écritures OR vérifient l'assignation du technicien; les mutations passent aussi par une vérification de statut `DIAGNOSTIC`. Ces contrôles existent, même si la restriction de toutes les mutations à ce statut peut mériter validation métier distincte.
- Parcouru les routes de rendez-vous : confirmé OAS-027 pour la création par rôle et la lecture par ID. Les routes d'annulation client vérifient la propriété; les routes de mise à jour backoffice déclarent des rôles.
- Comparé `validerRendezVous()` à son implémentation pré-août : la confirmation sans création de fiche atelier est déjà présente dans le code antérieur (depuis juin), malgré le résumé de l'annotation controller. Ce décalage de contrat est antérieur à la période auditée et n'est pas classé comme régression d'août à aujourd'hui.
- Revisité le flux fiche atelier → OR couvert par OAS-002/OAS-011; pas de constat additionnel retenu dans cet échantillon sans comparer les diffs UI préexistants qui sont actuellement modifiés localement.
- Vérifié les flux de connexion, émission/rafraîchissement/suppression des cookies, inscription et gardes Angular. Pas d'autre régression fonctionnelle datée retenue dans ce passage; les cookies d'authentification sont configurés `secure(false)` dans le backend, risque de transport à traiter selon le protocole réellement servi. La base de déploiement n'a pas été vérifiée.

### OAS-028 — Les contrôles de configuration de fiche atelier ont été supprimés puis restaurés

**Suppression historique confirmée puis réparée le 14 septembre (`25f6413`).**

Le commit `b776b01` supprime simultanément `FicheAtelierConfigController`, l'entité `FicheAtelierConfig` et `FicheAtelierConfigRepository`. Le commit `25f6413`, dont le message indique explicitement que la configuration avait été « supprimée par mégarde », les réintroduit. La suppression a temporairement retiré la route et le modèle de persistance des paramètres de fiche atelier; ils sont présents dans l'état courant. Je classe ceci comme une régression historique corrigée, pas comme une panne actuelle.

**Fichiers supprimés puis restaurés :** contrôleur, entité et repository du module fiche atelier config (déplacement final vers `features/ficheAtelierConfig/`).

**Vérification supplémentaire du commit de suppression :** `StockGarage` et son repository ont également été supprimés dans `b776b01`, mais la recherche à la révision parente montre seulement une entité/repository inutilisés, sans chemin de lecture/écriture des quantités. Aucun comportement stock actif supprimé n'est donc confirmé à partir de cette seule suppression.

### OAS-029 — Le runner de conversion du schéma legacy a disparu sans remplacement ciblé

**Risque de mise à niveau de base confirmé dans l'historique; impact conditionnel aux bases déjà créées.**

Le commit de réorganisation `69a1d14` supprime `FixDatabaseConstraintsRunner` (et l'ancien `DatabaseMigration`). Le runner exécutait au démarrage plusieurs opérations destinées à faire évoluer les bases existantes : renommage de `bons_de_livraison` en `bons_de_reception`, retrait de contraintes CHECK pour les statuts/types, copie des anciennes valeurs `pieces_detachees.prix` vers `prix_unitaire` et adaptation de la contrainte de catégorie. Les entités et modules courants utilisent maintenant `BonDeReception`, des enums élargis et `prixUnitaire`. Je n'ai trouvé aucun remplacement versionné par migration SQL/Flyway/Liquibase pour ces conversions dans la version courante.

Sur une base qui n'avait pas exécuté l'ancien runner, l'application peut créer de nouvelles tables/colonnes sans reprendre les anciennes données, ou conserver des contraintes CHECK incompatibles avec les valeurs courantes. Une partie de l'ancien runner était risquée : il effectuait aussi des `DELETE` automatiques sur des tables de liaison de mécaniciens et avalait plusieurs exceptions. Sa suppression est donc recensée comme régression potentielle de mise à niveau, mais ce runner ne doit pas être restauré tel quel; il faut concevoir des migrations explicites, ciblées et vérifiables. Le contenu réel des bases n'a pas été inspecté.

**Fichiers et changements :**

- `oas-back/src/main/java/sn/oas/facturation/FixDatabaseConstraintsRunner.java` (renommage de table, prix, contraintes et suppressions de lignes; retiré au refactor du 2 septembre).
- `oas-back/src/main/java/sn/oas/facturation/config/DatabaseMigration.java` (retrait des trois suppressions de contraintes de statut).
- Modèle courant `features/bonDeReception`, enum Statuts OR/facturation et champs de pièce (`prixUnitaire`).

### Étape 12 — inventaire Git des suppressions

- Passé la liste Git des fichiers supprimés d'août à octobre dans les deux dépôts, puis rapproché les suppressions des chemins réintroduits/renommés.
- FicheAtelierConfig a été supprimée puis rétablie (OAS-028); le migrateur de mouvements supprimé est ajouté à la preuve OAS-024; les suppressions `StockGarage` ne touchent qu'une entité/repository non utilisés.
- Le runner legacy retiré par la réorganisation portait des conversions de données nécessaires pour certaines bases préexistantes, mais contenait aussi des suppressions automatiques de lignes. Le risque de conversion non remplacée est noté en OAS-029; les bases actives restent à inspecter avant toute conclusion sur leurs données.
- Les routes et écrans BonDeLivraison supprimés ont une suite courante dans le module BonDeReception et le popup de réception des BDC; l'ancien service interdisait déjà création/modification manuelles. Pas de preuve de rupture fonctionnelle distincte dans cette suppression.
- Les autres suppressions examinées dans cette passe sont des déplacements/renommages ou des tests/styles, sans perte métier démontrée à ce stade.

### OAS-030 — Le renouvellement de session ne peut pas reprendre après expiration dans le déploiement cross-site

**Régression de reprise de session introduite par le renouvellement cookie (frontend 6 septembre, backend 2 septembre).**

Le frontend de production est configuré sur `https://oas-front.vercel.app` et l'API sur `https://oas-back.onrender.com`, soit deux sites distincts. Le backend émet son cookie `refreshToken` avec `SameSite=Lax`. Le standard de cookies précise que `Lax` ne permet le cookie en contexte cross-site que lors de certaines navigations de premier niveau; les appels `fetch`/XHR POST cross-site ne le transportent pas. Le client appelle `POST /api/auth/refresh` avec `{}` et ne conserve ni n'envoie `response.refreshToken` dans le corps ou un en-tête. Le cookie HttpOnly de refresh est donc l'unique refresh token disponible côté navigateur, mais il est absent de ces appels cross-site.

Le fonctionnement nominal est partiellement masqué par un fallback : le refresh est programmé une minute avant expiration et `AuthController.extractRefreshToken()` accepte aussi le bearer access token reçu dans `Authorization`; tant qu'il n'est pas expiré, `AuthServiceImpl.refreshToken()` valide ce JWT et génère les nouveaux tokens. Si le navigateur suspend/throttle le timer ou revient après expiration, le refresh cookie reste absent et le bearer access token est expiré; le backend répond « Refresh token invalide ou expiré ». Le handler du 401 termine alors par un logout. Le même échec existe à la reprise via le timer immédiat si le token est déjà expiré.

**Impact :** une session inactive ou suspendue au-delà de l'expiration d'accès peut perdre la possibilité de renouvellement et obliger l'utilisateur à se reconnecter, malgré le refresh token annoncé à sept jours. C'est un défaut reproductible à partir de la configuration et des branches de code; aucune session réelle n'a été manipulée. La règle de livraison SameSite est documentée dans le [draft IETF RFC 6265bis](https://datatracker.ietf.org/doc/draft-ietf-httpbis-rfc6265bis/).

**Fichiers et changements :**

- `oas-front/src/environments/environment.prod.ts` (origine frontend Vercel).
- `oas-back/src/main/resources/application-prod.yml` (CORS autorise cette origine) et `features/auth/controller/AuthController.java` (`SameSite=Lax`, extraction du refresh).
- `oas-front/src/app/core/services/auth.service.ts` (requête refresh avec corps vide, timer et aucun stockage de `refreshToken`).
- `oas-front/src/app/core/interceptors/auth.interceptor.ts` (retry sur 401 puis logout si le renouvellement échoue).
- Commits `69a1d14`/`75aa25d` (base cookie/session) et `0710d4b` (renouvellement automatique et retry 401).

### OAS-031 — Le déplacement des routes utilisateurs a contourné leur garde de rôle

**Régression d'autorisation introduite par le refactor du 3 septembre (`acc6b32`).**

Avant le refactor, les opérations de gestion des utilisateurs étaient exposées sous `/api/admin/users/**`, chemin explicitement protégé dans `WebSecurityConfig` pour `SUPER_AGENT` et `MASTER`. Le commit déplace le contrôleur vers `features/user` et change son préfixe en `/api/users`, mais ne met pas à jour le matcher de sécurité. La règle existante reste attachée à `/api/admin/users/**`; le chemin courant `/api/users/**` tombe donc dans `.anyRequest().authenticated()`.

Le contrôleur courant expose sur ce préfixe la liste paginée et complète, la lecture par ID, la création, la modification, l'archivage/restauration, l'activation/désactivation et la suppression. En conséquence, tout utilisateur authentifié autorisé par le filtre général — dont un client ou technicien — peut appeler ces opérations administratives. Les méthodes `/me` sont légitimement utilisateur-courant, mais les autres routes ne déclarent pas de `@PreAuthorize`.

**Impact :** un compte authentifié non administrateur peut lire les utilisateurs et déclencher des mutations de comptes. Le code service confirme que `toggleUserStatus`, `archiveUser`, `updateUser` et `deleteUser` agissent sur l'ID fourni sans contrôle de rôle local. Constat statique; aucune action n'a été exécutée sur une instance.

**Fichiers et preuve :**

- `oas-back/src/main/java/sn/oas/facturation/features/user/controller/UserController.java` (préfixe `/api/users` et routes d'administration sans garde méthode).
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java` (garde maintenue sur l'ancien `/api/admin/users/**`; repli authentifié pour le reste).
- Commit `acc6b32` du 3 septembre : déplacement du contrôleur et changement de route; comparaison avec la règle présente avant et après.
- `features/user/service/UserServiceImpl.java` (mutations par ID sans contrôle d'autorisation propre au rôle).

### OAS-032 — L'inscription publique peut créer un compte agent avec un rôle privilégié

**Risque critique introduit dans le module d'authentification ajouté le 2 septembre (`69a1d14`), toujours présent.**

`/api/auth/**` est explicitement en `permitAll`. Dans `AuthController.signup`, le seul type refusé explicitement est `TECHNICIEN`; les types `CLIENT` et `AGENT` sont transmis à `AuthService.register()`. Le chemin `TypeUser.AGENT` construit directement un `Agent` avec `role=request.role()` et le `garageId` fourni, sans vérifier l'identité de l'appelant ni autoriser le rôle demandé. L'enum `Role` inclut `SUPER_AGENT` et `MASTER`.

Ainsi, la route publique d'inscription ne garantit pas « client seulement » : un appelant anonyme peut soumettre `type=AGENT` et un rôle privilégié pour tenter de créer un compte de backoffice. L'inscription d'un agent sans garage fourni peut aussi créer l'entité avec `garage=null`; l'absence de validation de rôle est toutefois le risque principal. Le garde-fou réservé aux techniciens montre que le code sait distinguer ce flux, mais aucune protection équivalente n'existe pour `AGENT`.

**Impact :** création potentielle de comptes privilégiés sans authentification, selon les contraintes métier/données supplémentaires non observées dans le code. Aucune requête n'a été envoyée à un environnement actif. La présence de contrôles d'unicité/prévalidation dans le reste du service ne constitue pas un contrôle d'autorisation.

**Fichiers et preuve :**

- `features/auth/controller/AuthController.java` (`signup`, refus `TECHNICIEN` seulement).
- `security/WebSecurityConfig.java` (`/api/auth/**` dans `permitAll`).
- `features/auth/service/AuthServiceImpl.java` (branche `TypeUser.AGENT`, rôle fourni par la requête, sans appelant autorisé requis).
- `features/auth/dto/request/RegisterRequest.java` (`type`, `role`, `garageId` fournis par l'appelant).
- Commit `69a1d14` du 2 septembre qui ajoute le contrôleur et le flux public; le garde explicite du type technicien est ajouté ensuite, sans fermeture du chemin `AGENT`.

### OAS-033 — Les endpoints génériques de diagnostic contournent le contrôle d'assignation technicien

**Défaut d'autorisation introduit avec le module diagnostic le 18 septembre (`da64090`).**

`/api/diagnostics/**` n'a pas de matcher de rôle dédié dans `WebSecurityConfig` et relève donc de `.anyRequest().authenticated()`. Le contrôleur n'ajoute aucun `@PreAuthorize`. Le service générique `DiagnosticServiceImpl` lit, crée, modifie, change le statut, supprime diagnostics, pièces jointes et remarques uniquement à partir des IDs reçus; ces chemins ne vérifient ni le rôle appelant, ni l'assignation du technicien à l'ordre, ni le garage de la ressource. Le portail technicien, lui, a des contrôles d'assignation relevés à l'étape 11, mais ils ne protègent pas les routes `/api/diagnostics/**`.

Un client, technicien non assigné ou autre compte authentifié peut donc appeler les endpoints génériques par ID et lire ou modifier les diagnostics d'autres ordres. Les opérations incluent également les suppressions par ID de pièces jointes et de remarques.

**Impact :** exposition et altération de diagnostics hors périmètre utilisateur; les contrôles du portail dédié sont contournables via le contrôleur générique. Constat établi en lecture du code, sans appel à une instance.

**Fichiers et preuve :**

- `features/diagnostic/controller/DiagnosticController.java` (toutes les routes sans garde de rôle).
- `features/diagnostic/service/DiagnosticServiceImpl.java` (recherche/mutations par ID sans vérification d'appelant ou d'assignation).
- `security/WebSecurityConfig.java` (aucun matcher diagnostic; repli authentifié).
- Commit `da64090` du 18 septembre (introduction du module diagnostic générique).

### OAS-034 — Les actions de gestion des devis prévisionnels sont accessibles à tout compte authentifié

**Défaut d'autorisation introduit avec le nouveau module de devis du 30 septembre (`a5fd294`).**

`/api/devis-previsionnels/**` n'a pas de matcher de rôle dédié et ses routes ne portent pas de garde `@PreAuthorize`; la règle générale exige seulement une authentification. Les routes client dédiées `/me`, `/client-accepter` et `/client-refuser` résolvent le client connecté et les deux dernières vérifient bien la propriété du devis. Cette protection ne couvre pas les routes génériques : `GET`/recherche et lecture par ID exposent les données d'autres clients; `PUT /{id}`, suppression, validation et annulation agissent sur l'ID fourni sans contrôle d'appelant. La création seule appelle `getAgentConnecte()` dans le service; cette vérification ne protège pas les autres opérations.

**Impact :** un client ou technicien authentifié peut lire des devis hors de son compte et appeler les mutations de devis prévues pour le backoffice, dont accepter/annuler/supprimer, par les endpoints génériques. Les contrôles de propriété sur le sous-ensemble client sont positifs mais incomplets pour la surface entière.

**Fichiers et preuve :**

- `features/devisPrevisionnel/controller/DevisPrevisionnelController.java` (routes génériques et routes client sur le même contrôleur sans garde de rôle).
- `features/devisPrevisionnel/service/DevisPrevisionnelServiceImpl.java` (seule l'acceptation/refus client compare le propriétaire; les opérations génériques chargent par ID).
- `security/WebSecurityConfig.java` (aucun matcher devis; repli authentifié).
- Commit `a5fd294` du 30 septembre (module de devis prévisionnel).

### OAS-035 — Risque hérité : les routes génériques de gestion client restent accessibles aux comptes authentifiés

**Risque actuel confirmé, mais pas classé comme régression de la période : mêmes routes et même absence de garde avant août.**

Le contrôleur `/api/clients/**` existait déjà sous ce préfixe dans le code antérieur à août (ancien package `sn.oas.facturation.client.controller`). La règle générique `.anyRequest().authenticated()` et l'absence de garde locale ne sont donc pas des changements introduits dans la période auditée. Le refactor du 2 septembre déplace le module et remplace plusieurs réponses d'entités par des DTO; je n'ai pas trouvé de preuve qu'il a créé l'accès transversal qui existait déjà.

Le risque reste notable dans l'état courant : clients et techniciens authentifiés atteignent les listes, lectures et mutations génériques; les services chargent les clients par ID sans contrôle local de rôle/propriétaire. Je le conserve comme constat de contexte à ne pas compter parmi les régressions datées d'août à aujourd'hui. Aucun appel à une instance n'a été fait.

**Fichiers comparés :** `features/client/controller/ClientController.java`, `features/client/service/ClientServiceImpl.java`, `security/WebSecurityConfig.java`; baseline du 17 mai sous `client/controller/ClientController.java`, comparaison avec le refactor du 2 septembre (`69a1d14`).

### OAS-036 — Risque hérité : les routes génériques véhicules contournent les contrôles de propriétaire des routes `/me`

**Risque actuel confirmé, mais les routes génériques concernées précèdent août.**

`/api/vehicules/**` existait déjà en mai avec les opérations génériques de liste, lecture, création, modification, suppression et consultation par client, sous `.anyRequest().authenticated()`. Le service charge les ressources par ID et la mise à jour permet de changer le client associé. Ces faits sont sérieux, mais l'historique consulté ne permet pas de les présenter comme une régression introduite depuis août.

Changement positif relevé le 18 septembre (`a9cd202`) : ajout de `DELETE /me/{id}` qui appelle `archiveVehiculeByClient(id, clientId)`; le service vérifie bien l'appartenance avant archivage. La route `/me` liste aussi désormais les véhicules actifs. Ces protections n'ont toutefois pas retiré ou restreint les anciennes routes génériques. Je conserve donc OAS-036 comme risque hérité/hors période, sans le compter comme régression datée.

**Fichiers comparés :** `features/vehicule/controller/VehiculeController.java`, `features/vehicule/service/VehiculeServiceImpl.java`, `security/WebSecurityConfig.java`; baseline du 17 mai et changement client du 18 septembre (`a9cd202`).

### OAS-037 — Tout compte authentifié peut publier/envoyer un proforma au client

**Défaut d'autorisation introduit par le nouvel endpoint du 17 août (`1627c67`).**

Le commit introduit `POST /api/proformas/{id}/valider-envoi`, dont le contrat décrit une validation des prix par le chef d'atelier, puis la publication du devis pour le client. Aucun matcher `/api/proformas/**` n'existe dans `WebSecurityConfig`; la route tombe sur `.anyRequest().authenticated()`. Le contrôleur n'a ni `@PreAuthorize` ni contrôle du rôle, et `ProformaServiceImpl.validerEnvoi()` ne vérifie pas l'identité de l'appelant.

Le service rend le proforma visible, le sauvegarde et envoie son PDF à l'adresse du client liée à l'ordre. Il ne vérifie pas non plus s'il est déjà visible/envoyé : des appels répétés peuvent réexpédier plusieurs courriels. Un client ou technicien connecté peut donc déclencher cette étape staff sur un proforma choisi par ID, avant l'approbation métier attendue, et répéter l'envoi.

**Impact :** contournement de l'approbation atelier et envoi prématuré/répété de documents commerciaux. L'impact de l'envoi trop tôt à la création existe aussi et est décrit en OAS-021; OAS-037 porte spécifiquement sur la nouvelle route de publication à distance et son absence de garde. Aucune requête n'a été envoyée à une instance.

**Fichiers et preuve :**

- `features/proforma/controller/ProformaController.java` (`POST /{id}/valider-envoi`, sans garde).
- `features/proforma/service/ProformaServiceImpl.java` (`validerEnvoi`, rend visible et envoie l'email sans vérifier le rôle ni l'état précédent).
- `security/WebSecurityConfig.java` (pas de règle proforma dédiée; repli authentifié).
- Commit `1627c67` du 17 août : ajout de l'endpoint et du service de publication.

### OAS-038 — La liste des conversations réservée aux agents est lisible par tout compte connecté

**Défaut d'autorisation introduit avec le contrôleur de messagerie du 2 septembre (`e4cd464`).**

`GET /api/messages/conversations` (alias `/clients`) est décrit dans le contrôleur comme une liste de conversations « Agent », mais il appelle directement `messageService.getActiveConversations()` sans vérifier l'identité agent. Aucune règle dédiée à `/api/messages/**` n'existe dans `WebSecurityConfig`; le fallback n'exige que l'authentification. Le service récupère tous les clients ayant des messages et retourne notamment leur nom, téléphone, dernier message et compteur de messages non lus.

Les routes de lecture d'une conversation donnée et de réponse (`/clients/{clientId}`) passent bien par `authService.getAgentConnecte()`; la fuite vient précisément de l'endpoint global qui n'applique pas cette vérification. Un client ou technicien connecté peut donc lister les conversations de tous les clients.

**Impact :** exposition interclients de coordonnées et de contenus de messagerie privée. Aucune requête réelle n'a été envoyée.

**Fichiers et preuve :**

- `features/messagerie/controller/MessageController.java` (`getActiveConversations`, aucune résolution ou garde d'agent).
- `features/messagerie/service/MessageServiceImpl.java` (`getActiveConversations`, agrège les conversations de tous les clients).
- `features/messagerie/dto/ClientConversationResponse.java` (nom, téléphone, aperçu du dernier message, compteur).
- `security/WebSecurityConfig.java` (pas de matcher messagerie; repli authentifié).
- Commit `e4cd464` du 2 septembre qui introduit cet endpoint dans le contrôleur réorganisé.

### OAS-039 — Une valeur de filtre `type` invalide est ignorée et transforme la recherche en liste générale

**Régression de contrat introduite par le changement de filtre du 29 septembre (`23650a6`).**

Avant ce commit, le contrôleur recevait directement `TypePiece` comme paramètre Spring. Une valeur inconnue échouait donc lors de la conversion de requête, au lieu d'exécuter la recherche sans filtre. Le changement remplace ce type par `String`, tente `TypePiece.valueOf(...)`, puis avale `IllegalArgumentException` sans erreur ni indication et appelle `getPieces()` avec `parsedType == null`.

Un appel tel que `?type=valeur-inconnue` reçoit alors les pièces non filtrées (ou les résultats des autres filtres) au lieu d'un 400. Si la valeur vient d'un filtre UI, d'une ancienne valeur persistée ou d'une faute de frappe, la page peut présenter des catégories inattendues et la requête élargit son résultat sans avertissement. Le même parsing silencieux est appliqué au nouveau paramètre `statut`; lui constitue une validation insuffisante de la nouvelle option plutôt qu'une perte d'un ancien comportement.

**Impact :** contrat de recherche moins strict et résultats plus larges en cas de valeur invalide; gravité fonctionnelle faible, mais risque de confusion et de chargement inutile de données. Aucun appel à une instance n'a été exécuté.

**Fichiers et preuve :**

- `features/piecedetache/controller/PieceDetacheController.java` (conversion manuelle qui ignore les enums inconnus).
- `features/piecedetache/service/PieceDetacheServiceImpl.java` (valeur `null` signifie absence du filtre et recherche plus large).
- Commit `23650a6` du 29 septembre; comparaison avec le paramètre `TypePiece` strict de la version précédente.

### OAS-040 — Le cache de profil client survit à la déconnexion et peut faire modifier le profil du compte précédent

**Régression d'isolation de session introduite par le cache frontend du 18 septembre (`d5d1a43`).**

`ClientPortalService.getMe()` transforme désormais la réponse en observable racine `shareReplay(1)`. `clearMeCache()` est appelé après une sauvegarde de profil, mais la déconnexion courante (`AuthService.logout()` puis navigation Angular vers `/login`) ne vide pas ce cache, et aucun autre appel à `clearMeCache()` n'existe.

Dans une même instance SPA, si le compte A est connecté puis se déconnecte et le compte B se connecte sans rechargement complet, le service peut renvoyer à B le profil de A. `ClientProfileComponent.ngOnInit()` stocke alors `me.id` dans `clientId`; `saveProfile()` transmet cet ID à `ClientProfileService.updateProfile()`, qui appelle `PUT /api/clients/{id}`. Le backend actuel met à jour par ID sans contrôle de propriété locale (risque hérité séparément noté OAS-035). Le cache introduit donc un chemin frontend concret qui peut faire qu'un second utilisateur soumette une modification sur l'ID du précédent compte, au lieu de son propre profil.

**Condition et impact :** il faut deux sessions client successives dans le même runtime Angular sans rafraîchissement de page, puis ouvrir/enregistrer le profil du second compte. Le comportement est déduit des flux de cache, de logout et d'écriture; aucune session réelle n'a été manipulée. En plus d'afficher les données de A à B, cela peut modifier les nom/prénom de A avec les valeurs soumises par B. La correction backend d'OAS-035 reste nécessaire pour fermer la frontière, mais OAS-040 date le nouveau déclencheur de cache.

### OAS-041 — Une erreur de transition proforma fait quand même avancer l'écran de réparation

**Période / fichier :** ajoutée le 18 septembre 2026 dans le commit `0c54129` (`feat: add repair order workflow steps...`), `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-proforma/step-proforma.component.ts`, méthode `passerEtapeSuivante()`.

**Changement observé :** la nouvelle méthode appelle `updateStatut()` pour passer à `BON_DE_COMMANDE` ou `BON_DE_SORTIE`. Dans son callback `error`, elle ignore l'erreur reçue et navigue tout de même vers l'approvisionnement ou le bon de sortie. Le commentaire suppose que l'échec signifie que le statut est déjà synchronisé, sans vérifier la réponse ni relire l'ordre. Avant ce commit, `validerProforma()` affichait le message backend et restait à l'étape courante sur tout échec.

**Conséquence :** une panne réseau/serveur, un refus métier ou une erreur de persistance peut laisser le statut backend à la proforma alors que l'interface affiche l'étape suivante. L'utilisateur peut alors rencontrer des opérations qui échouent ou revenir à l'étape précédente après rechargement. C'est une divergence UI/API nouvellement introduite, distincte des problèmes de contrôle d'accès relevés ailleurs.

**Preuve / limite :** le comportement est certain à la lecture du callback Angular; aucune erreur réelle n'a été provoquée et aucun état de production n'a été inspecté. La validité des valeurs de statuts ne supprime pas le cas général d'erreur de la requête.

### OAS-042 — La validation du proforma place systématiquement l'ordre en approvisionnement

**Période / fichiers :** branche frontend ajoutée le 18 septembre 2026 par `0c54129`, `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-proforma/step-proforma.component.ts` (`validerProforma()`), en interaction avec `oas-back/src/main/java/sn/oas/facturation/features/proforma/service/ProformaServiceImpl.java` (`valider()`).

**Changement observé :** avant `0c54129`, la validation de cette étape choisissait directement le prochain statut en fonction du stock calculé : attente de commande en cas de rupture, attente de sortie sinon. Après le commit, le bouton « Valider le proforma » appelle `PUT /api/proformas/{id}/valider`. Le service backend marque le proforma accepté et fixe inconditionnellement l'ordre à `BON_DE_COMMANDE`. Le calcul conditionnel du frontend n'intervient que plus tard, quand l'utilisateur déclenche séparément le passage à l'étape suivante.

**Conséquence :** entre ces deux actions, l'état persisté classe comme « approvisionnement » aussi les ordres sans rupture de stock. Si l'utilisateur recharge/quitte l'écran après l'acceptation, l'auto-redirection du détail lit `BON_DE_COMMANDE` et rouvre `approvisionnement`, au lieu du bon de sortie prévu par le calcul de stock. Ce changement introduit une transition intermédiaire incompatible avec le routage fondé sur l'état backend et peut imposer une étape d'approvisionnement vide.

**Preuve / limite :** le callback backend et l'auto-redirection frontend sont confirmés par lecture statique et comparaison Git. Le scénario suppose une interruption/recharge entre validation du proforma et clic « Suivant »; il n'a pas été reproduit en session réelle. Aucun contrôle de stock backend dans `ProformaServiceImpl.valider()` ne conditionne l'affectation `BON_DE_COMMANDE`.

### OAS-043 — « Valider le diagnostic » avance l'ordre sans valider le diagnostic persistant

**Période / fichiers :** contrat introduit avec les modules diagnostic et OR du 18 septembre 2026 (backend `da64090` et `14869d7`, frontend `0c54129`), `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-diagnostic/step-diagnostic.component.ts`, `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/dto/steps/DiagnosticStepDto.java` et `.../features/diagnostic/service/DiagnosticServiceImpl.java`.

**Changement observé :** la fonction frontend `marquerValide()` envoie `statut: 'VALIDE'` dans `POST /api/diagnostics/step`, puis place l'ordre à `PIECES_MO`. Or le DTO backend `DiagnosticStepDto` ne contient pas de propriété `statut`, et `saveFromStep()` ne modifie jamais le statut d'un diagnostic existant; lors de sa création, il initialise celui-ci à `EN_COURS`. La réponse persistée retourne donc le statut antérieur, alors que l'interface annonce la validation et débloque la suite.

**Conséquence :** l'ordre peut avancer aux pièces et main-d'œuvre tandis que son diagnostic reste `TERMINE` ou `EN_COURS` en base. Au retour sur l'étape diagnostic, le frontend privilégie `o.diagnostic.statut` lorsqu'il existe; il réaffiche donc le statut non validé plutôt que `VALIDE`. Les listes et autres consommateurs qui lisent le statut du diagnostic constatent eux aussi un état différent de celui de l'ordre.

**Preuve / limite :** le champ absent, l'absence d'affectation dans `saveFromStep()`, le statut initial `EN_COURS` et la priorité de lecture frontend sont vérifiés dans le code. Aucune donnée réelle n'a été modifiée; l'écart est un défaut de contrat frontend/backend introduit lors de l'ajout du flux.

### OAS-044 — Le détail OR ne renvoie plus les stocks des pièces et force le chemin d'approvisionnement

**Période / fichier :** projection `GET /api/ordres-reparation/{id}` introduite le 2 octobre 2026 par `24b7524`, `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/service/OrdreReparationServiceImpl.java` (`mapToResponseDTO`) et `OrdreReparationResponseDTO.PieceDto`.

**Changement observé :** avant ce commit, le contrôleur renvoyait l'entité OR et ses pièces avec les stocks. Le nouveau DTO déclare `stockMagasin` et `stockAtelier`, mais le mapper ne renseigne que id, référence, désignation, prix et type pour `PieceDto`. Les propriétés de stock arrivent donc absentes/nulles dans la réponse.

**Conséquence :** `StepProformaComponent` calcule `hasRupture` à partir de `piece.stockMagasin + piece.stockAtelier`, avec `0` comme valeur de repli; `StepApprovisionnementComponent` calcule de même les quantités manquantes et ne peut pas utiliser le stock magasin. Un ordre avec stock réel suffisant est traité comme une rupture : l'interface force le bon de commande/approvisionnement au lieu de permettre le chemin direct vers le bon de sortie. Le même défaut peut commander des pièces déjà en stock.

**Interaction avec OAS-042 :** avant ce nouveau DTO, OAS-042 rendait déjà le statut backend `BON_DE_COMMANDE` même sans rupture et pouvait rediriger vers l'approvisionnement lors d'un rechargement. Dans le contrat courant, OAS-044 force par ailleurs `hasRupture=true`; ce défaut de projection masque donc souvent la branche frontend « sans rupture », mais ne corrige pas l'affectation inconditionnelle du statut backend.

**Preuve / limite :** le mapper et les deux consommateurs Angular sont suivis statiquement. Aucun ordre ni stock réel n'a été modifié et aucun appel API n'a été fait.

### OAS-045 — La sauvegarde automatique du diagnostic envoie un courriel à chaque modification

**Période / fichiers :** ajout de l'envoi courriel le 2 octobre 2026 (`f0a09b8`), `oas-back/src/main/java/sn/oas/facturation/features/diagnostic/service/DiagnosticServiceImpl.java`, méthode `saveFromStep()`; sauvegarde automatique préexistante dans `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-diagnostic/step-diagnostic.component.ts`.

**Changement observé :** le commit ajoute, à la fin de `saveFromStep()`, la génération et l'envoi d'un PDF diagnostic au client à chaque appel. Le composant appelle ce même endpoint depuis `saveDiagnosticSilently()` après chaque clic sur une panne et après 600 ms de saisie libre. L'envoi est aussi exécuté lors de la création de l'enregistrement par l'étape.

**Conséquence :** tant que le client possède une adresse email, chaque modification automatique peut déclencher un nouvel email avec une copie intermédiaire du diagnostic. Le client peut recevoir plusieurs documents incomplets ou successifs pendant que le technicien continue son diagnostic; le mécanisme ne distingue pas brouillon, validation, changement effectif ou envoi déjà réalisé.

**Preuve / limite :** l'appel du frontend et l'effet d'email dans le service sont établis statiquement; aucun email n'a été envoyé pendant l'audit. La fréquence réelle dépend des modifications et de la présence d'une adresse client.

### OAS-046 — Le nouveau réglage « client fidèle » permet à tout compte connecté d'altérer les données financières d'un client

**Période / fichiers :** nouveau comportement du 2 octobre 2026 (`036c28e`), `ClientController` (`PATCH/DELETE /api/clients/{id}/fidele`), `ClientServiceImpl`, `ClientListResponse` et `WebSecurityConfig`.

**Changement observé :** le nouveau contrôleur expose la mise à jour du statut fidèle, remise, plafond, échéance, NINEA, RCCM et RIB par ID sans `@PreAuthorize` ni vérification du propriétaire dans le service. `WebSecurityConfig` ne définit pas de garde `/api/clients/**`; ces routes atteignent donc la règle générale `.anyRequest().authenticated()`. Le même commit ajoute ces champs à `ClientListResponse`, utilisé pour les listes/recherches et lectures individuelles génériques.

**Conséquence :** un compte client authentifié peut appeler les nouvelles routes sur l'ID d'un autre client pour activer/modifier son statut et ses paramètres financiers, ou les effacer; les réponses génériques peuvent aussi inclure les nouveaux champs financiers dans le périmètre d'accès déjà trop large. OAS-035 documente la faiblesse héritée des routes clients génériques; OAS-046 est une extension nouvelle et sensible de cette surface pendant la période.

**Preuve / limite :** contrôleur, service et règles de sécurité courants vérifiés; aucune requête n'a été envoyée. La sévérité repose sur le rôle client qui est authentifié mais n'a pas de contrôle de propriété pour ces routes.

### OAS-047 — Toute RuntimeException non traitée devient une erreur HTTP 400

**Période / fichier :** handler ajouté le 2 octobre 2026, `oas-back/src/main/java/sn/oas/facturation/shared/exception/GlobalExceptionHandler.java` (`handleRuntimeException`).

**Changement observé :** un handler global attrape toute `RuntimeException` et renvoie `400 Bad Request` avec son message brut. Avant cette méthode, les exceptions non prises en charge tombaient dans le handler générique `Exception` et renvoyaient `500 Internal Server Error`.

**Conséquence :** une panne applicative inattendue représentée par une runtime exception (par exemple une `NullPointerException` ou une défaillance d'invariant) est désormais présentée comme une requête client invalide. Les clients peuvent traiter à tort le défaut comme une erreur récupérable de saisie, et le message interne peut être exposé. Les handlers plus précis pour les types métier gardent la priorité, mais ne couvrent pas les erreurs imprévues.

**Preuve / limite :** changement de dispatch Spring et statuts HTTP vérifié dans le handler. Aucun endpoint n'a été forcé en erreur.

**Fichiers et preuve :**

- `oas-front/src/app/client/layout/client-portal.service.ts` (`shareReplay(1)` sur `getMe`; invalidation manuelle uniquement).
- `oas-front/src/app/client/layout/client-layout.component.ts` (déconnexion sans invalidation du cache, puis navigation sans rechargement).
- `oas-front/src/app/core/services/auth.service.ts` (suppression des cookies/session locale, aucun reset du cache portail).
- `oas-front/src/app/client/profile/client-profile.component.ts` (copie de `me.id` dans `clientId`, puis sauvegarde avec cet ID).
- `oas-front/src/app/client/profile/client-profile.service.ts` et `oas-back/.../features/client/controller/ClientController.java` (PUT par ID; autorisation absente déjà recensée en OAS-035).
- Commit `d5d1a43` du 18 septembre (mise en cache du profil partagé).

### Étape 13 — parcours d'authentification après expiration

- Vérifié le trajet connexion → cookies backend → stockage frontend → bearer → renouvellement programmé → reprise après 401.
- Comparé les origines de production configurées aux attributs de cookie; consigné OAS-030 pour les reprises après expiration/suspension.
- La revue du profil, des inscriptions et des autres endpoints utilisateur reste à croiser fichier par fichier avec les modifications de l'application et les changements des modules client/technicien.

### Étape 14 — gestion des utilisateurs et déplacement des frontières d'accès

- Comparé la garde historique `/api/admin/users/**` avec le nouveau préfixe `/api/users/**` introduit lors du déplacement du module le 3 septembre. Le matcher n'a pas suivi la route : consigné OAS-031.
- Suivi les endpoints du contrôleur vers les services : les opérations admin (liste, lecture par ID, création, modification, statut, archivage et suppression) n'ont pas de garde locale et les services mutent par ID fourni.
- Revu le DTO de liste : le changement vers une projection dédiée masque le mot de passe, mais ce bénéfice de projection ne corrige pas l'accès trop large relevé en OAS-031.
- Le profil `/me` et son changement de mot de passe résolvent bien l'utilisateur connecté depuis le contexte de sécurité. L'endpoint de liste `/all` contourne la pagination, à garder en tête côté performance, mais son impact est inclus dans la garde OAS-031 et n'est pas compté séparément.

### Étape 15 — périmètre des inscriptions et rôle demandé

- Suivi `/api/auth/signup` à travers `permitAll`, le DTO et les branches `AuthServiceImpl.register()`. La route rejette les techniciens mais transmet une demande `AGENT` sans validation de rôle; OAS-032 consigne le risque d'élévation à `SUPER_AGENT`/`MASTER`.
- Revu la création d'un technicien : elle a des contrôles au contrôleur public et dans le service pour exiger un `Agent` authentifié `SUPER_AGENT` ou `MASTER`; cette restriction est bien présente dans le code courant.
- Vérifié la modification du mot de passe public : elle exige la connaissance de l'ancien mot de passe du compte visé; pas de contournement autonome établi dans ce flux pendant cette passe.
- Revu l'évolution multi-garage des `MASTER` : l'exception au filtre et à l'affectation garage avait été retirée dès le commit initial du 12 août, mais le service utilisateurs filtre explicitement ensuite les `MASTER` sur leur garage. Sans contrat produit établissant un accès multi-garage attendu, je ne classe pas ce changement comme régression confirmée.

### Étape 16 — diagnostic générique et endpoints de devis

- Suivi le nouveau contrôleur diagnostic générique jusque dans le service : il n'applique pas les vérifications d'assignation utilisées par le portail technicien. Les mutations et lectures directes par ID sont consignées en OAS-033.
- Comparé les routes client de devis, qui vérifient le propriétaire, avec les routes de gestion génériques du même contrôleur. La séparation des fonctions n'est pas imposée côté serveur; lecture, validation, annulation, modification et suppression génériques sont consignées en OAS-034.
- Les deux modules sont des ajouts de septembre; le constat concerne les frontières d'autorisation de leurs nouveaux endpoints, et non une suppression d'un comportement historique.

### Étape 17 — routes de gestion client et données personnelles

- Comparé l'historique avant août au refactor du 2 septembre : les routes et la portée générale `/api/clients/**` existaient déjà, donc OAS-035 est explicitement classé risque hérité, pas régression de période.
- La projection DTO introduite au refactor masque le hash du mot de passe; elle réduit les champs exposés sans corriger les contrôles d'accès historiques.

### Étape 18 — propriété et rattachement des véhicules

- Comparé la baseline de mai aux routes actuelles : les routes génériques sans contrôle de propriétaire précèdent août; OAS-036 est conservé comme risque hérité, non comme régression introduite sur la période.
- Vérifié la protection ajoutée le 18 septembre sur l'archivage client : `archiveVehiculeByClient` compare bien le propriétaire avant de muter.
- La liste `/me` est passée aux véhicules actifs; pas de perte de fonction historique repérée dans ce changement. Les dépendances du véhicule sur rendez-vous/OR expliquent la gravité du risque général, mais ne datent pas son introduction.

### Étape 19 — validation et publication de proforma

- Comparé le flux de publication avant/après le 17 août : `valider-envoi` est une nouvelle opération de visibilité/email sans restriction staff côté serveur; consigné OAS-037.
- Suivi l'effet de l'appel dans le service : sauvegarde `visibleClient=true`, génération PDF puis email à chaque appel si le client a une adresse; aucune protection d'idempotence ou de statut antérieur repérée.
- Les endpoints client `client-valider` et `client-refuser` font une vérification de propriété (OAS-021 et annotations historiques); cette vérification n'est pas appliquée à `valider-envoi`.

### Étape 20 — messagerie privée et liste des conversations

- Suivi les routes agent/client jusqu'au service : les lectures d'une conversation individuelle et les réponses exigent un compte `Agent`, tandis que les endpoints client utilisent le client de session.
- Vérifié l'endpoint global `/conversations` : contrairement aux autres routes agent, il n'extrait aucun agent et retourne toutes les conversations depuis le service; consigné OAS-038.
- Les notifications unifiées de la même période filtrent les lectures par utilisateur courant et vérifient la propriété avant `markAsRead`; pas de défaut équivalent retenu dans ce passage.

### Étape 21 — filtres catalogue et chemins de stock

- Comparé l'ancienne conversion stricte de `TypePiece` aux parsings manuels introduits le 29 septembre : une valeur `type` non reconnue est ignorée et élargit les résultats; consigné OAS-039 (faible gravité).
- Vérifié les filtres valides `type`, `statut`, dépôt et mot-clé dans le contrôleur vers la specification JPA. Le cas de `statut` invalide est aussi avalé, mais comme ce filtre a été ajouté dans le changement, il s'agit d'une validation déficiente de la nouvelle option.
- Contrôlé les annotations de cache commentées le 11 septembre : les lectures `@Cacheable` des statistiques et tableaux de bord étaient elles aussi désactivées dans cette version; leur retrait ne constitue donc pas une invalidation manquante dans le code courant.
- Les chemins entrée/sortie/ajustement/inventaire exigent la résolution d'un compte Agent dans les services. La logique d'invariants et historiques stock déjà décrite dans OAS-001/OAS-006/OAS-007/OAS-024/OAS-025/OAS-026 a été recroisée; aucun nouveau défaut de mouvement daté dans cette passe.

### Étape 22 — invalidation du cache d'identité entre sessions client

- Suivi le `shareReplay(1)` de `getMe()` jusqu'au logout, la navigation sans reload et la mise à jour de profil par ID. L'invalidation n'est appelée qu'après sauvegarde du profil; consigné OAS-040 avec le scénario A → logout → B.
- Confirmé que le frontend se sert de l'ID mis en cache pour `PUT /api/clients/{id}` et que le backend ne revérifie pas la propriété (interaction avec OAS-035).
- La vérification a été faite statiquement; aucun changement applicatif ni test/session de production n'a été exécuté.

### Étape 23 — transition de la proforma vers les étapes de stock

- Comparé `StepProformaComponent` avant/après `0c54129` : la nouvelle séparation de la validation proforma et du passage d'étape conserve les transitions attendues selon disponibilité des pièces, mais son callback d'erreur a perdu le traitement explicite qui affichait l'échec et maintenait l'écran. Consigné OAS-041.
- Recroisé l'appel Angular avec `OrdreReparationService.updateStatut()`; l'observable HTTP transmet bien les erreurs au callback, et la branche les absorbe sans validation d'état backend.
- Analyse statique uniquement : aucune requête n'a été provoquée et aucun fichier applicatif modifié.

### Étape 24 — cohérence de l'état après accord du proforma

- Suivi le clic « Valider le proforma » vers `ProformaService.valider()` puis `ProformaServiceImpl.valider()` : le backend fixe `BON_DE_COMMANDE` sans consulter les lignes ou la disponibilité du stock.
- Recroisé avec le calcul `hasRupture` exécuté à l'étape suivante et l'auto-redirection du détail basée sur le statut persisté; une interruption entre ces deux actions renvoie un ordre sans rupture vers l'approvisionnement. Consigné OAS-042.
- Le chemin d'ordres avec rupture suit le routage prévu; pas d'autre défaut établi pour ce clic dans cette comparaison.

### Étape 25 — validation persistée du diagnostic

- Suivi `marquerValide()` : envoi du DTO avec `statut: 'VALIDE'`, sauvegarde via `/api/diagnostics/step`, puis transition distincte de l'ordre à `PIECES_MO`.
- Vérifié le DTO Java et `DiagnosticServiceImpl.saveFromStep()` : le champ `statut` est absent du DTO; le service initialise le diagnostic à `EN_COURS` et ne change pas le statut lors des mises à jour. Le détail OR inclut bien `diagnostic.statut`, que le composant relit prioritairement. Consigné OAS-043.
- Aucun autre écart de contrat établi dans la liste paginée du portail technicien : le frontend convertit sa page 1-based en index 0-based et la réponse `PageResponse` est décodée par les utilitaires communs.

### Étape 26 — bon de sortie et consommation des pièces au démarrage de réparation

- Recroisé le payload des lignes personnalisées avec OAS-008 : confirmation du même défaut BDS, sans créer une fiche en doublon.
- Retracé la séquence de double débit et comparé à OAS-007 : même défaut de stock déjà consigné; le démarrage depuis l'étape Assignation n'est qu'un chemin d'activation supplémentaire, pas une nouvelle régression.
- Aucun mouvement de stock réel ni test d'intégration exécuté; analyse fondée sur les écritures statiques et leur ordre.

### Étape 27 — projection des stocks de pièces dans le détail OR

- Inspecté les commits du 2 octobre, dont `24b7524` : le détail OR a migré de l'entité JPA vers `OrdreReparationResponseDTO`.
- Comparé les champs stock du DTO avec le mapper puis avec `hasRupture` (proforma) et le calcul de `manquant` (approvisionnement). Les champs sont déclarés mais non mappés; consigné OAS-044.
- Recroisé l'effet temporel avec OAS-042 : OAS-042 reste une écriture de statut inconditionnelle, tandis que la nouvelle projection rend maintenant vraie la branche de rupture même si le stock physique suffit.
- `24b7524` omet aussi le statut dans son sous-DTO Diagnostic, mais l'étape frontend effectue une lecture séparée de `/api/diagnostics/ordre-reparation/{id}`; aucun constat distinct retenu pour cette omission.

### Étape 28 — commits transverses du 2 octobre : emails diagnostic, compte fidèle et erreurs

- Commit `f0a09b8` : lié l'envoi email ajouté à `saveFromStep()` aux appels d'autosauvegarde existants du formulaire diagnostic; consigné OAS-045.
- Commit `036c28e` : vérifié les nouvelles routes `fidele`, les champs NINEA/RCCM/RIB ajoutés aux réponses client et l'absence de garde/contrôle propriétaire; consigné OAS-046 en interaction avec le risque hérité OAS-035.
- Commit `036c28e` : le catch-all `RuntimeException` convertit désormais les erreurs inattendues de 500 en 400 et renvoie le message; consigné OAS-047.
- Les autres changements récents examinés : la génération PDF devis a été déplacée dans une classe dédiée sans perte visible du HTML; la correction diagnostic `204` pour absence de résultat correspond au `catchError(() => of(null))` frontend; `a08b775` retire un cast PDP invalide pour les pièces génériques (correction positive); la validation internationale de téléphone et les contrôles de confirmation de mot de passe n'ont pas montré de régression métier dans ce contrôle ciblé.

### Étape 29 — passe de cohérence de cette revue groupée

- Recroisé les OAS-044/OAS-045 provisoires du lot précédent avec OAS-008/OAS-007 : supprimés comme doublons; leurs confirmations restent notées à l'étape 26.
- Vérifié les commits récents jusqu'au 2 octobre et leurs conséquences intermodules; ajouté OAS-044 à OAS-047 pour la projection de stock OR, les emails d'autosauvegarde diagnostic, les nouvelles routes du compte fidèle et le statut d'erreur global.
- Vérifié la cohérence des références d'étapes et la mise à jour de la borne temporelle; le fichier rapport n'a pas d'espaces de fin. Aucun code applicatif/build/test/service/données n'a été modifié ou exécuté pour cette passe.

### OAS-048 — La réception totale masque l'échec du bon de sortie automatique et diverge de la réception partielle

**Sévérité :** élevée — commande réceptionnée sans débloquer l'ordre de réparation
**État :** régression confirmée dans le flux ajouté pendant la période.
**Zone :** réception d'un bon de commande lié à un ordre de réparation.

Le service expose deux chemins de réception. Dans `receptionner()` (réception totale historique), le statut du BC passe à `RECU` et les entrées de stock sont effectuées avant la génération du BDS. Le code construit ensuite le BDS automatique même si `lignesPieces` est vide; si sa validation échoue, l'exception est seulement journalisée et avalée. Le BC peut donc être persisté comme reçu avec le stock entré, alors que l'ordre reste `BON_DE_COMMANDE` et qu'aucun BDS n'a été associé. La réception avec quantités, elle, ne tente pas de créer de BDS vide et relance l'exception de création pour provoquer l'échec transactionnel. Les deux chemins donnent ainsi des garanties différentes pour une même opération métier.

Le risque est renforcé par le calcul des lignes automatiques : il ne prend que les quantités pouvant encore être sorties du magasin (`aSortirMagasin`). Si, au moment de la réception, la pièce est déjà suffisamment disponible en atelier ou si aucune quantité positive ne peut être générée, la liste est vide. Le flux partiel saute proprement la création, mais ne fait alors pas avancer l'ordre depuis `BON_DE_COMMANDE`; le flux total tente le BDS vide, échoue silencieusement et laisse également l'ordre sans progression. La politique métier attendue pour les quantités déjà disponibles n'est pas établie par le code et reste à confirmer.

**Preuve historique :** `BonDeCommandeServiceImpl.receptionner()` et `receptionnerAvecQuantites()` apparaissent dans le nouveau module de septembre (`acc6b32`/`b7361eb`). La voie totale englobe la création automatique dans un `catch` qui ne relance pas l'exception; la voie détaillée ajoutée dans le même ensemble vérifie `!lignesPieces.isEmpty()` et relance les échecs de BDS. Il s'agit d'une divergence dans le flux introduit pendant la période, non d'un changement métier prouvé par migration.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/service/BonDeCommandeServiceImpl.java` (`receptionner`, `receptionnerAvecQuantites`)
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeCommande/dto/ReceptionBonDeCommandeRequest.java`
- `oas-back/src/main/java/sn/oas/facturation/features/bonDeSortie/service/BonDeSortieServiceImpl.java` (validation du BDS)

**Interaction avec constats existants :** OAS-008 couvre la déréférence des lignes personnalisées dans la préparation automatique, mais pas l'erreur absorbée après que la réception totale et l'entrée de stock ont commencé. OAS-009 couvre les effets sautés quand le statut OR a déjà avancé; le présent constat est le chemin distinct où il n'avance pas à la suite d'un BDS vide ou rejeté.

### Étape 30 — réceptions BDC, contrôleurs atelier et clôture du lot

- Comparé les deux chemins de réception BDC jusqu'aux effets stock, au reçu, au BDS automatique et au statut OR. Confirmé la divergence de rollback et l'exception avalée sur la réception totale; consigné OAS-048. Recroisé le cas des lignes personnalisées avec OAS-008 pour ne pas dupliquer ce défaut.
- Repris l'historique Fiche Atelier : le commit du 24 septembre change un refus d'OR actif en suppression automatique d'un ordre sans fiche associée. L'OR sans fiche est un cas supporté par le contrôleur/service de création directe et par l'ancien flux de validation des rendez-vous. La suppression peut donc effacer un ordre valide et ses liens en cascade; confirmation ajoutée à OAS-011 plutôt qu'un nouveau doublon.
- Parcouru les contrôleurs User, Technicien, Client, Fiche Atelier et OR contre les matchers de sécurité. Les constats de nouvelles routes/permissions restent ceux déjà consignés (OAS-031, OAS-046); `/api/ordres-reparation/**` garde une règle de rôles dédiée. Les autres API génériques exposent encore un périmètre d'accès large, mais une part était déjà présente avant août et ne peut être comptée comme régression sans nouvelle différence datée.
- Le passage statique consolidé est avancé; la revue n'est pas exhaustive au sens littéral de chaque fichier des deux dépôts. Il reste les validations dépendant du schéma/données réels et plusieurs parcours moins fréquents listés ci-dessous.

### OAS-049 — L'étape « paiement validé » du workflow OR contourne le règlement réel

**Sévérité :** élevée — livraison possible sans paiement complet
**État :** régression de workflow confirmée dans l'étape OR introduite le 18 septembre (`3f2799d`/`0c54129`).
**Zone :** étapes Facturation/Règlement et Livraison de l'ordre de réparation.

L'étape frontend « Facturation & Règlement » charge une facture pour afficher son numéro et `statutPaiement`, mais `validateStep()` appelle uniquement `OrdreReparationService.updateStatut(id, 'PRET_A_LIVRER')`. Elle n'appelle pas le parcours reçu/paiement et le backend `OrdreReparationServiceImpl.updateStatut()` accepte la transition sans vérifier l'état de la facture. L'étape Livraison appelle ensuite directement `updateStatut(id, 'LIVRE')`, sans garde d'encaissement non plus.

Le parcours backend d'encaissement existe séparément : `POST /api/recus` / `/payer` crée un reçu, met à jour le montant payé et le reste; `RecuServiceImpl.create()` ne place l'OR en `PRET_A_LIVRER` que lorsque le reste atteint zéro. La nouvelle étape OR n'utilise pas cette opération : un agent peut donc marquer « paiement validé », puis livrer, alors que la facture reste `NON_PAYE`, partielle, ou absente. Le texte affiché dans l'interface présente une confirmation métier que l'appel serveur ne contrôle pas.

**Preuve historique :** les composants de paiement et de livraison sont ajoutés par `3f2799d`; `0c54129` change l'état cible de `TERMINE` à `PRET_A_LIVRER`, sans ajouter d'appel à l'API reçus. Le service reçu et sa règle « reste à zéro » sont créés dans la même période (`acc6b32`/`b7361eb`). Dans l'implémentation courante, le seul effet du bouton de l'étape est une mise à jour du statut OR.

**Fichiers :**
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-paiement/step-paiement.component.ts` (`validerPaiement`, `loadInvoice`)
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-livraison/step-livraison.component.ts` (`livrerVehicule`)
- `oas-back/src/main/java/sn/oas/facturation/features/ordreReparation/service/OrdreReparationServiceImpl.java` (`updateStatut`)
- `oas-back/src/main/java/sn/oas/facturation/features/recu/service/RecuServiceImpl.java` (`create`)

**Limite :** le système peut avoir une politique de paiement hors application; aucun besoin métier de crédit/exception n'est documenté dans le code parcouru. Le constat porte sur le décalage entre le libellé « paiement validé », la facture enregistrée et les statuts qui autorisent la livraison.

### OAS-050 — L'étape paiement cherche la facture dans une page arbitraire et peut annoncer qu'elle est absente

**Sévérité :** moyenne — écran de règlement trompeur sur les factures anciennes
**État :** défaut introduit avec l'étape OR du 18 septembre (`3f2799d`).
**Zone :** chargement de la facture associée à un OR.

`StepPaiementComponent.loadInvoice()` appelle `FactureService.getAll()` sans préciser de page puis cherche localement une facture dont l'`ordreReparationId` correspond. La route backend `GET /api/factures` renvoie par défaut une `Page` de 10 résultats; si la facture de cet ordre n'est pas dans la page initiale, le composant affiche « Aucune facture spécifique trouvée ou en cours d'encaissement » même si elle existe. En cas d'erreur réseau, il traite aussi l'erreur comme une liste vide sans distinguer échec de chargement et absence de facture.

Cela renforce OAS-049 : l'agent peut continuer vers la confirmation d'un « paiement » alors que la facture n'a simplement pas été trouvée par la recherche locale incomplète.

**Preuve historique :** le composant et son appel `getAll()` sont nouveaux en septembre; le contrôleur courant renvoie une `Page<FactureListResponse>` avec `page=0`, `size=10` par défaut. Le service frontend retourne la page sans faire de requête ciblée par OR.

**Fichiers :**
- `oas-front/src/app/agent/ordres-reparation/ordre-reparation-detail/steps/step-paiement/step-paiement.component.ts`
- `oas-front/src/app/agent/factures/facture.service.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/facture/controller/FactureController.java`

### Étape 31 — facture, règlement, portail technicien et transfert véhicule

- Suivi la facture créée à la validation BDS jusqu'à l'encaissement et la livraison. Confirmé que le nouveau bouton de l'étape OR avance à `PRET_A_LIVRER` sans enregistrer un reçu; le chemin réel du reçu n'avance qu'après paiement complet (OAS-049).
- Vérifié l'appel `getAll()` de l'étape paiement contre la pagination par défaut de `GET /api/factures`; confirmé le faux négatif au-delà des dix premières factures et l'erreur réseau confondue avec une absence (OAS-050).
- Recroisé le portail technicien avec le commit `11942fe` et l'écran : la règle serveur limite les écritures à l'état `DIAGNOSTIC`, et le client masque les contrôles en dehors de ce même état; pas de régression d'accès retenue.
- Parcouru le service de transfert de véhicule : demande limitée au client courant, décision limitée aux rôles staff et verrouillée, avec historique de propriété; aucune perte de comportement datée établie dans cette passe.
- Aucune base réelle interrogée et aucun test/build/service lancé. Les modifications locales existantes des deux modules ont été préservées.

### OAS-051 — Les filtres facture statut/client/date ne couvrent plus que la page chargée

**Sévérité :** moyenne — recherche et suivi des factures incomplets
**État :** régression de pagination confirmée par comparaison avant/après le 7 septembre (`5d888f9`).
**Zone :** liste backoffice des factures.

La liste passe à la pagination serveur via `getAll(getPageParams())`, qui renvoie une page de dix factures. `applyFilter()` continue cependant à filtrer `this.factures` localement pour le statut, le client et les dates; ces filtres restent donc limités à la page actuellement reçue alors que la pagination et `totalElements` comptent toujours toutes les factures non filtrées. Les factures correspondant aux critères sur les pages suivantes sont omises et l'indication de volume ne correspond pas au résultat filtré. Le mot-clé est, lui, envoyé au backend par le composant parent.

**Preuve historique :** avant `5d888f9`, `load()` chargeait la collection puis le filtrage local portait sur l'ensemble reçu. Le commit active les pages serveur sans déplacer les filtres statut/client/date vers la requête et sans filtrer toutes les pages.

**Fichiers :**
- `oas-front/src/app/agent/factures/factures.component.ts` (`load`, `applyFilter`)
- `oas-front/src/app/agent/factures/facture.service.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/facture/controller/FactureController.java` (pagination/keyword seulement)

### OAS-052 — La recherche rendez-vous ignore le statut quand un mot-clé est aussi renseigné

**Sévérité :** moyenne — résultats de recherche mélangés entre statuts
**État :** défaut de contrat introduit avec les filtres paginés du backoffice en septembre.
**Zone :** recherche et filtre statut des rendez-vous.

Le composant agent transmet en même temps `keyword` et `statut` à `GET /api/rendez-vous`. Le contrôleur choisit exclusivement une branche : quand le mot-clé n'est pas vide, il appelle `searchRendezVous()` et n'applique jamais le statut; les autres branches par `clientId` ou `statut` ne sont évaluées que sinon. Une recherche comme « Dupont » avec statut « confirmé » retourne donc aussi les rendez-vous du même client dans les autres statuts. L'interface affiche néanmoins le filtre statut sélectionné.

**Preuve historique :** le contrôleur déclare les deux paramètres mais les traite dans un `if / else if`; le composant courant envoie simultanément les deux. Le backend ne possède pas de recherche combinée keyword+statut pour cette route.

**Fichiers :**
- `oas-front/src/app/agent/rendezvous/rendezvous.component.ts` (`load`)
- `oas-front/src/app/agent/rendezvous/rendezvous.service.ts`
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/controller/RendezVousController.java` (`getAllRendezVous`)
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/repository/RendezVousRepository.java`

### OAS-053 — Le formulaire de création de facture ne charge plus les clients ni les OR sur la branche `2oct`

**Sévérité :** élevée — création manuelle de facture inutilisable depuis la liste
**État :** régression introduite le 7 septembre (`5d888f9`) et encore présente dans le HEAD audité (`2oct`, `58bbea3`).
**Zone :** assistant de création de facture.

Dans `ngOnInit()`, les deux sources du `forkJoin` (`clientService.getAll()` et `ficheService.getAll()`) sont commentées, alors que le callback consomme toujours les propriétés `clients` et `fiches`. Avec RxJS 7.8, `forkJoin({})` termine sans émettre; le callback ne s'exécute donc jamais et `clients`/`ordresReparation` restent leurs tableaux initiaux vides. Le sélecteur du formulaire ne peut proposer aucun client ni ordre de réparation, rendant le chemin manuel inutilisable.

**État des branches :** le commit `e1a69e5` du 2 octobre, accessible sur `remotes/sadouzz/main`, réactive ces deux appels, mais il n'est pas ancêtre du HEAD `2oct` audité. Le défaut est donc corrigé dans cette branche distante mais pas dans l'état courant de cette branche de travail.

**Preuve historique :** le commit `5d888f9` a commenté les deux appels tout en gardant leur consommation; le HEAD `58bbea3` est encore dans cet état. Aucun fichier applicatif n'a été modifié pendant l'audit.

**Fichiers :**
- `oas-front/src/app/agent/factures/factures.component.ts` (`ngOnInit`)
- `oas-front/src/app/shared/models/api-response.model.ts` (`extractContent` retourne `[]` sur `undefined`)

### OAS-054 — L'encaissement depuis la liste facture appelle une route backend supprimée

**Sévérité :** élevée — paiement des factures impossible depuis le bouton d'encaissement
**État :** régression de contrat confirmée lors du déplacement backend de septembre.
**Zone :** bouton « Encaisser paiement » du backoffice.

Le frontend envoie `POST /api/admin/portal/factures/{id}/payer`. Cette route était fournie par l'ancien `AdminPortalController` pré-période. Elle n'existe plus dans le backend courant. Le module reçu expose désormais `POST /api/recus/payer` avec les mêmes données sous forme de paramètres `factureId`, `montant` et `methodePaiement`; aucun contrôleur courant ne mappe l'ancien chemin. Le bouton de paiement du tableau échoue donc avant d'atteindre la logique qui crée le reçu et met à jour `montantPaye`, `resteAPayer` et le statut de paiement.

**Preuve historique :** la route `/api/admin/portal/factures/{id}/payer` se trouve dans la baseline dans `AdminPortalController`; elle n'existe plus dans le contrôleur courant. Le frontend conserve l'ancien chemin depuis juin, tandis que `RecuController` courant expose la nouvelle route `/api/recus/payer`.

**Fichiers :**
- `oas-front/src/app/agent/factures/facture.service.ts` (`payFacture`)
- `oas-front/src/app/agent/factures/factures.component.ts` (`submitPayment`)
- `oas-back/src/main/java/sn/oas/facturation/features/recu/controller/RecuController.java` (`/payer`)
- ancienne route : `oas-back/src/main/java/sn/oas/facturation/client/controller/AdminPortalController.java`

### OAS-055 — Tout compte authentifié non client peut créer un rendez-vous « agent » pour n'importe quel client

**Sévérité :** élevée — création de rendez-vous au nom d'un tiers
**État :** régression d'autorisation introduite le 11 septembre (`f9a4cc2`).
**Zone :** `POST /api/rendez-vous`.

Cette route porte seulement `@PreAuthorize("isAuthenticated()")`. Elle identifie les clients par l'autorité exacte `ROLE_CLIENT`; si l'utilisateur n'a pas cette autorité, elle appelle `createRendezVousByAdmin(request)`. Un compte Technicien authentifié, ou tout autre rôle non client, peut ainsi envoyer les `clientId` et `vehiculeId` d'un tiers. Le service vérifie que le véhicule appartient au client fourni, mais ne vérifie pas que l'appelant est un membre du personnel autorisé. Le contrôleur possède bien une route `/api/rendez-vous/admin` avec une liste de rôles staff, mais le frontend agent continue d'utiliser la route partagée et la restriction de celle-ci reste trop large.

**Preuve historique :** le commit `f9a4cc2` ajoute l'autorisation générique authentifiée et la branche « sinon admin » en même temps qu'une route admin explicitement limitée aux rôles du personnel. La configuration globale ne refuse pas les autres comptes authentifiés.

**Fichiers :**
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/controller/RendezVousController.java` (`bookRendezVous`, `createRendezVousByAdmin`)
- `oas-back/src/main/java/sn/oas/facturation/features/rendezvous/service/RendezVousServiceImpl.java` (`createRendezVousByAdmin`)
- `oas-back/src/main/java/sn/oas/facturation/security/WebSecurityConfig.java`
- `oas-front/src/app/agent/rendezvous/rendezvous.service.ts`

### Étape 32 — facturation, rendez-vous et vérification des contrats HTTP

- Comparé la liste factures avant/après pagination : les filtres statut/client/date sont restés locaux et limités à la page. Consigné OAS-051.
- Suivi simultanément le keyword et le statut des rendez-vous de l'UI au repository : branche de recherche exclusive côté contrôleur, consigné OAS-052.
- Vérifié `forkJoin({})` sous RxJS 7.8 (complétion sans émission) et comparé la correction `e1a69e5` sur `remotes/sadouzz/main`, absente de l'ascendance du HEAD audité; consigné OAS-053 avec cette nuance de branche.
- Comparé le bouton encaissement au contrôleur de la baseline et au `RecuController` courant : l'ancien endpoint frontend n'est plus mappé, consigné OAS-054.
- Suivi `POST /api/rendez-vous` via l'autorité du principal et le service de création administrative; un compte non-client peut emprunter le chemin admin, consigné OAS-055.
- L'historique des reçus en backoffice reçoit la liste complète, et sa pagination locale couvre cette liste; pas de régression ajoutée sur cette partie. Les contrôles du paiement restent statiques; aucune requête d'application n'a été envoyée.

### À examiner dans les prochains lots

1. Pièces/stock : revus les migrations et plusieurs chemins d'appel transverses aux étapes 1–3, 7–9, 21, 26 et 27. La cohérence avec les données réellement persistées et l'historique de mouvements de la production reste non vérifiée sans base active.
2. Réparation/diagnostic : principales transitions UI→API→service suivies aux étapes 3, 6–7 et 23–32; la divergence BDC→BDS est en OAS-048, le contournement de paiement en OAS-049 et le lookup paginé en OAS-050. L'idempotence de réception et la politique d'avancement quand aucun mouvement atelier n'est nécessaire restent à confirmer.
3. Fiches atelier/rendez-vous : constats principaux consignés aux étapes 4 et 11; les fichiers actuellement modifiés localement ont été préservés et ne sont pas pris comme changements historiques. Compléter une comparaison historique exhaustive de tous les écrans et configurations.
4. Permissions/tenant : principaux défauts datés consignés dans OAS-012/OAS-013/OAS-027/OAS-031 à OAS-038/OAS-046; OAS-035/OAS-036 restent des risques hérités. Le passage par chaque méthode de chaque contrôleur n'est pas certifié exhaustif par les seuls balayages statiques.
5. Auth/session : expiration, inscription, déplacement des routes utilisateurs et cache de profil examinés aux étapes 13–15 et 22; les chemins entreprise et tous les endpoints utilisateur restent à confirmer dans une passe dédiée.
6. Achats, fournisseurs, clients, véhicules, facturation et rendez-vous : défauts majeurs relevés dans les étapes 3–11, 19–20, 28–32; compléter les chemins moins fréquentés et la matrice de statuts.
7. Clôture historique : 120/98 commits et les suppressions/renommages baseline→HEAD ont été inventoriés; les migrations OAS-024/OAS-026/OAS-029 exigent toujours une comparaison de schéma réelle. Aucun serveur ou service de données n'a été lancé.


## Méthode de vérification

Pour chaque lot, comparer les anciennes et nouvelles versions par historique Git et détection de renommage, suivre le flux UI → service HTTP → contrôleur → service métier → repository/entity/DTO, puis inscrire ici uniquement les pertes de comportement prouvées ou les risques justifiés. Ne pas modifier l'application dans le cadre de cet audit.
