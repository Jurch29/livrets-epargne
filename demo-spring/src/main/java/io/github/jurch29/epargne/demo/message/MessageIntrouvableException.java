package io.github.jurch29.epargne.demo.message;

/** Exception métier : non vérifiée, nommée dans le langage du domaine. Le web la traduira en 404. */
public class MessageIntrouvableException extends RuntimeException {

    public MessageIntrouvableException(long id) {
        super("Aucun message d'identifiant " + id);
    }
}
