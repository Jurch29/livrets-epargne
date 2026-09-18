package io.github.jurch29.epargne.demo.message;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * La logique métier. Aucune notion de HTTP ici : ni code de statut, ni requête, ni réponse.
 *
 * <p>Testable sans Spring : {@code new MessageService(unFauxRepository)}.
 */
@Service
public class MessageService {

    private final MessageRepository repository;

    // Injection par constructeur : champ final, objet valide dès sa création.
    MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public List<Message> tous() {
        return repository.tous();
    }

    public Message parId(long id) {
        return repository.parId(id).orElseThrow(() -> new MessageIntrouvableException(id));
    }

    public Message publier(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("Le texte d'un message ne peut pas être vide");
        }
        return repository.enregistrer(texte.strip());
    }
}
