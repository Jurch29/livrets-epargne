# Mémo — à relire le matin même

> Une page. Si tu ne lis qu'un fichier, c'est celui-là.

## Les cinq réflexes du kata

1. Reformuler le besoin, poser deux ou trois questions sur les cas limites.
2. Écrire la liste des cas, annoncer par lequel je commence — le plus simple.
3. Un test à la fois : rouge, vert, refactor. **Voir le rouge.**
4. Penser à voix haute ; nommer avec les mots du besoin.
5. Une proposition de l'autre : l'essayer, pas la défendre contre.

## Les phrases à savoir dire

- Le test est le premier client de l'API : le TDD produit du design, pas juste des tests.
- Le code minimal d'abord ; c'est le test suivant qui force la généralisation.
- Un bon test casse quand le comportement change, pas quand le code change.
- L'unité, c'est un comportement, pas une classe.
- *Tell, don't ask* : `compte.deposer(m)`, pas `setSolde(...)`. Sinon : modèle anémique.
- Un objet valeur se valide à la construction — la réponse à l'obsession des primitives.
- Valider avant de muter : une opération refusée laisse l'objet dans son état d'avant.
- Une mauvaise abstraction coûte plus cher qu'une duplication.
- Hexagonal : le métier ne dépend de rien, l'infra s'y branche. Coût : du mapping.
  Inutile sur du CRUD.
- Monolithe modulaire par défaut ; les microservices se paient en exploitation.

## Java — les réflexes

- Jamais `double` pour de l'argent → `BigDecimal`, et `compareTo` plutôt que `equals`.
- Immuable par défaut : `final`, records, pas de setter.
- Exceptions métier non vérifiées ; valeur invalide (400) ≠ règle refusée (422).
- `Optional` en retour, jamais en champ ni en paramètre.
- `sealed` + `switch` exhaustif : ajouter un cas casse la compilation.

## Spring Boot — les six lignes qui suffisent

- Le conteneur crée les beans et les relie ; injection **par constructeur**.
- Bean singleton → aucun état mutable.
- L'auto-configuration configure d'après le classpath ; mon bean remplace le sien.
- `@Transactional` passe par un **proxy** → un appel interne l'ignore.
- Rollback par défaut sur les exceptions **non vérifiées** seulement.
- Tranches de test (`@WebMvcTest`, `@DataJpaTest`) ; le métier pur se teste sans Spring.

## Trois pièges à citer spontanément

1. Trop de mocks → des tests qui cassent au moindre refactoring.
2. N+1 en JPA, et `open-in-view` laissé à `true`.
3. L'auto-invocation qui neutralise `@Transactional`.

## À assumer franchement

- Pas de production en microservices ni en événementiel : j'en connais les principes et
  les coûts, je ne prétends pas l'expérience.
- Je ne connais pas par cœur la liste des codes HTTP ni des annotations : je sais où
  chercher et je sais expliquer les choix.
- Face à une question ouverte : donner le compromis et dire sur quoi je trancherais.
