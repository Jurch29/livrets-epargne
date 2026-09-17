package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;

public class SoldeInsuffisantException extends RuntimeException {

    public SoldeInsuffisantException(Montant solde, Montant retrait) {
        super("Retrait de " + retrait.valeur() + " € impossible : le solde n'est que de " + solde.valeur() + " €");
    }
}
