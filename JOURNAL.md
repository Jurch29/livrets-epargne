# Journal

Une entrée par étape : ce qui a été fait, ce que j'ai appris, où j'ai buté.
Les questions de contrôle et leurs réponses complètes sont dans `docs/revision/`.

## ▶ Prochaine étape (à mettre à jour à chaque fin d'étape)

- **En attente** : réponses aux questions de contrôle de l'étape 1.2
  1. Pourquoi la règle « on ne retire pas plus que le solde » est-elle portée par
     `Livret` et non par le refus des montants négatifs dans `Montant` ?
  2. Pourquoi tester le retrait *égal* au solde ?
  3. *(niveau 2, optionnelle)* En production, deux retraits concurrents de 80 € sur un
     solde de 100 € : qu'est-ce qui empêche le solde de passer à −60, et à quel prix ?
- **Ensuite, phase 1 (mode accéléré jusqu'au 2026-09-18)** :
  - 1.3 — Types de livret (Livret A, LDDS, Livret Jeune) et plafond de dépôt
  - 1.4 — Titulaire et éligibilité (âge du Livret Jeune, `Clock` injectée)
  - 1.5 — Unicité du Livret A par personne (invariant entre plusieurs agrégats)
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
