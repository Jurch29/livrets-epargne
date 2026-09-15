# Cheatsheet — révision avant entretien

Synthèse dense, alimentée à chaque étape. Les questions de contrôle posées pendant
le projet sont reprises ici avec leur réponse.

---

## Build et outillage (phase 0)

### Concepts clés

- **Gradle wrapper** : `gradlew` + `gradle/wrapper/gradle-wrapper.jar` + `.properties`.
  Permet de builder sans Gradle installé, à une version figée par le projet.
- **`distributionSha256Sum`** : le wrapper refuse d'exécuter une distribution dont
  le hash diffère de celui déclaré.
- **Toolchain Java** : `java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }`.
  Le JDK qui compile et exécute les tests est celui déclaré, pas celui qui lance Gradle.
  `sourceCompatibility` / `--release` ne contraignent que le niveau de langage / d'API.
- **Version catalog** (`gradle/libs.versions.toml`) : versions centralisées, accesseurs
  typés (`libs.assertj.core`). Remplace les propriétés `ext` non typées.
- **`repositoriesMode = FAIL_ON_PROJECT_REPOS`** : dépôts déclarés uniquement dans
  `settings.gradle.kts` ; un module qui en déclare un fait échouer le build.
- **`java-library` vs `java`** : `java-library` ajoute `api` (exposé aux consommateurs)
  vs `implementation` (caché). À utiliser pour tout module consommé par un autre.
- **BOM / `platform(...)`** : aligne les versions d'une famille d'artefacts
  (`platform(libs.junit.bom)` → junit-jupiter et junit-platform-launcher sans version).
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
`distributionSha256Sum`, et que ne protège-t-il pas ?**

On le commite parce que c'est lui qui télécharge Gradle. Sans lui dans le dépôt, il
faudrait Gradle installé pour… obtenir Gradle (~45 Ko, c'est acceptable).

Le checksum protège **la distribution téléchargée** (le zip de ~140 Mo) : miroir
compromis, proxy d'entreprise qui réécrit, corruption. Il ne protège **pas** :
- **le jar du wrapper lui-même**. C'est un binaire, illisible en revue de code : un jar
  trafiqué dans une PR exécute du code sur le poste et la CI avant toute vérification.
  Parade : l'action GitHub `gradle/actions/wrapper-validation`, qui compare le jar aux
  sommes officielles publiées par Gradle (phase 10) ;
- **une PR qui modifie l'URL et le checksum en même temps** : seule la revue le voit ;
- **les dépendances et les plugins** : c'est le rôle de la *dependency verification*
  (`gradle/verification-metadata.xml`), rarement mise en place en mission car lourde
  à maintenir.

**Q. Qu'est-ce qui distingue un ADR d'une documentation d'architecture ? Que fait-on
d'un ADR quand la décision change ?**

La documentation d'architecture décrit **l'état courant** (le quoi) ; elle évolue et
se périme. Un ADR (*Architecture Decision Record*, format popularisé par Michael
Nygard en 2011) capture **une décision datée** : le contexte à ce moment-là, la
décision, les alternatives écartées et les conséquences acceptées (le pourquoi).

Un ADR est **immuable** : on ne le réécrit pas. Si la décision change, on écrit un
nouvel ADR (« Remplace ADR-0001 ») et l'ancien passe au statut « Remplacé par
ADR-0007 ». On conserve ainsi l'historique du raisonnement. Deux ans plus tard, on
sait *pourquoi* un choix a été fait, donc si le contexte qui le justifiait a changé
et si on peut le remettre en cause.

Statuts usuels : Proposé, Accepté, Déprécié, Remplacé. Piège : en écrire pour tout
(bruit) ou jamais (décisions orales perdues au premier départ).

**Q. Un build doit être reproductible. Figer la version de Gradle règle une source
de variabilité ; quelles sont les autres ?**

- **Le JDK** : sans toolchain, on compile avec le JDK qui lance Gradle, donc celui du
  poste ou de l'image CI → toolchain.
- **Les versions de dépendances** : versions dynamiques (`1.+`, `latest.release`,
  intervalles) ou transitives qui bougent → versions fixes dans le catalogue ; au-delà,
  *dependency locking* (`gradle.lockfile`).
- **Les dépôts** : un module qui ajoute un dépôt tiers peut résoudre un artefact
  différent → `FAIL_ON_PROJECT_REPOS`.
- **Les versions des plugins Gradle**, pour la même raison que les dépendances.
- **L'environnement d'exécution** : fuseau horaire, locale, date du jour. Le test qui
  passe sur le poste et échoue en CI à minuit UTC ou un 31 décembre → horloge
  injectée (`java.time.Clock`) dans le code.
- **Le contenu des archives** : horodatages et ordre des fichiers dans les jars.
  Gradle 9 produit des archives reproductibles par défaut.
