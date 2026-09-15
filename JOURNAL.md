# Journal

Une entrée par étape : ce qui a été fait, ce que j'ai appris, où j'ai buté.
Les questions de contrôle et leurs réponses complètes sont dans `docs/revision/`.

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
