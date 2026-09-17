package io.github.jurch29.epargne.domain.titulaire;

/**
 * Intervalle d'âges éligibles, bornes incluses.
 *
 * <p>L'absence de borne haute est représentée par une sentinelle plutôt que par un {@code Integer} nullable : le record
 * reste comparable par valeur et aucun appelant n'a de {@code null} à traiter.
 */
public record TrancheDAge(int minimum, int maximum) {

    private static final int SANS_LIMITE = Integer.MAX_VALUE;

    public static final TrancheDAge TOUS_AGES = aPartirDe(0);

    public TrancheDAge {
        if (minimum < 0) {
            throw new IllegalArgumentException("Âge minimum négatif : " + minimum);
        }
        if (maximum < minimum) {
            throw new IllegalArgumentException("Tranche d'âge vide : " + minimum + " à " + maximum);
        }
    }

    public static TrancheDAge aPartirDe(int minimum) {
        return new TrancheDAge(minimum, SANS_LIMITE);
    }

    public static TrancheDAge de(int minimum, int maximum) {
        return new TrancheDAge(minimum, maximum);
    }

    public boolean contient(int age) {
        return age >= minimum && age <= maximum;
    }
}
