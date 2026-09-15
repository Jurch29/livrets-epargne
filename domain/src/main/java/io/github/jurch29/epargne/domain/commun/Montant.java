package io.github.jurch29.epargne.domain.commun;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Somme d'argent en euros, au centime, jamais négative.
 */
public record Montant(BigDecimal valeur) {

    public static final Montant ZERO = new Montant(BigDecimal.ZERO);

    public Montant {
        Objects.requireNonNull(valeur, "valeur");
        if (valeur.signum() < 0) {
            throw new IllegalArgumentException("Un montant ne peut pas être négatif : " + valeur);
        }
        if (valeur.scale() > 2) {
            throw new IllegalArgumentException("Un montant a au plus deux décimales : " + valeur);
        }
        // Échelle normalisée : sans elle, 100 et 100.00 seraient deux montants différents,
        // car BigDecimal.equals compare aussi l'échelle.
        valeur = valeur.setScale(2);
    }

    /** Construction depuis une chaîne pour ne jamais transiter par un double. */
    public static Montant de(String valeur) {
        return new Montant(new BigDecimal(valeur));
    }

    public Montant ajouter(Montant autre) {
        return new Montant(valeur.add(autre.valeur));
    }

    public boolean estNul() {
        return valeur.signum() == 0;
    }
}
