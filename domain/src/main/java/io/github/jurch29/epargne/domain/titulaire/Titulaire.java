package io.github.jurch29.epargne.domain.titulaire;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * La personne qui détient un livret.
 *
 * <p>Réduit à sa date de naissance : aucune règle ne demande encore de nom ni d'identifiant. L'identité arrivera avec
 * l'unicité du Livret A par personne, qui en fera une entité.
 */
public record Titulaire(LocalDate dateDeNaissance) {

    public Titulaire {
        Objects.requireNonNull(dateDeNaissance, "dateDeNaissance");
    }

    /** Âge révolu à une date donnée : le domaine ne lit jamais l'heure courante, elle lui est fournie. */
    public int ageLe(LocalDate date) {
        if (date.isBefore(dateDeNaissance)) {
            throw new IllegalArgumentException("Date antérieure à la naissance : " + date);
        }
        return Period.between(dateDeNaissance, date).getYears();
    }
}
