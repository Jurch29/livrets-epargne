package io.github.jurch29.epargne.demo.message;

import java.util.List;
import java.util.Optional;

/**
 * Le contrat dont le service a besoin, exprimé dans son vocabulaire.
 *
 * <p>Le service dépend de cette interface, pas de l'implémentation : on peut passer d'une map en mémoire à JPA sans
 * toucher au métier. {@code Optional} en retour dit « peut être absent » sans {@code null}.
 */
public interface MessageRepository {

    List<Message> tous();

    Optional<Message> parId(long id);

    Message enregistrer(String texte);
}
