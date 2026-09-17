package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;

/**
 * Les types de livret réglementés, et ce qui les distingue aujourd'hui : leur plafond.
 *
 * <p>Simplification : le plafond réel porte sur les versements, pas sur le solde — les intérêts capitalisés peuvent le
 * dépasser. La distinction capital / intérêts arrivera avec la rémunération.
 */
public enum TypeLivret {
    LIVRET_A(Montant.de("22950")),
    LDDS(Montant.de("12000")),
    LIVRET_JEUNE(Montant.de("1600"));

    private final Montant plafond;

    TypeLivret(Montant plafond) {
        this.plafond = plafond;
    }

    public Montant plafond() {
        return plafond;
    }
}
