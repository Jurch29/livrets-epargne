# Niveau 1 — Fondamentaux

> **Ce qui se relit avant un entretien.** Six blocs courts : l'idée, pourquoi on le
> fait, le piège à citer. Rien de plus — le détail vit dans `2-approfondissement.md`
> et n'est pas nécessaire ici.
>
> Chaque bloc finit par des questions **sans réponse** : la réponse est juste au-dessus.
> Se tester fait retenir ; relire une réponse rédigée donne surtout l'illusion de savoir.
>
> Le matin même, ne relire que `0-memo.md`.

---

## 0. Ce qui est évalué

- **Le raisonnement plus que la mémoire.** Justifier un choix par un contexte et dire
  ce qu'on sacrifie. « Ça dépend » est une bonne réponse *si* on dit de quoi.
- **L'honnêteté.** « Je ne l'ai pas fait en production, voilà comment je raisonnerais »
  passe très bien. Inventer est rédhibitoire.
- **Avec un PC à apporter : tu vas coder.** Probablement un kata en binôme (Bank
  Account, FizzBuzz, Roman Numerals, Gilded Rose). On observe la taille de tes pas, ton
  nommage et ta communication — pas ta vitesse, ni le fait de finir.

---

## 1. Coder devant quelqu'un

**Avant de taper** : reformuler le besoin, poser deux ou trois questions sur les cas
limites, écrire la liste des cas à traiter, annoncer par lequel tu commences — le plus
simple.

**Pendant** : penser à voix haute (ils évaluent ce qu'ils entendent). Un test à la
fois. Rester vert le plus souvent possible. Nommer avec les mots du besoin. Si on te
propose autre chose : l'essayer, plutôt que la défendre contre.

**Le piège** : foncer en silence sur la solution complète. Trois cas traités proprement
et commentés valent mieux que dix cas expédiés.

→ *Te tester* : par quel cas je commence, et pourquoi ? qu'est-ce que je fais si je
suis bloqué cinq minutes ? si l'énoncé est ambigu ?

---

## 2. TDD et tests

**Le cycle.** Rouge (un test qui échoue *pour la bonne raison*) → vert (le code
**minimal**, quitte à écrire une constante) → refactor (améliorer sans changer le
comportement, en restant vert). Le refactor est l'étape qu'on saute, et c'est celle qui
produit le design.

**Pourquoi.** Un filet pour refactorer, un design piloté par l'usage (le test est le
premier client de l'API), une documentation exécutable, beaucoup moins de debug.

**Bonnes pratiques.** Petits pas (bloqué = le pas était trop grand) ; triangulation (un
second exemple force la généralisation) ; Given / When / Then ; un comportement par
test ; un nom qui décrit le comportement (`refuse_un_retrait_superieur_au_solde`).

**La pyramide.** Beaucoup d'unitaires rapides, moins d'intégration, très peu de
bout-en-bout. L'unité est un *comportement*, pas une classe. Mocker ce qui est lent,
externe ou non déterministe, derrière ses propres interfaces — jamais les objets valeur.

**Le piège** : tester l'implémentation (souvent : trop de mocks) → des tests qui cassent
au moindre refactoring. Un bon test casse quand le *comportement* change.

**La couverture** dit ce qui n'est pas testé, pas si ce qui s'exécute est vérifié (un
test sans assertion couvre). Ce qui mesure la qualité des assertions : les tests de
mutation.

→ *Te tester* : pourquoi voir le test échouer d'abord ? que faut-il mocker ? le TDD
ralentit-il ? 100 % de couverture, c'est bien testé ?

---

## 3. Conception objet et code propre

**Encapsuler le comportement plutôt qu'exposer les données** : `compte.deposer(montant)`
et non `getSolde()` / `setSolde()` — *Tell, don't ask*. Le défaut inverse est le
**modèle anémique** : des objets à getters/setters et toute la logique dans des
services ; plus rien n'empêche alors `setSolde(-100)`.

**Objet valeur** : pas d'identité, immuable, égal par valeur, **se valide à la
construction** (`Montant`, `Email`, `Iban`). Son absence donne l'**obsession des
primitives** : un `BigDecimal` ou une `String` qui circulent et que n'importe qui peut
rendre invalides. **Entité** : on la suit dans le temps, elle est égale par identifiant.

**Invariant** : une condition toujours vraie, garantie par l'objet lui-même. Valider
**avant** de muter, pour qu'une opération refusée laisse l'objet exactement dans son
état d'avant.

**SOLID en une ligne chacun.** S : une seule raison de changer. O : ajouter un
comportement sans modifier l'existant (polymorphisme). L : un sous-type remplace son
parent sans surprise. I : de petites interfaces ciblées. D : dépendre d'abstractions.

**YAGNI** (pas de code « au cas où »), **KISS**, et le **piège de DRY** : une mauvaise
abstraction coûte plus cher qu'une duplication ; deux codes identiques qui évolueront
pour des raisons différentes ne sont pas un doublon.

**Smells à citer** : méthode longue, obsession des primitives, *feature envy*, longue
liste de paramètres, *shotgun surgery*.

→ *Te tester* : le modèle anémique, quel est le problème concret ? entité ou objet
valeur, comment je tranche ? un exemple d'obsession des primitives ? quand DRY nuit ?

---

## 4. Java : ce qui compte

- **Immuabilité par défaut** : `final`, records, pas de setter. Un objet immuable est
  sûr entre threads et personne ne peut le casser après coup.
- **Records** : objets valeur et DTO immuables, `equals`/`hashCode` générés, validation
  dans le constructeur compact. Ils n'encapsulent pas (accesseurs publics) et ne
  protègent pas d'un composant mutable.
- **`equals`/`hashCode`** : deux objets égaux ont le même `hashCode`, sinon
  `HashSet`/`HashMap` se comportent mal.
- **Argent : jamais `double`** (0.1 + 0.2 ≠ 0.3). `BigDecimal` avec échelle et arrondi
  explicites, ou des centimes en `long`. Piège : `BigDecimal.equals` compare aussi
  l'échelle (`2.0` ≠ `2.00`) → `compareTo`, ou normaliser à la construction.
- **Exceptions** : non vérifiées, nommées dans le langage métier. Distinguer une
  **valeur invalide** (`IllegalArgumentException`, futur 400) d'une **règle métier
  refusée** (exception métier, futur 422).
- **`Optional`** en type de retour pour « peut être absent » ; pas en paramètre, pas en
  champ.
- **`sealed` + `switch` avec pattern matching** : hiérarchie fermée, switch exhaustif
  sans `default` → ajouter un cas casse la compilation là où il doit être traité.
- **Virtual threads** (21) : beaucoup d'I/O bloquantes concurrentes sans réactif ; ils
  n'accélèrent pas le calcul.
- **Actualité** : Java 25 LTS depuis septembre 2025 ; Spring Boot 4 / Framework 7.

→ *Te tester* : pourquoi un record pour un objet valeur ? pourquoi pas `double` pour de
l'argent ? une exception métier, vérifiée ou non ?

---

## 5. Spring Boot, vu de haut

- **Inversion de contrôle** : Spring crée les objets (*beans*) et les relie ; le
  conteneur est l'`ApplicationContext`.
- **Injection par constructeur** : champs `final`, objet valide dès sa construction,
  testable sans Spring (`new Service(faux)`), et huit paramètres signalent une
  responsabilité de trop. Éviter `@Autowired` sur un champ.
- **Singleton par défaut** : un bean ne doit pas porter d'état mutable.
- **Auto-configuration** : Spring Boot configure des beans d'après le classpath et les
  propriétés ; déclarer le tien remplace le sien.
- **Proxies** : `@Transactional`, `@Cacheable`, `@Async` fonctionnent parce que le bean
  est enveloppé. Donc un appel interne (`this.methode()`) ne passe pas par le proxy et
  l'annotation est ignorée — le grand classique d'entretien.
- **`@Transactional`** : sur le cas d'usage, une transaction = une opération métier.
  Rollback par défaut sur les exceptions **non vérifiées** seulement. `readOnly` en
  lecture. Pas d'appel réseau lent à l'intérieur.
- **Tester** : tranches (`@WebMvcTest`, `@DataJpaTest`) plutôt que `@SpringBootTest`
  qui charge tout. Le métier pur se teste sans Spring du tout.
- **Configuration** hors du code (environnement, profils) ; les secrets ne vont jamais
  dans le dépôt.

→ *Te tester* : pourquoi l'injection par constructeur ? mon `@Transactional` n'a aucun
effet, quelles causes possibles ? comment Spring sait-il créer une `DataSource` ?

---

## 6. Ce qu'il faut savoir situer

**Architecture.** En couches (controller → service → repository) : simple et répandu,
mais le métier dépend de la technique. **Hexagonale** : le métier au centre ne dépend de
rien, il déclare des **ports** (interfaces) que l'infrastructure implémente — inversion
de dépendance. Gain : tester le métier sans base ni Spring. Coût : des classes et du
mapping en plus, donc inutile sur du CRUD.

**JPA.** Une entité chargée est suivie : ses modifications partent au commit sans
`save()`. Le **N+1** (une requête par parent) se corrige par `JOIN FETCH`,
`@EntityGraph` ou une projection. Tout en `LAZY`, et charger explicitement ce dont le
cas d'usage a besoin. Migrations versionnées (Flyway), `ddl-auto=validate` — jamais
`update` en production.

**Concurrence.** Deux écritures simultanées sur la même ligne = **mise à jour perdue**.
Verrou **optimiste** (`@Version` : on détecte et on réessaie) quand les conflits sont
rares, ce qui est le cas général ; **pessimiste** quand ils sont fréquents. Une
contrainte en base reste le dernier filet.

**REST.** Ressources au pluriel, verbes HTTP pour l'action ; une action métier est une
sous-ressource créée (`POST /comptes/{id}/depots`). DTO en entrée et en sortie, jamais
l'entité (contrat couplé au modèle interne, champs exposés). Validation de *forme* à la
frontière, règles *métier* dans le domaine. Erreurs normalisées et centralisées.

**Équipe et livraison.** Petites PR, commits atomiques, CI qui bloque la fusion,
formatage automatisé pour ne jamais en débattre. Monolithe modulaire par défaut : les
microservices s'achètent au prix du réseau, de la cohérence à terme et de
l'exploitation.

→ *Te tester* : à quoi sert vraiment l'hexagonal, et quand s'en passer ? c'est quoi le
N+1 ? optimiste ou pessimiste ? pourquoi ne pas exposer l'entité en REST ?

---

## 7. Ce que ce projet illustre (pour raconter un choix)

Le domaine (livrets d'épargne) n'a aucune importance ici ; ce qui compte est d'avoir un
exemple vécu sous la main.

- **Objet valeur** — `Montant` : immuable, jamais négatif, deux décimales, échelle
  normalisée à la construction pour que `100` et `100.00` soient égaux.
- **Invariant dans l'entité** — `Livret.retirer()` valide *avant* de muter : un retrait
  refusé laisse le solde intact, sans compter sur le rollback transactionnel.
- **Valeur invalide vs règle refusée** — montant négatif → `IllegalArgumentException` ;
  retrait supérieur au solde → exception métier.
- **Refactor sous tests** — l'objet valeur a été extrait quand la règle est apparue,
  pas prévu « au cas où » (YAGNI).
- **Architecture vérifiée par la structure** — le module `domain` n'a aucune dépendance
  de production : une violation ne compile pas, il n'y a aucune discipline à tenir.
