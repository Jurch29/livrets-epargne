# Niveau 2 — Approfondissement

> Détails, pièges, concepts creusés et questions pointues. **Pas prioritaire pour un
> entretien** : à explorer une fois le niveau 1 (`1-fondamentaux.md`) maîtrisé.
> Les questions de contrôle de niveau 2 posées pendant le projet sont reprises ici
> avec leur réponse.

---

## Build et outillage (phase 0)

### Concepts

- **Gradle wrapper** : `gradlew` + `gradle/wrapper/gradle-wrapper.jar` + `.properties`.
  Permet de builder sans Gradle installé, à une version figée par le projet.
- **`distributionSha256Sum`** : le wrapper refuse d'exécuter une distribution dont
  le hash diffère de celui déclaré.
- **Toolchain Java** : `java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }`.
  Le JDK qui compile et exécute les tests est celui déclaré, pas celui qui lance Gradle.
- **Version catalog** (`gradle/libs.versions.toml`) : versions centralisées, accesseurs
  typés (`libs.assertj.core`). Remplace les propriétés `ext` non typées.
- **`repositoriesMode = FAIL_ON_PROJECT_REPOS`** : dépôts déclarés uniquement dans
  `settings.gradle.kts` ; un module qui en déclare un fait échouer le build.
- **JUnit 6** (2025) : même API Jupiter que JUnit 5 (`org.junit.jupiter.*`), exige
  Java 17+, versions alignées entre Platform, Jupiter et Vintage.

### Pièges

- `gradlew` en CRLF (clone Windows avec `autocrlf`) → « bad interpreter » sur la CI
  Linux. Parade : `.gitattributes` avec `gradlew text eol=lf`.
- Gradle 9 ne fournit plus implicitement `junit-platform-launcher` : sans
  `testRuntimeOnly(...)`, les tests ne se lancent pas.
- `buildSrc` : toute modification invalide le cache de tout le build. Préférer un
  *included build* `build-logic/` pour les convention plugins.
- `allprojects {}` / `subprojects {}` : configuration croisée entre projets, couplage
  implicite, incompatible avec les optimisations récentes de Gradle. Préférer les
  convention plugins.

### Questions / réponses

**Q. Pourquoi commite-t-on `gradle-wrapper.jar`, un binaire ? Que protège exactement
`distributionSha256Sum`, et que ne protège-t-il pas ?** *(étape 0.1)*

On le commite parce que c'est lui qui télécharge Gradle. Sans lui dans le dépôt, il
faudrait Gradle installé pour… obtenir Gradle (~45 Ko, c'est acceptable).

Le checksum protège **la distribution téléchargée** (le zip de ~140 Mo) : miroir
compromis, proxy qui réécrit, corruption. Il ne protège **pas** :
- **le jar du wrapper lui-même**. C'est un binaire, illisible en revue de code : un jar
  trafiqué dans une PR exécute du code sur le poste et la CI avant toute vérification.
  Parade : l'action GitHub `gradle/actions/wrapper-validation`, qui compare le jar aux
  sommes officielles publiées par Gradle (phase 10) ;
- **une PR qui modifie l'URL et le checksum en même temps** : seule la revue le voit ;
- **les dépendances et les plugins** : c'est le rôle de la *dependency verification*
  (`gradle/verification-metadata.xml`), rarement mise en place en mission car lourde
  à maintenir.

**Q. Qu'est-ce qui distingue un ADR d'une documentation d'architecture ? Que fait-on
d'un ADR quand la décision change ?** *(étape 0.1 — la version courte est au niveau 1)*

La documentation d'architecture décrit **l'état courant** (le quoi) ; elle évolue et
se périme. Un ADR (*Architecture Decision Record*, format popularisé par Michael
Nygard en 2011) capture **une décision datée** : le contexte à ce moment-là, la
décision, les alternatives écartées et les conséquences acceptées (le pourquoi).

Un ADR est **immuable** : si la décision change, on écrit un nouvel ADR (« Remplace
ADR-0001 ») et l'ancien passe au statut « Remplacé par ADR-0007 ». On conserve ainsi
l'historique du raisonnement : deux ans plus tard, on sait *pourquoi* un choix a été
fait, donc si le contexte qui le justifiait a changé.

Statuts usuels : Proposé, Accepté, Déprécié, Remplacé. Piège : en écrire pour tout
(bruit) ou jamais (décisions orales perdues au premier départ).

**Q. Un build doit être reproductible. Figer la version de Gradle règle une source
de variabilité ; quelles sont les autres ?** *(étape 0.1)*

- **Le JDK** : sans toolchain, on compile avec le JDK qui lance Gradle → toolchain.
- **Les versions de dépendances** : versions dynamiques (`1.+`, `latest.release`,
  intervalles) ou transitives qui bougent → versions fixes dans le catalogue ; au-delà,
  *dependency locking* (`gradle.lockfile`).
- **Les dépôts** : un module qui ajoute un dépôt tiers peut résoudre un artefact
  différent → `FAIL_ON_PROJECT_REPOS`.
- **Les versions des plugins Gradle**, pour la même raison.
- **L'environnement d'exécution** : fuseau horaire, locale, date du jour. Le test qui
  passe sur le poste et échoue en CI à minuit UTC ou un 31 décembre → horloge injectée
  (`java.time.Clock`).
- **Le contenu des archives** : horodatages et ordre des fichiers dans les jars.
  Gradle 9 produit des archives reproductibles par défaut.

**Q. Dans `java-library`, différence entre `api` et `implementation` ? Si
`application` expose des types du domaine dans ses ports, comment déclarer la
dépendance à `domain` ?** *(étape 0.2)*

- `implementation` : la dépendance sert au module, mais n'est **pas visible** à la
  compilation de ses consommateurs. Encapsulation, et recompilation limitée quand elle
  change.
- `api` : la dépendance fait partie de l'interface publique du module ; elle est
  transmise au classpath de compilation des consommateurs.

Règle : **si un type d'une dépendance apparaît dans une signature publique** (paramètre,
retour, super-type), c'est `api`. Si `application` expose `Livret` dans un port et
déclare `domain` en `implementation`, `infrastructure` échoue à compiler (« cannot
access Livret ») dès qu'elle appelle ce port. Alternative tout aussi saine :
`infrastructure` déclare elle-même `domain`, puisqu'elle en utilise directement les
types (mappers) — on déclare ce qu'on utilise.

**Q. Toolchain, `sourceCompatibility` et `options.release` : que contrôle chacun ?**
*(étape 0.2)*

- `sourceCompatibility` / `targetCompatibility` (`javac -source/-target`) : niveau de
  langage et version du bytecode, **mais on compile contre la bibliothèque du JDK
  utilisé**. Avec un JDK 25 et `-source 21`, un appel à une API apparue en Java 22
  compile, puis plante en production sur Java 21 (`NoSuchMethodError`).
- `options.release` (`javac --release 21`, depuis Java 9) : niveau de langage, bytecode
  **et** API limitée à celle de Java 21. Protège de ce piège.
- **Toolchain** : choisit le JDK qui compile, exécute les tests et lance l'application.
  Avec une toolchain 21, l'API est naturellement celle de 21, et les tests tournent sur
  la même version qu'en production. C'est l'option la plus complète.

**Q. Qu'est-ce qu'un BOM ? Différence entre `platform()` et `enforcedPlatform()` ?**
*(étape 0.2)*

Un **BOM** (*Bill of Materials*) est un POM qui ne contient que des versions
recommandées pour une famille d'artefacts testés ensemble (le BOM Spring Boot aligne
des centaines de bibliothèques). On déclare ensuite les dépendances sans version.

- `platform(...)` : les versions du BOM sont des **recommandations** qui participent à
  la résolution de conflits normale de Gradle (la plus haute version demandée gagne).
- `enforcedPlatform(...)` : les versions sont **imposées**, y compris à la baisse. Il
  peut forcer une bibliothèque sous la version qu'une autre exige (erreurs à
  l'exécution), et il s'impose aussi aux consommateurs d'une librairie qui l'utilise.
  À réserver à une application, pour un cas précis ; préférer `platform` + une
  contrainte ciblée.

Pour Spring Boot sous Gradle : le plugin `io.spring.dependency-management` reproduit
la sémantique Maven (le BOM l'emporte) ; l'alternative native est
`implementation(platform(SpringBootPlugin.BOM_COORDINATES))`.
