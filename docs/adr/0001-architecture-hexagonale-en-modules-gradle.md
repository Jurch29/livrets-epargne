# ADR 0001 — Architecture hexagonale en modules Gradle

- **Statut** : Accepté
- **Date** : 2026-09-15

## Contexte

Le domaine (livrets d'épargne réglementée) porte des règles métier contraignantes
qu'on veut modéliser et tester sans aucun framework. La règle « le domaine ne dépend
ni de Spring ni de JPA » doit tenir dans la durée, y compris quand l'IDE propose
d'importer une annotation par auto-complétion.

Le projet est un support d'apprentissage : la frontière domaine / application /
infrastructure doit être lisible dans la structure même du dépôt.

## Décision

Trois modules Gradle, avec des dépendances à sens unique :

```
infrastructure  →  application  →  domain
```

- `domain` : Java pur, aucune dépendance de production.
- `application` : cas d'usage et ports ; dépend de `domain` uniquement.
- `infrastructure` : adapters (web, persistance, messaging) et démarrage Spring Boot.

Chaque module est créé à la phase où il devient nécessaire (`domain` en phase 0,
`application` en phase 2, `infrastructure` en phase 3), pas avant.

Dans chaque module, les packages sont organisés par concept métier (`livret`,
`titulaire`…), pas par type technique (`model`, `service`, `exception`).

## Alternatives écartées

- **Mono-module + packages + ArchUnit.** Le plus répandu en mission, et défendable.
  Mais la règle n'est vérifiée qu'à l'exécution des tests : un import Spring dans le
  domaine compile. Ici, le classpath l'interdit dès la compilation. ArchUnit reste
  utile à l'intérieur des modules (phase 5).
- **Modules fins** (`adapter-web`, `adapter-persistence`, `bootstrap`…). Isoler les
  adapters les uns des autres dans des modules n'apporte rien à cette échelle et
  alourdit le build.
- **Découpage par bounded context** (Spring Modulith, un module par contexte). Adapté
  à plusieurs contextes métier ; il n'y en a qu'un ici.
- **Module `core` unique (domaine + application).** Viable si la couche application
  reste sans Spring. La question de `@Transactional` dans les cas d'usage n'étant pas
  tranchée (phase 2), on garde le domaine séparé pour qu'il reste pur quoi qu'on
  décide.

## Conséquences

- Une dépendance interdite est une erreur de compilation, pas un test rouge.
- Plus de configuration de build qu'en mono-module ; la duplication entre modules
  sera factorisée par un convention plugin (`build-logic/`) quand le deuxième module
  apparaîtra.
- Plus rigoureux que la majorité des projets en production, qui s'en tiennent au
  mono-module + ArchUnit. Assumé pour rendre la frontière visible.
- La répartition exacte des packages du domaine est provisoire : l'emplacement des
  invariants portant sur plusieurs livrets (unicité du Livret A par personne) sera
  tranché en phase 1 et pourra la faire évoluer.
