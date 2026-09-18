package io.github.jurch29.epargne.demo.message;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Couche web : elle traduit du HTTP vers le métier et retour. Aucune règle métier ici.
 *
 * <p>On expose des DTO ({@link CreationDeMessage}) et jamais les entités : le contrat d'API reste stable même si le
 * modèle interne change.
 */
@RestController
@RequestMapping("/messages")
class MessageController {

    private final MessageService service;

    MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    List<Message> tous() {
        return service.tous();
    }

    @GetMapping("/{id}")
    Message parId(@PathVariable("id") long id) {
        return service.parId(id);
    }

    /** 201 + en-tête Location : la réponse dit où la ressource créée est consultable. */
    @PostMapping
    ResponseEntity<Message> publier(@RequestBody CreationDeMessage requete) {
        Message message = service.publier(requete.texte());
        return ResponseEntity.created(URI.create("/messages/" + message.id())).body(message);
    }
}
