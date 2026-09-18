package io.github.jurch29.epargne.demo.message;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * Implémentation en mémoire : de quoi faire tourner l'application sans base.
 *
 * <p>Un bean est un singleton partagé par tous les threads, donc l'état doit être sûr en concurrence — d'où la
 * {@link ConcurrentHashMap} et l'{@link AtomicLong} plutôt qu'une {@code HashMap} et un {@code long}.
 */
@Repository
class MessageEnMemoire implements MessageRepository {

    private final ConcurrentHashMap<Long, Message> messages = new ConcurrentHashMap<>();
    private final AtomicLong prochainId = new AtomicLong(1);

    @Override
    public List<Message> tous() {
        return List.copyOf(messages.values());
    }

    @Override
    public Optional<Message> parId(long id) {
        return Optional.ofNullable(messages.get(id));
    }

    @Override
    public Message enregistrer(String texte) {
        Message message = new Message(prochainId.getAndIncrement(), texte);
        messages.put(message.id(), message);
        return message;
    }
}
