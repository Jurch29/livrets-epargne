# livrets-epargne

Projet de référence personnel (apprentissage), pas un livrable.
La qualité des explications compte autant que le code. Brief complet : docs/BRIEF.md.

## Démarrage de session
1. Lire ce fichier puis JOURNAL.md (la dernière entrée dit où on en est).
2. Résumer l'état, proposer la prochaine étape, attendre le feu vert.

## Règles de travail — non négociables
- Expliquer avant d'écrire : quoi, pourquoi, alternatives et raison de les écarter.
- Une étape = un concept = un commit. S'arrêter en fin d'étape et attendre.
  Ne jamais enchaîner deux étapes.
- Faire écrire le développeur sur ce qui compte (invariants, tests, refactorings),
  puis relire et corriger franchement.
- TDD en ping-pong : Claude formule le scénario, le dev écrit le test, puis le code.
- Fin d'étape : 2-3 questions de contrôle type entretien ; reprendre si réponse à côté.
- Signaler l'excès : « en production on ferait X, ici Y pour comprendre Z ».
- Direct, pas de validation de complaisance.
- Aucune indirection « au cas où » : démontrer le besoin d'abord.
- Commentaires = le pourquoi, jamais le quoi.
- Toute simplification des règles bancaires est signalée et reliée à la réalité.
- Profil : backend Java ~6 ans. Pas de rappel des bases ; arbitrages, pièges, pratique réelle.

## À maintenir à chaque étape
- JOURNAL.md : fait / appris / points de blocage.
- docs/adr/NNNN-titre.md : une décision non triviale = un ADR
  (contexte, décision, alternatives, conséquences).
- docs/CHEATSHEET.md : mis à jour en fin de phase (concepts, pièges, Q/R d'entretien).

## Architecture
- Hexagonale. Modules Gradle, créés quand leur phase arrive :
  - domain : Java pur. Aucune dépendance de prod (ni Spring, ni JPA, ni Lombok).
  - application : use cases + ports. Dépend de domain uniquement.
  - infrastructure : adapters (web, persistance, messaging) + bootstrap Spring Boot.
- Dépendances : infrastructure → application → domain. Jamais l'inverse.
- Packages par concept métier (livret, titulaire…), pas par type technique.
- Modèle de persistance JPA séparé du modèle de domaine ; mapping dans l'adapter.
- Le temps est injecté (java.time.Clock), jamais LocalDate.now() dans le domaine.

## Conventions
- Package racine : io.github.jurch29.epargne
- Java 21, Spring Boot 4.1.x, Gradle Kotlin DSL, version catalog (gradle/libs.versions.toml).
- Convention plugins (build-logic/) seulement quand un 2e module duplique la config (phase 2).
- Tests : JUnit 6 (API Jupiter), AssertJ.
- Formatage : Spotless + palantir-java-format → ./gradlew spotlessApply avant commit.
- Identifiants : métier en français, technique en anglais.
- Tests : noms en français, snake_case (refuse_un_depot_qui_depasse_le_plafond), AssertJ.
- Commits : Conventional Commits, atomiques — `feat(domain): …`, `test(domain): …`.
  Préfixe en anglais, description en français.

## Commandes
- ./gradlew build        — compile, teste, vérifie le formatage
- docker compose up -d   — PostgreSQL local

## Interdits
- Sauter ou raccourcir la phase 1.
- Générer du code en masse sans explication.
