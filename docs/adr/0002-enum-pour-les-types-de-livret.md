# ADR 0002 — Un `enum` pour les types de livret

- **Statut** : Accepté
- **Date** : 2026-09-17

## Contexte

Le plafond de dépôt diffère selon le livret : 22 950 € pour le Livret A, 12 000 € pour
le LDDS, 1 600 € pour le Livret Jeune. C'est la première règle du domaine qui varie
selon le type de livret, donc la première fois que le type doit exister dans le modèle.

D'autres différences sont connues et arriveront : la tranche d'âge du Livret Jeune
(12–25 ans, étape 1.4), l'unicité du Livret A par personne (étape 1.5), et plus tard
des taux de rémunération distincts.

## Décision

Un `enum TypeLivret`, chaque constante portant son plafond sous forme de `Montant`. Le
type est obligatoire à l'ouverture (`Livret.ouvrir(TypeLivret)`) : un livret sans type
n'existe pas.

`Livret` interroge le type pour appliquer la règle — il ne connaît aucune valeur.

## Alternatives écartées

- **`sealed interface TypeLivret` + un record par type.** Le choix qui gagne dès que
  les types diffèrent par leurs **données** (une tranche d'âge que le Livret A n'a pas)
  ou par leur **comportement** (une règle d'éligibilité propre). Écarté *aujourd'hui* :
  les trois types ne diffèrent que par un nombre, et une hiérarchie de types pour
  porter un nombre est du cérémonial. À rouvrir à l'étape 1.4 — si l'éligibilité fait
  diverger les types autrement que par des valeurs, un ADR 0003 remplacera celui-ci.
- **Un champ `Montant plafond` dans `Livret`, sans type.** Supprime le concept de type
  de livret, qui existe pourtant dans le langage métier et servira aux règles à venir.
  Rien n'empêcherait alors d'ouvrir un livret avec un plafond arbitraire.
- **Une hiérarchie de classes `LivretA extends Livret`.** L'héritage d'entité fige le
  type pour la vie de l'objet et se marie mal avec la persistance (stratégies de mapping
  d'héritage, requêtes polymorphes). La composition suffit.
- **Un value object `Plafond`.** Un plafond *est* un montant ; l'envelopper n'ajoute
  aucune règle que `Montant` ne porte pas déjà. Indirection sans besoin démontré.

## Conséquences

- Ajouter un type de livret = ajouter une constante. `Livret` n'est pas modifié
  (principe ouvert/fermé), et un `switch` sur l'enum reste exhaustif par le compilateur.
- Les valeurs réglementaires sont dans le code plutôt que dans une configuration. Elles
  changent par décret (le taux du Livret A bouge plus souvent que son plafond) : en
  production, ces valeurs seraient **datées** et chargées depuis un référentiel, car un
  barème a une période de validité et l'historique doit rester recalculable. Simplifié
  ici tant que le temps n'est pas entré dans le modèle.
- Un enum se persiste par son nom (jamais par son `ordinal`, qui change si on réordonne
  les constantes) — à retenir pour la phase de persistance.
- La règle modélisée (« le solde ne dépasse pas le plafond ») est une simplification :
  le plafond réel porte sur les **versements**, et les intérêts capitalisés peuvent le
  dépasser. La distinction capital / intérêts devra apparaître avec la rémunération.
