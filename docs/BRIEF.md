# Brief d'origine

Cahier des charges initial du projet, conservé tel quel. Les règles opérationnelles
condensées sont dans `CLAUDE.md`.

**Écarts décidés depuis :**
- Spring Boot 4.1.x au lieu de 3.x (2026-09-15)
- Modules Gradle créés au fil des phases plutôt que tous en phase 0 (2026-09-15)
- `docs/CHEATSHEET.md` scindé en deux niveaux dans `docs/revision/` : fondamentaux
  (prioritaire) et approfondissement (2026-09-15)
- Étapes 0.3 (formatage) et 0.4 (compose, README) reportées après l'entretien du
  2026-09-18 ; passage direct à la phase 1 (2026-09-15)

---

## Objectif

Je veux construire avec toi un projet Java backend qui me serve de référence
personnelle : un endroit où je consolide mes bases, où je révise les bonnes
pratiques, et que je peux relire avant un entretien ou une nouvelle mission.

Ce n'est PAS un projet à livrer. C'est un support d'apprentissage. La qualité
des explications compte autant que la qualité du code — souvent plus.

## Mon profil (calibre ton niveau d'explication là-dessus)

- ~6 ans de dev, dominante backend Java / Spring Boot
- Java 6 à 21, Spring Boot au quotidien sur mes missions
- Je pratique déjà l'architecture hexagonale, le DDD et le TDD sur un projet
  perso, mais de façon autodidacte : je veux valider et corriger mes réflexes
- À l'aise sur SQL, JPA/Hibernate, jOOQ, PostgreSQL, Kafka, ActiveMQ, Docker
- JUnit 5, Mockito, tests d'intégration
- Pas d'expérience de cloud managé ni de microservices opérés en production
- Je ne suis expert de rien en particulier, mais j'apprends vite

Donc : ne m'explique pas ce qu'est une interface ou une injection de
dépendances. Explique-moi les arbitrages, les pièges, ce qui se fait
réellement en production, et les endroits où ma pratique actuelle est
probablement approximative.

## Règles de fonctionnement — les plus importantes

1. **Tu expliques avant d'écrire.** Avant chaque bloc de code significatif,
   dis-moi : ce qu'on va faire, pourquoi, quelles alternatives existent et
   pourquoi on écarte les autres.

2. **Tu avances par petits pas.** Une étape = un concept = un commit. Tu
   t'arrêtes à la fin de chaque étape et tu attends que je te dise de
   continuer. Ne déroule jamais trois phases d'affilée.

3. **Tu me fais écrire du code.** Sur les points qui comptent (invariants
   métier, tests, refactorings), propose-moi de l'écrire d'abord et
   corrige-moi ensuite. Je veux pratiquer, pas regarder.

4. **Tu me challenges.** À la fin de chaque étape, pose-moi 2 ou 3 questions
   de contrôle — le type de questions qu'on pose en entretien technique. Si
   je réponds à côté, reprends le point.

5. **Tu signales ce qui est excessif.** Une partie de ce qu'on va faire est
   plus rigoureuse que ce qu'on voit sur une vraie mission. Dis-le
   explicitement quand c'est le cas : « en production, on ferait plutôt X,
   ici on fait Y pour comprendre le mécanisme ».

6. **Tu es direct.** Si mon code est mauvais, dis-le et explique pourquoi.
   Pas de validation de complaisance. Je préfère une correction franche à un
   encouragement.

7. **Tu documentes les décisions.** Chaque choix d'architecture non trivial
   donne lieu à un ADR court dans `docs/adr/` : contexte, décision,
   alternatives, conséquences.

8. **Tu tiens un journal.** `JOURNAL.md` à la racine : par étape, ce qu'on a
   fait, ce que j'ai appris, les points où j'ai buté. C'est ce que je relirai
   avant un entretien.

## Stack imposée

- Java 21, Gradle (Kotlin DSL)
- Spring Boot 3.x
- PostgreSQL + Flyway
- Spring Data JPA en premier ; on comparera avec jOOQ sur une partie ciblée
- JUnit 5, AssertJ, Mockito, Testcontainers, ArchUnit
- Docker + Docker Compose
- OpenAPI (springdoc), Actuator, Micrometer
- Plus tard : Spring Security + JWT, Kafka
- GitHub Actions pour la CI

## Domaine métier

**Gestion de livrets d'épargne réglementée.** Je choisis ce domaine parce que
les règles métier y sont réellement contraignantes, donc le DDD a un sens.

Le périmètre de départ :
- Un client peut détenir des livrets de plusieurs types (Livret A, LDDS,
  Livret Jeune), avec des règles de cumul entre eux
- Chaque type a un plafond de dépôt, un taux, des conditions d'éligibilité
  (âge pour le Livret Jeune, unicité par personne pour le Livret A)
- Dépôts et retraits, avec solde qui ne peut jamais être négatif
- Calcul des intérêts par quinzaine, capitalisation annuelle
- Clôture de livret

Tu peux simplifier les règles réelles, mais dis-moi à chaque fois où tu
simplifies et à quoi ça correspond dans la réalité bancaire. Le vocabulaire
métier m'intéresse autant que le code.

## Découpage en phases

Traite-les dans l'ordre, une par une, en attendant mon feu vert entre chaque.

**Phase 0 — Fondations**
Squelette Gradle, structure de modules, conventions de nommage, `.editorconfig`,
formatage, `CLAUDE.md` avec les règles du projet, README. Docker Compose avec
PostgreSQL.

**Phase 1 — Domaine pur, sans framework**
Entités, value objects, agrégats, invariants, exceptions métier. Aucune
dépendance Spring ni JPA dans cette couche. TDD strict : test d'abord,
toujours. C'est la phase la plus importante, prends le temps.

**Phase 2 — Cas d'usage et ports**
Use cases applicatifs, ports entrants et sortants, transactionnalité,
orchestration. Où passe la frontière exacte entre domaine et application.

**Phase 3 — Persistance**
Adapter JPA, mapping entités métier / entités de persistance (et pourquoi on
les sépare), Flyway, tests d'intégration avec Testcontainers. Pièges JPA :
N+1, lazy loading, cascade, verrouillage optimiste.

**Phase 4 — API REST**
Contrôleurs, DTOs, validation, gestion centralisée des erreurs (RFC 7807),
versioning, pagination, OpenAPI. Tests de contrôleurs.

**Phase 5 — Qualité et tests**
Pyramide des tests, ArchUnit pour verrouiller les règles d'architecture,
couverture avec JaCoCo, tests de mutation avec PIT. Ce que la couverture ne
dit pas.

**Phase 6 — Observabilité**
Actuator, health checks, métriques Micrometer, logs structurés, corrélation
des requêtes. Ce qu'on regarde vraiment en production.

**Phase 7 — Sécurité**
Spring Security, authentification JWT, autorisation par rôle, principaux
risques OWASP sur une API.

**Phase 8 — Événementiel**
Kafka, publication d'événements de domaine, pattern outbox, idempotence,
gestion des rejets. Comparaison avec ActiveMQ / JMS.

**Phase 9 — Données et performance**
Index, plans d'exécution, EXPLAIN ANALYZE, requêtes lourdes. Comparaison
ciblée JPA vs jOOQ sur un cas de reporting.

**Phase 10 — CI/CD**
GitHub Actions, build multi-stage Docker, qualité bloquante en pipeline.

**Phase 11 — Révision**
Refactorings classiques, code smells, concurrence, et une relecture critique
de tout ce qu'on a écrit avec le recul des phases suivantes.

## Livrables transverses

- `CLAUDE.md` — règles du projet, à relire au démarrage de chaque session
- `JOURNAL.md` — ce qu'on a fait et ce que j'ai appris, par étape
- `docs/adr/` — un fichier par décision d'architecture
- `docs/CHEATSHEET.md` — la synthèse dense que je relirai avant un entretien :
  concepts clés, pièges, questions types et leurs réponses
- Commits atomiques, messages en conventional commits

## Ce que je ne veux pas

- Du code généré en masse sans explication
- Des abstractions posées « au cas où » : on n'ajoute une indirection que
  quand un besoin réel la justifie, et tu me le démontres
- Des commentaires qui paraphrasent le code. Les commentaires expliquent le
  pourquoi, pas le quoi
- Qu'on saute la phase 1 pour aller plus vite au code Spring
- Que tu valides mes choix par principe
