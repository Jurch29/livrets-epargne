# Mémo — à relire le matin même

> Une page. Si tu ne lis qu'un fichier, c'est celui-là.

## Si tu ne retiens que ça — six phrases

1. **`equals` et `hashCode` vont ensemble**, sinon l'objet disparaît de la `HashMap`.
2. **`==` compare les références**, `equals` compare le contenu. Jamais `==` sur des `String`.
3. **Jamais de `double` pour de l'argent** → `BigDecimal`, et `compareTo` plutôt que `equals`.
4. **`@Transactional` marche par proxy** → un appel interne l'ignore, et le rollback n'est
   automatique que sur les exceptions non vérifiées.
5. **Injection par constructeur** ; un bean singleton ne porte pas d'état mutable.
6. **On teste le comportement, pas l'implémentation.**

---

## Les réponses à savoir dire

> Formulées pour l'oral. Deux ou trois phrases, pas plus : on répond, on s'arrête, on
> laisse l'autre relancer.

**« Pourquoi redéfinir `equals` et `hashCode` ensemble ? »**
Parce qu'ils ont un contrat : deux objets égaux doivent avoir le même `hashCode`.
L'inverse n'est pas exigé — deux objets différents peuvent partager un hash, c'est une
collision, c'est normal.
*Relance « qu'est-ce qui casse ? »* → Une `HashMap` choisit le bucket d'après le
`hashCode`. Sans lui, deux objets égaux tombent dans deux buckets différents : je range
un objet, je le cherche avec un objet égal, il est introuvable.
*Relance « autre piège ? »* → Une clé **mutable** : si un champ change après l'insertion,
le hash change et la clé n'est plus dans le bon bucket. D'où des clés immuables, et des
`record` qui génèrent les deux méthodes correctement.

**« `==` ou `equals` ? »**
`==` compare les **références** — est-ce le même objet en mémoire. `equals` compare le
**contenu**, si la classe l'a redéfini. Sur des objets, toujours `equals` ; `==` ne vaut
que pour les primitifs et les enums.
*Relance « et les `String` ? »* → Les littéraux sont mis en commun dans le *pool*, donc
`"abc" == "abc"` est vrai, ce qui donne une fausse confiance. Dès qu'une chaîne vient
d'une saisie, d'une base ou d'un calcul, c'est faux.

**« Pourquoi pas `double` pour de l'argent ? »**
Parce qu'un `double` est binaire : 0,1 n'est pas représentable exactement, donc
`0.1 + 0.2` vaut `0.30000000000000004`. Sur des milliers d'opérations les erreurs
s'accumulent, et un centime d'écart en comptabilité, c'est un incident. J'utilise
`BigDecimal` avec échelle et arrondi explicites, ou des centimes en `long`. Piège :
`BigDecimal.equals` compare aussi l'échelle, donc `compareTo` — ou je normalise
l'échelle à la construction, comme dans mon objet `Montant`.

*Relance « des pièges avec `BigDecimal` ? »* → `equals` compare aussi l'échelle
(`2.0` != `2.00`) : `compareTo`, ou normaliser à la construction. Et `divide` sans mode
d'arrondi lève une exception sur un décimal infini, typiquement une division par 3.

**« `@Transactional`, ça fait quoi ? »**
Ça délimite une transaction autour de la méthode : soit tout est écrit, soit rien.
Spring l'obtient en enveloppant le bean dans un **proxy** qui ouvre la transaction avant
l'appel, et commit ou rollback après. Rollback par défaut sur les exceptions **non
vérifiées** seulement. Le piège classique : un appel interne (`this.autreMethode()`) ne
passe pas par le proxy, donc l'annotation est ignorée.

*Relance « mon `@Transactional` ne fait rien, pourquoi ? »* → Appel interne
(`this.methode()`, le proxy est contourné — la cause la plus fréquente) ; méthode privée ;
exception vérifiée (pas de rollback par défaut) ; objet créé avec `new`, donc pas un bean.

**« Vous testez comment ? »**
Je teste le **comportement observable** : ce que la méthode retourne, l'état de l'objet
après l'appel, l'exception levée. Pas l'implémentation — sinon le test casse au moindre
refactoring. Un comportement par test, un nom qui décrit ce comportement. Je mocke ce
qui est lent ou externe (base, API tierce), jamais les objets métier.

**« Pourquoi un domaine sans Spring ni JPA ? »**
Pour que les règles métier se testent sans démarrer le framework ni une base — dans mon
projet, 28 tests en deux secondes. Et pour que le métier ne soit pas déformé par la
technique : JPA réclame un constructeur vide et des setters, ce qui pousse au modèle
anémique. Le framework se remplace, les règles métier restent.

**« Une méthode de 200 lignes, vous la découpez comment ? »**
En méthodes nommées par leur intention, une seule chose chacune. Mon critère concret :
dès que j'ai besoin d'un commentaire pour expliquer un bloc, ce bloc est une méthode et
le commentaire est son nom. Et je le fais sous couvert de tests — sans tests, je ne
refactore pas, je réécris.

**« C'est quoi du bon code ? »**
Du code qu'un collègue comprend sans que je sois là pour l'expliquer, et qu'on peut
changer sans peur parce qu'il est testé. Le reste vient après, et seulement si un besoin
réel le demande.

**« Un projet sans aucun test, par où commencez-vous ? »**
Je le lance et je le fais tourner d'abord, pour comprendre le comportement réel. Ensuite
j'écris des **tests de caractérisation** : ils figent le comportement actuel, même s'il
est bizarre — leur but n'est pas de dire ce qui est juste, mais de m'alerter si je change
quelque chose. Après seulement je refactore, par petits pas.

**« Interface ou classe abstraite ? »**
L'interface est un contrat : elle laisse l'appelant dépendre du *quoi* et pas du
*comment*, donc je peux remplacer l'implémentation — par un double en test, par une
autre techno en production. La classe abstraite sert à partager du code commun, et on
n'hérite que d'une seule classe.

---

## Les cinq réflexes du kata

0. **Reformuler la question avant d'y répondre.** « Vous me demandez pourquoi il ne
   s'applique pas, c'est ça ? » — deux secondes, et on ne répond jamais à côté. Sous
   stress, le réflexe est de répondre à la question qu'on sait, pas à celle qui est posée.
   Si la question est vague, c'est souvent volontaire : cadrer à voix haute (« il y a
   deux angles, je prends celui-ci ? ») vaut mieux que deviner.
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
