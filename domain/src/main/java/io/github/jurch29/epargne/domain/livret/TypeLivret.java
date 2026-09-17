package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;
import io.github.jurch29.epargne.domain.titulaire.TrancheDAge;

/**
 * Les types de livret réglementés, et ce qui les distingue : plafond de dépôt et tranche d'âge éligible.
 *
 * <p>Les types ne diffèrent encore que par des valeurs — un plafond, une tranche d'âge — ce qui justifie l'enum
 * plutôt qu'une hiérarchie scellée (ADR 0002).
 *
 * <p>Simplification : le plafond réel porte sur les versements, pas sur le solde — les intérêts capitalisés peuvent le
 * dépasser. La distinction capital / intérêts arrivera avec la rémunération.
 */
public enum TypeLivret {
    LIVRET_A(Montant.de("22950"), TrancheDAge.TOUS_AGES),
    LDDS(Montant.de("12000"), TrancheDAge.aPartirDe(18)),
    // Simplification : le vrai Livret Jeune se ferme au 31 décembre de l'année des 25 ans.
    LIVRET_JEUNE(Montant.de("1600"), TrancheDAge.de(12, 25));

    private final Montant plafond;
    private final TrancheDAge trancheDAge;

    TypeLivret(Montant plafond, TrancheDAge trancheDAge) {
        this.plafond = plafond;
        this.trancheDAge = trancheDAge;
    }

    public Montant plafond() {
        return plafond;
    }

    public TrancheDAge trancheDAge() {
        return trancheDAge;
    }
}
