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
        exigerMouvementPositif(montant);
        solde = solde.ajouter(montant);
    }

    public void retirer(Montant montant) {
        exigerMouvementPositif(montant);
        if (solde.estInferieurA(montant)) {
            throw new SoldeInsuffisantException(solde, montant);
        }
        solde = solde.soustraire(montant);
    }

    // Validation avant mutation : un mouvement refusé laisse le livret dans son état d'avant.
    private static void exigerMouvementPositif(Montant montant) {
        if (montant.estNul()) {
            throw new MouvementNulException();
        }
    }
}
