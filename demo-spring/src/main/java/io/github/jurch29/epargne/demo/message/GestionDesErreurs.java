package io.github.jurch29.epargne.demo.message;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduction centralisée des exceptions métier en réponses HTTP : aucun try/catch dans les controllers.
 *
 * <p>{@link ProblemDetail} est le format normalisé des erreurs (RFC 9457).
 */
@RestControllerAdvice
class GestionDesErreurs {

    @ExceptionHandler(MessageIntrouvableException.class)
    ProblemDetail introuvable(MessageIntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail requeteInvalide(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
