package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;

public final class Livret {

    private Montant solde = Montant.ZERO;

    private Livret() {}

    public static Livret ouvrir() {
        return new Livret();
    }

    public Montant solde() {
        return solde;
    }

    public void deposer(Montant montant) {
        if (montant.estNul()) {
            throw new VersementInsuffisantException(montant);
        }
        solde = solde.ajouter(montant);
    }
}
