# Niveau 1 — Fondamentaux

> **Ce qu'il faut maîtriser pour un entretien.** Les principes, la mécanique, la vision
> d'ensemble et les bonnes pratiques d'un projet Java / Spring Boot de qualité.
> Pour chaque thème : l'idée, pourquoi on le fait, **un** piège à connaître, et les
> questions probables avec une réponse courte.
>
> Les détails, cas pointus et questions d'expert sont dans `2-approfondissement.md`.
> Ce fichier se complète au fil des phases : chaque phase rend un thème concret dans
> le code du projet.

---

## 0. Ce qu'un entretien « craft » évalue vraiment

- **Le raisonnement plus que la mémoire.** Justifier un choix par un contexte, et
  dire ce qu'on sacrifie. « Ça dépend » est une bonne réponse *si* on dit de quoi.
- **L'honnêteté technique.** « Je ne sais pas, mais voilà comment je raisonnerais »
  est bien reçu. Inventer est rédhibitoire.
- **L'exercice en binôme (kata) en TDD** est fréquent dans les ESN orientées craft.
  On y observe le cycle rouge/vert/refactor, la taille des pas, le nommage, la
  communication — pas la vitesse. Le kata *Bank Account* (dépôt, retrait, relevé) est
  un classique : c'est exactement la phase 1 de ce projet.
- **Le recul sur ses propres projets** : ce que tu as appris, ce que tu referais
  autrement.

---

## 1. Architecture applicative

### Couches classiques vs architecture hexagonale

**L'idée.**
- *En couches* (controller → service → repository) : simple et très répandu. Mais le
  métier dépend de la technique : le service manipule des entités JPA.
- *Hexagonale* (ports & adapters, Alistair Cockburn, 2005) : le cœur métier est au
  centre et ne dépend de rien. Il déclare des **ports** (interfaces, dans son propre
  vocabulaire). L'extérieur fournit des **adapters**.
  - Côté *pilotant* (driving) : ce qui appelle l'application — REST, CLI, consumer
    Kafka, tests.
  - Côté *piloté* (driven) : ce que l'application appelle — base de données, broker,
    API externe.
- Le mécanisme central est l'**inversion de dépendance** : l'interface
  `LivretRepository` appartient au cœur ; l'implémentation JPA vit dans
  l'infrastructure et dépend du cœur, jamais l'inverse.
- Clean Architecture (Robert C. Martin) et Onion : même famille, même règle — les
  dépendances pointent vers le métier.

**Pourquoi.** Le métier se teste sans base ni Spring (tests rapides et lisibles), et
il n'est pas pollué par les contraintes techniques (annotations, setters et
constructeur vide exigés par JPA, lazy loading).

**Le piège : l'appliquer partout.** Coût réel : plus de classes, et du mapping entre
modèles (domaine ↔ JPA ↔ DTO). Sur une application CRUD sans règles métier, c'est de
la cérémonie : les couches classiques suffisent.

**Questions probables.**
- **Q.** Différence entre port et adapter ?
  **R.** Le port est une interface définie par le cœur (ce dont il a besoin, ou ce
  qu'il offre). L'adapter est l'implémentation technique qui branche le monde extérieur
  sur ce port (contrôleur REST, repository JPA).
- **Q.** Pourquoi l'interface du repository est-elle dans le cœur et pas dans
  l'infrastructure ?
  **R.** Inversion de dépendance : c'est le métier qui exprime son besoin dans son
  langage ; la technique s'y adapte. Ainsi le métier ne dépend pas de la technique.
- **Q.** Quand ne pas faire d'hexagonal ?
  **R.** Quand le métier est pauvre (CRUD, passe-plat vers la base). L'hexagonal se
  paie en indirections ; il se justifie par la richesse des règles métier.

### Faire respecter l'architecture

- Une règle d'architecture non vérifiée automatiquement s'érode. Deux moyens :
  la **structure** (modules Gradle/Maven : une dépendance interdite ne compile pas)
  ou des **tests** (ArchUnit).
- Organiser les packages **par concept métier** (`livret`, `titulaire`) plutôt que par
  type technique (`services`, `models`, `exceptions`) : ce qui change ensemble est
  rangé ensemble.
- Documenter les décisions dans des **ADR** : une décision datée, son contexte, les
  alternatives écartées. Immuable : on le remplace par un nouvel ADR, on ne le
  réécrit pas.

---

## 2. Domain-Driven Design

### Le stratégique (le plus important, et le plus souvent oublié)

- **Langage omniprésent** (*ubiquitous language*) : le même vocabulaire chez les
  experts métier, dans le code et dans les tests. Ici : livret, titulaire, plafond,
  quinzaine, capitalisation.
- **Contexte délimité** (*bounded context*) : frontière à l'intérieur de laquelle un
  modèle et son vocabulaire sont cohérents. « Client » ne veut pas dire la même chose
  pour l'épargne et pour le marketing. Un contexte ≈ un modèle ≈ souvent une équipe.
- **Couche anti-corruption** : quand on consomme le modèle d'un autre système, on le
  traduit à la frontière pour qu'il ne contamine pas le nôtre.

### Le tactique (les briques)

| Brique | Définition | Exemple ici |
|---|---|---|
| **Entité** | Identité stable, cycle de vie, égalité par identifiant | `Livret` |
| **Value object** | Pas d'identité, immuable, égalité par valeur, **se valide à la construction** | `Montant`, `Taux`, `Plafond` |
| **Agrégat** | Grappe d'objets modifiée comme un tout, via une **racine** unique qui garantit les invariants | `Livret` et ses mouvements |
| **Repository** | Un par agrégat, se manipule comme une collection | `LivretRepository` |
| **Service de domaine** | Règle métier qui n'appartient naturellement à aucune entité | éligibilité croisant plusieurs livrets |
| **Événement de domaine** | Fait métier passé, nommé au passé | `DepotEffectue`, `LivretCloture` |

**Règles pratiques des agrégats** (Vaughn Vernon) :
1. Les invariants sont protégés à l'intérieur de la frontière de l'agrégat.
2. Des agrégats petits.
3. Référencer les autres agrégats par identifiant, pas par référence d'objet.
4. Un seul agrégat modifié par transaction ; entre agrégats, cohérence à terme
   (événements).

**Pourquoi.** Toutes les règles au même endroit : il devient impossible de construire
un objet dans un état invalide.

**Le piège : le modèle anémique** (Martin Fowler). Des entités à getters/setters, et
toute la logique dans des services : `livretService.deposer(livret, montant)` qui
fait `livret.setSolde(...)`. Rien n'empêche un autre code d'appeler
`setSolde(-100)`. En DDD : `livret.deposer(montant)`, pas de setter, l'invariant vit
dans la méthode.

**Questions probables.**
- **Q.** Entité ou value object : comment trancher ?
  **R.** Est-ce que deux instances aux mêmes valeurs sont interchangeables ? Oui →
  value object (deux billets de 10 €). Non, on suit *celui-là* dans le temps → entité
  (un livret dont le solde change reste le même livret).
- **Q.** Qu'est-ce qu'un invariant ?
  **R.** Une condition toujours vraie pour un objet valide, garantie par l'objet
  lui-même. Ici : le solde n'est jamais négatif ; un dépôt ne peut pas faire dépasser
  le plafond.
- **Q.** Pourquoi des agrégats petits ?
  **R.** Un agrégat se charge, se verrouille et se sauvegarde en entier. Gros agrégat =
  chargements lourds et conflits de concurrence fréquents.
- **Q.** Le DDD, c'est pour tous les projets ?
  **R.** Le tactique, non : seulement pour un métier complexe. Le stratégique (langage
  commun, frontières de contextes) est utile presque partout.

---

## 3. TDD

**L'idée.**
- **Rouge** : écrire un test qui échoue, pour la bonne raison.
- **Vert** : écrire le code *minimal* qui le fait passer.
- **Refactor** : améliorer le code (et les tests) sans changer le comportement, en
  restant vert. C'est l'étape la plus souvent sautée — et celle qui produit le design.
- **Petits pas** : un comportement à la fois. Bloqué ? Le pas était trop grand.
- **Liste de tests** : noter les scénarios avant de coder, commencer par le plus simple.
- **Triangulation** : ajouter un deuxième exemple pour forcer la généralisation quand
  le code minimal est « en dur ».
- Deux écoles : **Chicago / classique** (inside-out, on vérifie l'état, peu de mocks)
  et **Londres / mockiste** (outside-in, on vérifie les interactions). En pratique :
  classique pour le domaine, mocks aux frontières (ports).

**Pourquoi.** Filet de sécurité pour refactorer, design qui émerge de l'usage (le test
est le premier client de l'API), documentation exécutable, beaucoup moins de debug.

**Le piège : tester l'implémentation au lieu du comportement.** Des tests qui cassent
au moindre refactoring, souvent à cause de trop de mocks. Un bon test casse quand le
*comportement* change, pas quand le *code* change.

**Questions probables.**
- **Q.** Pourquoi voir le test échouer d'abord ?
  **R.** Pour prouver qu'il teste quelque chose. Un test qui ne peut pas échouer ne
  protège de rien.
- **Q.** Le TDD ralentit-il ?
  **R.** Un peu à court terme. À moyen terme : moins de régressions, moins de debug,
  et la confiance pour refactorer. Le gain principal est le design.
- **Q.** Que faut-il mocker ?
  **R.** Ce qui est lent, externe ou non déterministe, derrière nos propres
  interfaces (ports : repository, horloge, API tierce). Jamais les value objects ni le
  domaine. « Don't mock what you don't own » : on mocke ses ports, pas une
  bibliothèque tierce directement.

---

## 4. Stratégie de tests

- **Pyramide** (Mike Cohn) : beaucoup de tests unitaires (rapides, isolés), moins de
  tests d'intégration, très peu de bout-en-bout.
- **Unitaire ≠ une classe** : l'unité est un *comportement*. Un test peut traverser
  plusieurs classes du domaine.
- **Intégration avec une vraie base** (Testcontainers, même moteur qu'en production)
  plutôt que H2 : dialecte SQL différent, faux positifs.
- **Tests de tranche Spring** : `@WebMvcTest` (couche web seule), `@DataJpaTest`
  (persistance seule) ; `@SpringBootTest` charge tout, c'est plus lent.
- **Structure** : Given / When / Then. Un comportement par test. Un nom qui décrit le
  comportement métier (`refuse_un_retrait_superieur_au_solde`).
- **F.I.R.S.T.** : Fast, Independent, Repeatable, Self-validating, Timely.
- **Déterminisme** : l'heure et l'aléatoire s'injectent (`Clock`, générateur d'ID).
- **Couverture** : elle dit ce qui n'est **pas** testé ; elle ne dit pas si ce qui est
  exécuté est vérifié (un test sans assertion donne de la couverture). Les **tests de
  mutation** (PIT) modifient le code (inversent un `>`) : si aucun test ne casse, les
  tests sont faibles.

**Le piège : les tests instables (flaky).** Ils dépendent de l'ordre, de l'heure ou du
réseau ; l'équipe finit par ignorer le rouge, et la suite de tests ne protège plus rien.

**Questions probables.**
- **Q.** 100 % de couverture, c'est bien testé ?
  **R.** Non. La couverture mesure l'exécution, pas la vérification. Elle sert à
  trouver les trous ; la qualité des assertions se mesure par mutation.
- **Q.** Pourquoi pas H2 pour les tests ?
  **R.** Ce n'est pas le moteur de production : comportements et SQL différents
  (types, contraintes, fonctions). Testcontainers donne le vrai PostgreSQL.
- **Q.** Pourquoi vérifier l'état de l'objet après une exception, et pas seulement
  l'exception ?
  **R.** Ce sont deux comportements distincts. Un code qui muterait *puis* lèverait
  l'exception passerait un test qui n'assert que le type d'exception, tout en laissant
  l'agrégat incohérent. On vise la **garantie forte face aux exceptions**
  (atomicité : soit l'opération réussit, soit l'objet est exactement dans son état
  d'avant), ce qui impose de valider **avant** de muter. Ne pas compter sur le rollback
  transactionnel : il protège la base, pas l'objet en mémoire que l'appelant continue
  d'utiliser s'il attrape l'exception. C'est aussi l'assertion que cherche un test de
  mutation : sans elle, déplacer le `throw` ne casse aucun test.

---

## 5. Code propre et principes

**SOLID, en une ligne chacun :**
- **S** — Responsabilité unique : une classe n'a qu'une raison de changer.
- **O** — Ouvert/fermé : on ajoute un comportement sans modifier le code existant
  (polymorphisme, stratégie). Exemple : ajouter un type de livret.
- **L** — Liskov : un sous-type remplace son type parent sans surprise pour
  l'appelant.
- **I** — Ségrégation des interfaces : de petites interfaces ciblées plutôt qu'une
  interface fourre-tout.
- **D** — Inversion des dépendances : dépendre d'abstractions définies par le code de
  haut niveau. C'est le cœur de l'hexagonal.

**Autres principes :**
- **YAGNI** (pas de code « au cas où »), **KISS**.
- **DRY, avec son piège** : une mauvaise abstraction coûte plus cher qu'une
  duplication. Deux codes identiques qui évoluent pour des raisons différentes ne sont
  pas une duplication.
- **Tell, don't ask** : `livret.deposer(montant)` plutôt que
  `if (livret.getSolde() …) livret.setSolde(…)`.
- **Loi de Déméter** : éviter `a.getB().getC().faire()`.
- **Immuabilité par défaut** : `final`, records, pas de setters.
- **Nommage** : le nom porte l'intention métier. Une fonction fait une chose.
- **Commentaires** : le pourquoi, jamais le quoi.

**Code smells courants** : méthode longue, classe trop grosse, **obsession des
primitives** (un `BigDecimal` qui circule partout au lieu d'un `Montant`), *feature
envy* (une méthode qui utilise surtout les données d'une autre classe), longue liste de
paramètres, *shotgun surgery* (un changement oblige à toucher dix fichiers).

**Refactoring** (Fowler) : changer la structure sans changer le comportement, par
petites étapes, sous couvert de tests. *Boy scout rule* : laisser le code un peu plus
propre qu'on l'a trouvé.

**Questions probables.**
- **Q.** Un exemple d'obsession des primitives ?
  **R.** Passer un `BigDecimal` pour un montant : rien n'empêche un montant négatif ou
  trois décimales. Un value object `Montant` porte la règle une fois pour toutes.
- **Q.** Comment aborder un code legacy sans tests ?
  **R.** Poser des tests de caractérisation (qui figent le comportement actuel, même
  bizarre), puis refactorer par petites étapes ; remplacer progressivement (*strangler
  fig*) plutôt que tout réécrire.

---

## 6. Java moderne (17 → 21)

- **Records** : classes de données immuables, `equals`/`hashCode`/`toString`
  générés. Parfaits pour les value objects et les DTO ; la validation se fait dans le
  constructeur compact.
- **Interfaces scellées** (`sealed`) : hiérarchie fermée, le compilateur connaît tous
  les cas (types de livret, résultats, événements).
- **Pattern matching pour `switch`** + record patterns : un `switch` exhaustif sur un
  type scellé n'a pas besoin de `default`, donc ajouter un cas **casse la
  compilation** partout où il doit être traité.
- **Virtual threads** (Java 21) : threads très légers pour les I/O bloquantes, donc
  beaucoup de requêtes concurrentes sans programmation réactive
  (`spring.threads.virtual.enabled=true`). Ils n'accélèrent pas le calcul CPU.
- **Argent : jamais `double`** (0.1 + 0.2 ≠ 0.3). `BigDecimal` avec échelle et mode
  d'arrondi explicites, ou montant en centimes (`long`). Attention :
  `BigDecimal.equals` compare aussi l'échelle (`2.0` ≠ `2.00`) → `compareTo`.
- **Exceptions métier** : non vérifiées (unchecked), nommées dans le langage métier
  (`PlafondDepasseException`). Distinguer une **valeur invalide** (un montant négatif
  n'existe pas → `IllegalArgumentException` dans le value object, futur HTTP 400) d'une
  **règle métier refusée** (un versement nul est un montant valide, mais refusé par le
  livret → exception métier, futur HTTP 422).
- **`Optional`** : en type de retour pour « peut être absent » ; pas en paramètre, pas
  en champ.
- **Contrat `equals`/`hashCode`** : deux objets égaux ont le même `hashCode`, sinon
  `HashSet`/`HashMap` se comportent mal.
- **Actualité** : Java 25 est la LTS depuis septembre 2025. Spring Boot 4 / Spring
  Framework 7 (fin 2025) : Jackson 3, null-safety avec JSpecify, starters plus
  modulaires.

**Questions probables.**
- **Q.** Pourquoi un record pour un value object ?
  **R.** Immuable et égalité par valeur sans code à écrire ; la validation dans le
  constructeur compact garantit qu'aucune instance invalide n'existe.
  Ce qu'il ne fait pas : il n'encapsule pas (les accesseurs sont publics) et si un
  composant est mutable (`List`, `Date`), l'immuabilité n'est que de façade — il faut
  copier dans le constructeur compact *et* à la lecture.
- **Q.** Pourquoi normaliser l'échelle d'un `BigDecimal` dans un value object ?
  **R.** `BigDecimal.equals` compare la valeur **et** l'échelle : `100` ≠ `100.00`.
  Sans normalisation, deux montants qui représentent le même argent ne sont pas égaux,
  et le value object devient inutilisable comme clé de `HashMap`. On rend la
  représentation **canonique** à la construction plutôt que d'écrire un `equals`
  manuel basé sur `compareTo` — qui ferait perdre l'intérêt du record. Règle générale :
  normaliser à la construction (trim d'une chaîne, casse d'un e-mail, échelle d'un
  montant), pour que « égal » soit vrai une fois pour toutes.
- **Q.** Pourquoi pas `double` pour de l'argent ?
  **R.** Représentation binaire approximative : erreurs d'arrondi cumulées. `BigDecimal`
  est exact en décimal, avec un arrondi qu'on choisit explicitement.

---

## 7. Spring et Spring Boot : la mécanique

- **Inversion de contrôle** : Spring crée les objets (*beans*) et les relie ; le
  conteneur est l'`ApplicationContext`.
- **Injection par constructeur** (recommandée) : dépendances `final`, objet valide dès
  sa construction, testable sans Spring (`new Service(fauxRepository)`), et un
  constructeur à huit paramètres signale une responsabilité de trop. Éviter
  `@Autowired` sur un champ.
- **Scope singleton par défaut** : une seule instance partagée par tous les threads,
  donc un bean doit être **sans état mutable**.
- **Auto-configuration** : Spring Boot configure des beans selon le classpath et les
  propriétés (`@ConditionalOnClass`, `@ConditionalOnMissingBean`…). Déclarer son
  propre bean remplace celui par défaut. Pour comprendre ce qui a été configuré :
  démarrer avec `--debug` (rapport des conditions).
- **Configuration externalisée** : `application.yml`, profils, variables
  d'environnement ; `@ConfigurationProperties` typé plutôt que des `@Value` éparpillés.
  Les secrets ne vont jamais dans le dépôt.
- **Proxies** : `@Transactional`, `@Cacheable`, `@Async` et la sécurité par méthode
  fonctionnent parce que Spring enveloppe le bean dans un proxy qui intercepte les
  appels. Conséquence majeure : **l'auto-invocation** (`this.autreMethode()`) ne passe
  pas par le proxy, donc l'annotation est ignorée.

### `@Transactional`
- **Où** : sur le cas d'usage (couche application). Une transaction = une opération
  métier complète.
- **Rollback par défaut** sur `RuntimeException` et `Error` uniquement, **pas** sur les
  exceptions vérifiées.
- `readOnly = true` pour les lectures (Hibernate évite la détection de modifications).
- **Propagation** : `REQUIRED` par défaut (rejoint la transaction en cours) ;
  `REQUIRES_NEW` en ouvre une nouvelle (un journal d'audit qui doit survivre à un
  rollback).
- Pas d'appel réseau lent (API externe, broker) dans une transaction : la connexion à
  la base reste bloquée pendant ce temps.

**Questions probables.**
- **Q.** Mon `@Transactional` n'a aucun effet, pourquoi ?
  **R.** Appel depuis la même classe (auto-invocation, le proxy est contourné), méthode
  privée, exception vérifiée (pas de rollback), ou objet créé avec `new` (pas un bean).
- **Q.** `@Component`, `@Service`, `@Repository` : quelle différence ?
  **R.** Sémantique pour les deux premiers. `@Repository` ajoute la traduction des
  exceptions de persistance en `DataAccessException`.
- **Q.** Comment Spring Boot sait-il créer une `DataSource` ?
  **R.** Auto-configuration conditionnelle : un driver JDBC présent dans le classpath et
  des propriétés `spring.datasource.*` suffisent ; si je déclare ma propre
  `DataSource`, la sienne s'efface.

---

## 8. Persistance : JPA / Hibernate

- **Contexte de persistance** : cache de premier niveau, le temps d'une transaction.
  Une entité chargée est *managed* ; ses modifications sont détectées (**dirty
  checking**) et écrites au commit, sans appel à `save()`.
- **Lazy vs eager** : `@ManyToOne` est eager par défaut (à passer en `LAZY`),
  `@OneToMany` est lazy. Règle : tout en lazy, et charger explicitement ce dont le cas
  d'usage a besoin.
- **Problème N+1** : charger N parents, puis déclencher une requête par parent pour sa
  collection. Solutions : `JOIN FETCH`, `@EntityGraph`, projection vers un DTO, taille
  de batch. Détection : log SQL, compteur de requêtes en test.
- **`LazyInitializationException`** : accès à une relation lazy hors transaction. La
  mauvaise solution est *Open Session In View*, **activé par défaut** dans Spring Boot
  (à désactiver : `spring.jpa.open-in-view=false`). La bonne : charger ce qu'il faut
  dans la transaction du cas d'usage.
- **Verrouillage optimiste** (`@Version`) : l'`UPDATE` vérifie que la version n'a pas
  bougé ; sinon exception → réessayer ou répondre 409. **Pessimiste**
  (`SELECT … FOR UPDATE`) : on bloque, pour les cas de forte contention.
- **Migrations** : Flyway, scripts versionnés ; **ne jamais modifier une migration
  déjà appliquée** (Flyway vérifie une somme de contrôle) ; `ddl-auto=validate`,
  jamais `update` en production.
- **Modèle JPA séparé du modèle de domaine** : le domaine ne subit pas les contraintes
  JPA. Coût : le mapping. Beaucoup d'équipes annotent directement le domaine par
  pragmatisme — il faut savoir défendre les deux.
- **JPA vs SQL typé (jOOQ)** : JPA pour écrire des agrégats, SQL pour les lectures
  complexes et le reporting.

**Questions probables.**
- **Q.** Qu'est-ce que le N+1 et comment le corriger ?
  **R.** Voir ci-dessus ; la correction dépend du besoin : `JOIN FETCH` pour charger
  l'agrégat, projection DTO pour un écran de lecture.
- **Q.** Optimiste ou pessimiste ?
  **R.** Optimiste quand les conflits sont rares (le cas général) : pas de verrou tenu,
  on détecte et on réessaie. Pessimiste quand les conflits sont fréquents et que
  réessayer coûte cher.

---

## 9. API REST

- **Ressources** au pluriel ; les **verbes HTTP** portent l'action. GET (lecture, sans
  effet, idempotent), POST (création ou action, non idempotent), PUT (remplacement,
  idempotent), PATCH (modification partielle), DELETE (idempotent).
- **Actions métier** : `POST /livrets/{id}/depots` — un dépôt est une sous-ressource
  créée.
- **Codes** : 200 ; 201 + en-tête `Location` ; 204 ; 400 (requête mal formée) ;
  **401** (non authentifié) vs **403** (authentifié mais non autorisé) ; 404 ; 409
  (conflit d'état, version) ; 422 (règle métier violée — débat avec 409 : choisir et
  rester cohérent) ; jamais de 500 volontaire.
- **DTO** en entrée et en sortie, jamais les entités : contrat stable, pas de fuite de
  champs internes, pas d'affectation de masse.
- **Validation** : Bean Validation (`@Valid`, `@NotNull`) pour la *forme* à la
  frontière ; les *règles métier* restent dans le domaine.
- **Erreurs** au format **Problem Details** (RFC 9457, qui remplace la RFC 7807) :
  `ProblemDetail` dans Spring, centralisé dans un `@RestControllerAdvice`.
- **Pagination**, **versioning** (`/v1` dans l'URL : simple et le plus répandu),
  **idempotence** des opérations sensibles via un en-tête `Idempotency-Key`.
- **Contrat** documenté en OpenAPI (springdoc).

**Questions probables.**
- **Q.** Pourquoi ne pas exposer directement l'entité ?
  **R.** Le contrat d'API serait couplé au modèle interne : chaque évolution casse les
  clients, on expose des champs sensibles, on risque les boucles de sérialisation et le
  lazy loading dans la vue.
- **Q.** Comment rendre un dépôt idempotent ?
  **R.** Le client envoie une clé d'idempotence ; le serveur la mémorise avec le
  résultat et renvoie ce même résultat si la clé revient.

---

## 10. Transactions et concurrence

Sujet central pour un domaine bancaire.

- **Mise à jour perdue** (*lost update*) : deux retraits simultanés lisent un solde de
  100, retirent 80 chacun → solde à −60 si rien ne protège. Protections : verrou
  optimiste (l'un des deux échoue et réessaie), verrou pessimiste, et une contrainte en
  base (`CHECK (solde >= 0)`) comme dernier filet.
- **ACID**, et le niveau d'isolation par défaut de PostgreSQL : *READ COMMITTED*. Il
  **ne protège pas** d'une mise à jour perdue en lecture-modification-écriture côté
  application.
- **Invariant entre plusieurs agrégats** (un seul Livret A par personne) : contrainte
  d'unicité en base, verrou sur le titulaire, ou cohérence à terme. Chaque option a un
  coût ; c'est une vraie question de conception.

---

## 11. Architecture événementielle

- **Pourquoi** : découpler les services (l'épargne ne connaît pas les notifications),
  absorber les pics, garder une trace.
- **Garanties de livraison** : au plus une fois, **au moins une fois** (le standard),
  exactement une fois (coûteux et limité). Conséquence : des consommateurs
  **idempotents** (dédoublonnage par identifiant d'événement).
- **Double écriture** : écrire en base puis publier sur Kafka ; un crash entre les
  deux crée une incohérence. **Pattern outbox** : écrire l'événement dans une table
  *outbox*, dans la même transaction que le changement métier ; un relais le publie
  ensuite (scrutation ou CDC type Debezium).
- **Kafka** : journal persistant, partitions (ordre garanti *par partition*, d'où la
  clé = identifiant du livret), groupes de consommateurs, relecture possible.
  **JMS / ActiveMQ** : file de messages, message supprimé après acquittement, routage
  riche, pas de relecture.
- **Échecs** : réessais, puis *dead letter topic/queue*.

---

## 12. Observabilité

- **Trois piliers** : logs (ce qui s'est passé), métriques (combien, à quelle
  vitesse), traces (le parcours d'une requête entre services).
- **Actuator** : `/actuator/health`, métriques, infos. **Liveness** (l'application
  est-elle vivante ? ne pas y inclure la base, sinon redémarrages en cascade quand elle
  tombe) vs **readiness** (peut-elle recevoir du trafic ?).
- **Micrometer** : façade de métriques (comme SLF4J pour les logs), exportées vers
  Prometheus. Méthode **RED** : Rate, Errors, Duration.
- **Logs structurés** (JSON, natif dans Spring Boot depuis la 3.4) avec un
  **identifiant de corrélation** (traceId, propagé par l'en-tête W3C `traceparent`,
  placé dans le MDC).
- Ne jamais logguer de données personnelles ni de secrets.

---

## 13. Sécurité

- **Authentification** (qui es-tu ?) vs **autorisation** (as-tu le droit ?).
- **JWT** : jeton *signé*, pas chiffré (le contenu est lisible par tous), sans état
  côté serveur. Révocation difficile → durée de vie courte et *refresh token*. En
  pratique, l'API est un *resource server* qui valide des jetons émis par un
  fournisseur d'identité (Keycloak, Entra ID…) via OAuth2 / OpenID Connect ; on
  n'écrit pas son propre émetteur de jetons.
- **OWASP API Top 10**, n°1 : **BOLA** (*Broken Object Level Authorization*) —
  `/livrets/42` consultable par un autre client que le titulaire. Il faut vérifier
  que la ressource appartient à l'appelant, pas seulement son rôle.
- Requêtes paramétrées (injection SQL), secrets hors du code, dépendances à jour
  (CVE), moindre privilège.
- **CSRF** concerne les sessions par cookie, pas une API sans état authentifiée par
  un jeton *Bearer*.

---

## 14. Livraison et pratiques d'équipe

- **Intégration continue** : chaque push déclenche build, tests et contrôles
  qualité ; un pipeline rouge bloque la fusion.
- **Petites PR**, revue de code centrée sur le design et le comportement ; le
  formatage est automatisé pour ne jamais en débattre.
- **Commits atomiques** (Conventional Commits), branches courtes / *trunk-based*.
- **Docker** : image *multi-stage* (compilation dans une image JDK, exécution dans une
  image JRE minimale), utilisateur non-root.
- **12-factor** : configuration par l'environnement, logs sur la sortie standard,
  processus sans état, parité entre dev et production.
- **Build reproductible** : tout ce qui influence le résultat est figé et versionné —
  version de l'outil de build (wrapper), JDK (toolchain), versions de dépendances,
  dépôts autorisés.

**Question ouverte fréquente.**
- **Q.** Monolithe ou microservices ?
  **R.** Monolithe modulaire par défaut. Les microservices se justifient par
  l'autonomie d'équipes nombreuses ou des besoins de montée en charge très différents,
  et se paient en complexité opérationnelle (réseau, cohérence à terme, observabilité,
  déploiement). Sans expérience de production en microservices, le dire et montrer
  qu'on comprend ces coûts vaut mieux que de prétendre.
