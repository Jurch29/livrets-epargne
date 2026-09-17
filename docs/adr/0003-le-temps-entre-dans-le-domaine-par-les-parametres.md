# ADR 0003 — Le temps entre dans le domaine par les paramètres, pas par une horloge

- **Statut** : Accepté
- **Date** : 2026-09-17
- **Remplace** : la ligne « le temps est injecté (`java.time.Clock`) » de `CLAUDE.md`,
  ajustée en conséquence.

## Contexte

L'éligibilité au Livret Jeune dépend de l'âge du titulaire, donc d'une date de
référence. Le domaine ne doit jamais appeler `LocalDate.now()` : la règle serait
intestable et son résultat changerait selon le jour où la suite de tests tourne.

Reste à choisir *comment* le temps y entre. Aucune couche `application` n'existe encore
(phase 2), mais la décision oriente sa future signature.

## Décision

Le domaine reçoit des **dates**, jamais une source de temps :

```java
Livret.ouvrir(TypeLivret type, Titulaire titulaire, LocalDate dateDOuverture)
int Titulaire.ageLe(LocalDate date)
```

`Clock` restera un bean de la couche application, qui résout « aujourd'hui »
(`LocalDate.now(clock)`) et transmet la date au domaine. Le `Clock` ne franchit pas la
frontière du domaine.

La date d'ouverture est conservée par le livret : c'est une donnée métier, pas un
paramètre de passage.

## Alternatives écartées

- **`Clock` passée aux méthodes du domaine** (`ouvrir(type, titulaire, clock)`). Très
  répandu et parfaitement testable (`Clock.fixed`). Écarté parce que l'entité va alors
  chercher « maintenant » elle-même : on lui confie une décision qui appartient à
  l'appelant. Deux appels successifs dans une même opération métier peuvent tomber de
  part et d'autre de minuit ; avec une date passée, l'opération entière est datée une
  fois pour toutes.
- **`Clock` en champ de l'entité.** L'entité porterait une dépendance technique et
  deviendrait non sérialisable telle quelle ; la persistance aurait à la réinjecter.
- **Un port `FournisseurDeDate` défini par le domaine.** Correct en hexagonal, mais
  c'est une interface, une implémentation et un double de test pour obtenir ce qu'un
  paramètre donne directement. L'indirection n'est pas démontrée.
- **Calculer l'âge hors du domaine et lui passer un `int`.** La règle « comment se
  calcule un âge » sortirait du modèle, alors que c'est du métier — et le cas du
  29 février le prouve.

## Conséquences

- Les tests du domaine n'ont ni `Clock`, ni `Instant`, ni double : des `LocalDate`
  littérales, lisibles, et des cas limites écrits à la main (la veille de
  l'anniversaire).
- La couche application portera la responsabilité de dater l'opération, donc de choisir
  le fuseau (`Clock` en `Europe/Paris` : une opération à 23 h 30 le 31 décembre n'est
  pas de la même année en UTC).
- Écart assumé avec la règle initiale du projet, qui imposait `Clock` dans le domaine.
- Écart de calcul connu et figé par un test : `Period.between` fait vieillir une
  personne née un 29 février au 1er mars, quand l'usage juridique français retient le
  28 février. Corrigible par un calcul d'âge propre si une exigence le demande.
