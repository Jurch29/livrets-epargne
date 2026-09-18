# Journal

Une entrée par étape : ce qui a été fait, ce que j'ai appris, où j'ai buté.
Les questions de contrôle et leurs réponses complètes sont dans `docs/revision/`.

## ▶ Prochaine étape (à mettre à jour à chaque fin d'étape)

- **En attente** : questions de contrôle de l'étape 1.4 (voir fin du journal)
- **Ensuite, phase 1** :
  - 1.5 — Unicité du Livret A par personne. Premier invariant *entre* agrégats : il
    faudra une identité de titulaire, et trancher entre service de domaine, contrainte
    de base et cohérence à terme
- **Reporté après l'entretien** : étapes 0.3 (formatage Spotless) et 0.4 (compose, README)

## Phase 0 — Fondations

### 0.1 — Initialisation du dépôt (2026-09-15)

**Fait**
- Dépôt git (branche `main`), `.gitignore`, `.gitattributes`
- `CLAUDE.md` (règles du projet), `docs/BRIEF.md` (cahier des charges d'origine)
- Wrapper Gradle 9.7.1 avec checksum SHA-256 de la distribution, `settings.gradle.kts`

**Décisions**
- Architecture hexagonale en 3 modules Gradle (`domain`, `application`, `infrastructure`),
  chacun créé à la phase où il devient nécessaire — ADR 0001
- Spring Boot 4.1.x plutôt que 3.x (écart assumé par rapport au brief)
- Identifiants métier en français, techniques en anglais
- TDD en ping-pong pour la phase 1

**Appris**
- Le wrapper jar se commite (il amorce le téléchargement de Gradle) ; le checksum de
  distribution protège le zip téléchargé, **pas** le jar lui-même → validation du
  wrapper en CI
- Un ADR est une décision datée et immuable ; on le remplace, on ne le réécrit pas
- Sources de non-reproductibilité d'un build : Gradle, JDK, versions dynamiques,
  dépôts, plugins, environnement (fuseau, date), contenu des archives

**Points de blocage**
- Aucune des 3 questions de contrôle sue → réponses dans
  `docs/revision/2-approfondissement.md` (questions de niveau 2 : non prioritaires)
- Docker non installé sur le poste : nécessaire avant l'étape 0.4 (compose) et la phase 3

### 0.2 — Squelette de build et module `domain` (2026-09-15)

**Fait**
- `settings.gradle.kts` : dépôts centralisés (`FAIL_ON_PROJECT_REPOS`), module `domain`
- `gradle/libs.versions.toml` : JUnit 6.1.3 (via BOM), AssertJ 3.27.7
- `domain/build.gradle.kts` : `java-library`, toolchain Java 21, aucune dépendance de prod
- ADR 0001 — architecture hexagonale en modules Gradle
- Vérifié : `runtimeClasspath` de `domain` vide ; JUnit 6 s'exécute (test jetable supprimé)

**Décisions**
- Pas de convention plugin `build-logic/` tant qu'il n'y a qu'un module : rien à
  mutualiser. Extraction prévue en phase 2, quand `application` dupliquera la config
- JUnit 6 plutôt que 5 (version courante, alignée sur Spring Boot 4 ; même API Jupiter)

**Appris**
- `api` vs `implementation` : un type exposé dans une signature publique → `api`
- `--release` / toolchain protègent contre l'usage d'une API absente de Java 21 ;
  `sourceCompatibility` non
- BOM = versions recommandées ; `enforcedPlatform` impose (y compris à la baisse)

**Points de blocage**
- Aucune des 3 questions sue → réponses dans `docs/revision/2-approfondissement.md`
- Linux Mint : le dépôt Docker attend le nom de code Ubuntu (`noble`), pas celui de
  Mint (`zena`)
- Installation de Docker bloquée par le portail captif du Wi-Fi (pas un proxy) :
  valider la page d'accueil du réseau, sinon reporter

### Ajustement — préparation de l'entretien du 2026-09-18

**Constat** : les questions de contrôle des étapes 0.1 et 0.2 portaient sur des détails
d'outillage, pas sur ce qu'évalue un entretien craft.

**Décisions**
- Révision en deux niveaux dans `docs/revision/` : `1-fondamentaux.md` (principes,
  vision, bonnes pratiques — rédigé en entier dès maintenant pour être lu d'ici
  vendredi) et `2-approfondissement.md` (détails, pièges). Remplace `CHEATSHEET.md`
- Questions de contrôle : 2 de niveau 1 + au plus 1 de niveau 2 optionnelle
- Étapes 0.3 et 0.4 reportées après l'entretien ; phase 1 immédiatement

## Phase 1 — Domaine pur

### 1.1 — Premier cycle TDD : le dépôt (2026-09-15)

**Fait** (un commit par cycle rouge → vert → refactor)
- `Livret.ouvrir()` à solde nul
- `deposer()` : un dépôt augmente le solde, deux dépôts s'additionnent
- Refactor : extraction du value object `Montant` (package `commun`)
- Refus d'un versement nul (`VersementInsuffisantException`), solde inchangé

**Décisions**
- Méthode de fabrique `ouvrir()` + constructeur privé : un seul point d'entrée, objet
  valide dès sa création
- `Montant` = record immuable : jamais négatif, au plus deux décimales, échelle
  normalisée à 2 (sinon `100` ≠ `100.00` à cause de `BigDecimal.equals`), construit
  depuis une `String` pour ne jamais passer par un `double`
- Montant invalide → `IllegalArgumentException` (la valeur n'existe pas) ; versement
  nul → exception métier (valeur valide, règle refusée)
- `Montant` extrait *pendant le refactor*, quand la règle de validité est apparue —
  pas en premier « au cas où »
- Simplifications : ouverture sans versement initial, minimum de versement ramené à
  « strictement positif » (réel : 10 € sur le Livret A), pas encore d'identifiant de
  livret (arrivera avec le premier besoin : repository ou règle d'unicité)
- Passage en mode accéléré (voir CLAUDE.md) à la demande du dev, faute de temps

**Appris**
- Entité vs value object : critère d'interchangeabilité. Deux montants de 50 € sont
  interchangeables (value object) ; un livret se suit dans le temps (entité)
- `livret.deposer()` plutôt que `setSolde()` : encapsulation des invariants ; le défaut
  inverse s'appelle le **modèle anémique** (Fowler) ; principe *Tell, don't ask*
- Syntaxe AssertJ : `assertThat(valeur).isEqualTo(attendu)` — une seule valeur, puis
  chaînage (`assertThat(a, b)` est du Hamcrest)
- *Fake it* : le code minimal peut retourner une constante ; le test suivant force la
  généralisation (triangulation)
- Un test vide passe au vert : toujours voir le test rouge d'abord

**Points de blocage**
- Premier jet de `Livret` : package par défaut (invisible depuis le test), `ouvrir()`
  d'instance qui ne retournait rien, `Double` pour de l'argent, champs `protected`,
  constructeur public laissant un solde `null`, `System.out` dans le domaine
- Réponses aux questions de contrôle justes mais non justifiées → en entretien,
  toujours donner le *pourquoi*

### 1.2 — Le retrait : le solde ne devient jamais négatif (2026-09-17)

**Fait** (un commit par cycle)
- `Montant.soustraire()` et `Livret.retirer()`
- Refus d'un retrait supérieur au solde (`SoldeInsuffisantException`), solde inchangé ;
  le retrait *égal* au solde reste autorisé et amène le solde à zéro
- Refus d'un retrait nul
- Refactor : garde unique `exigerMouvementPositif`, `VersementInsuffisantException`
  renommée `MouvementNulException` (partagée par le dépôt et le retrait)

**Décisions**
- L'invariant « jamais de solde négatif » appartient au `Livret`, pas à `Montant` :
  un montant négatif est une *valeur inexistante* (`IllegalArgumentException`, futur
  400) ; retirer plus que le solde est une *règle métier refusée* (exception métier,
  futur 422). Le refus des négatifs dans `Montant` reste un dernier filet, pas
  l'expression de la règle
- `Montant.soustraire()` a une précondition garantie par l'appelant, plutôt qu'un
  retour `Optional<Montant>` : un seul appelant, l'indirection n'est pas démontrée
- `estInferieurA` plutôt que `Comparable<Montant>` : lisible à l'appel, et rien
  n'exige encore un tri de montants (YAGNI)
- Exception renommée : « versement insuffisant » décrivait mal une règle qui n'est
  qu'un refus du montant nul. Le nom reviendra en 1.3 avec le vrai minimum
- Simplifications signalées : dans la réalité, un retrait sur Livret A doit laisser
  10 € (sinon clôture), et le retrait peut être encadré sur un Livret Jeune mineur

**Appris**
- **Garantie forte face aux exceptions** : valider *avant* de muter, pour qu'une
  opération refusée laisse l'objet exactement dans son état d'avant. Le rollback
  transactionnel protège la base, pas l'objet en mémoire que l'appelant continue
  d'utiliser s'il attrape l'exception
- Tester la **limite** (retrait égal au solde) protège du glissement `<` / `<=` —
  c'est exactement ce que cherche un test de mutation
- `BigDecimal.equals` compare l'échelle, `compareTo` non : d'où la normalisation à la
  construction de `Montant`, qui rend la représentation canonique

**Points de blocage**
- Questions de contrôle de l'étape 1.1 non répondues par le dev → réponses consignées
  dans `docs/revision/1-fondamentaux.md` (§4 tests, §6 Java moderne)

### Ajustement — dégraissage de la révision (2026-09-17)

**Constat** (du dev) : `1-fondamentaux.md` avait grossi à 525 lignes, 14 sections toutes
au même niveau de détail, avec des Q/R entièrement rédigées. Impossible à ingérer en un
jour, et on le relit sans rien retenir.

**Contexte précisé** : entretien d'1 h à 1 h 30, PC à apporter (donc du code en direct),
Java généraliste, aucun domaine métier imposé. Objectif : quelques concepts solides, des
bonnes pratiques, et Spring Boot compris de haut.

**Décisions**
- Trois fichiers par usage : `0-memo.md` (une page, le matin même), `1-fondamentaux.md`
  (6 blocs courts, 208 lignes), `2-approfondissement.md` (tout le reste, qui vit pour
  lui-même et pour d'autres projets)
- Critère de tri : garder ce qu'on doit **justifier**, couper ce qui se **cherche**
  (listes de codes HTTP, d'annotations, comparatifs Kafka/JMS)
- Réduire le **nombre de sujets**, pas seulement leur longueur : 14 sections → 6 blocs.
  Observabilité, sécurité, événementiel descendent : le projet ne les contient pas, en
  parler longuement serait du bluff
- Questions de fin de bloc **sans réponse** : se tester fait retenir, relire une réponse
  rédigée donne l'illusion de savoir
- Poids déplacé vers ce qui se joue au clavier (bloc « Coder devant quelqu'un »), le
  domaine livret n'étant plus qu'une illustration

### 1.3 — Types de livret et plafond de dépôt (2026-09-17)

**Fait**
- `TypeLivret` (enum) : Livret A 22 950 €, LDDS 12 000 €, Livret Jeune 1 600 €
- Type obligatoire à l'ouverture : `Livret.ouvrir(TypeLivret)`
- Dépôt refusé au-delà du plafond (`PlafondDepasseException`), solde inchangé ;
  dépôt amenant *exactement* au plafond autorisé
- Tests paramétrés (`@ParameterizedTest` + `@CsvSource`) sur les trois types
- ADR 0002 — enum plutôt que hiérarchie scellée

**Décisions**
- Enum plutôt que `sealed interface` : les types ne diffèrent aujourd'hui que par une
  valeur. À rouvrir en 1.4 si l'éligibilité les fait diverger en données ou en
  comportement (voir ADR 0002)
- Pas de value object `Plafond` : un plafond *est* un montant, l'enveloppe n'ajoute
  aucune règle
- Le solde visé est calculé dans une variable locale avant validation — la mutation
  reste le dernier geste
- Simplifications signalées : le plafond réel porte sur les versements (les intérêts
  capitalisés peuvent le dépasser) ; le minimum de versement de 10 € est une règle
  d'ouverture, pas de dépôt, donc hors de cette étape

**Appris**
- Un test paramétré ne remplace pas un cycle TDD : celui-ci est passé **vert du premier
  coup**, faute de « fake it » au cycle précédent. Il garde sa valeur (il fige les
  montants réglementaires) mais ce n'est pas du TDD, et il faut savoir le dire
- Vérifier qu'un test n'est pas creux : casser volontairement le code (plafond du
  Livret A en dur) et constater le rouge — c'est du test de mutation à la main
- Un enum se persiste par son **nom**, jamais par son `ordinal`
- En production, un barème réglementaire est **daté** et chargé depuis un référentiel :
  un taux ou un plafond a une période de validité, et l'historique doit rester
  recalculable

**Points de blocage**
- Aucun. Étape courte et sans surprise

### 1.4 — Titulaire et éligibilité par l'âge (2026-09-17)

**Fait**
- `Titulaire` (record réduit à sa date de naissance) et `ageLe(LocalDate)`
- `TrancheDAge` : intervalle d'âges éligibles, bornes incluses
- `TypeLivret` porte sa tranche : Livret A tous âges, LDDS 18 ans et plus,
  Livret Jeune 12 à 25 ans
- `Livret.ouvrir(type, titulaire, dateDOuverture)` refuse un titulaire hors tranche
  (`AgeNonEligibleException`) ; la date d'ouverture devient une donnée du livret
- ADR 0003 — le temps entre par les paramètres ; `CLAUDE.md` ajusté
- 28 tests

**Décisions**
- **Pas de `Clock` dans le domaine** : il reçoit une date. « Aujourd'hui » est une
  donnée de l'appel, pas du modèle ; la `Clock` sera un bean de la couche application.
  Bénéfice concret : une opération est datée une seule fois, au lieu d'appeler l'horloge
  plusieurs fois et de risquer de tomber de part et d'autre de minuit
- **L'enum tient** : l'ADR 0002 avait nommé la condition de bascule vers une `sealed
  interface` (des types divergeant par leurs données ou leur comportement). Une tranche
  d'âge est encore une *valeur* — pas de polymorphisme
- Borne haute absente représentée par une sentinelle (`Integer.MAX_VALUE`) plutôt qu'un
  `Integer` nullable : le record reste comparable par valeur, aucun appelant ne gère de
  `null`
- `Titulaire` réduit à sa date de naissance : aucune règle ne demande encore de nom ni
  d'identifiant. L'identité arrivera en 1.5 et en fera une entité
- *Object mother* dans le test (`ouvrir(type)`) : les tests qui ne parlent pas
  d'éligibilité n'ont pas à choisir un titulaire

**Appris**
- Écrire un test sur le 29 février a **révélé un écart réel** : `Period.between` fait
  vieillir au 1er mars, l'usage juridique français retient le 28 février les années non
  bissextiles. Figé par un test et documenté, plutôt que découvert en production
- Le fuseau est une décision de la couche application : une opération à 23 h 30 le
  31 décembre ne tombe pas la même année en UTC et en `Europe/Paris`
- Deux étapes de suite ont produit des tests **verts d'emblée**, faute de « fake it » :
  ce sont des tests de spécification, utiles mais qui ne sont pas du TDD. À dire tel
  quel plutôt qu'à maquiller
- Vérifier un test par mutation manuelle (casser la borne haute, constater le rouge)
  coûte trente secondes et dit la vérité sur sa valeur

**Points de blocage**
- Aucun

**Questions de contrôle (niveau 1)**
1. Pourquoi la date d'ouverture est-elle un paramètre plutôt qu'une `Clock` injectée
   dans le `Livret` ? Donner l'argument métier, pas seulement « c'est plus testable ».
2. La tranche d'âge diffère par type de livret : pourquoi n'est-ce pas un cas de
   polymorphisme ? À quelle condition le deviendrait-il ?

**Question de niveau 2 (optionnelle)**
3. `TrancheDAge` utilise `Integer.MAX_VALUE` comme sentinelle. Quelles alternatives, et
   qu'est-ce qui les rend moins bonnes *ici* ?

### Hors phasage — démo Spring Boot (2026-09-18, avant l'entretien)

**Pourquoi** : besoin de visualiser un flux HTTP complet avant l'entretien, sans attendre
la phase 3. Module `demo-spring` isolé, sans lien avec `domain`, supprimable d'un `rm`
et d'une ligne retirée de `settings.gradle.kts`.

**Fait**
- Controller / Service / Repository, un rôle par fichier, package par concept (`message`)
- `MessageRepository` en interface, `MessageEnMemoire` en implémentation : le service
  dépend du contrat, pas de la technique
- `@RestControllerAdvice` traduisant les exceptions métier en `ProblemDetail` (RFC 9457) :
  404 et 400 sans un seul `try/catch` dans le controller
- DTO d'entrée `CreationDeMessage` sans identifiant : le client ne peut pas l'imposer
- Vérifié par appels réels : 201 + en-tête `Location`, liste, lecture, 404, 400

**Appris (par l'erreur, les deux fois)**
- `Port 8080 was already in use` : la configuration externalisée se surcharge à trois
  niveaux — `application.properties`, variable d'environnement `SERVER_PORT`, argument
  `--server.port`, du moins au plus prioritaire
- `Name for argument not specified… ensure the compiler uses the '-parameters' flag` :
  Java ne conserve pas le nom des paramètres dans le bytecode par défaut, donc Spring ne
  peut pas relier `@PathVariable` à `nom`. Le plugin Gradle de Spring Boot ajoute ce flag
  d'office ; le plugin `application` utilisé ici, non. Parade universelle : nommer
  explicitement, `@PathVariable("nom")`
- Un bean est un singleton partagé par tous les threads : d'où `ConcurrentHashMap` et
  `AtomicLong` dans le repository en mémoire, jamais `HashMap` et `long`

**À décider au retour** : garder ce module comme bac à sable, ou le supprimer et laisser
la phase 3 construire l'infrastructure proprement dans l'architecture hexagonale.
