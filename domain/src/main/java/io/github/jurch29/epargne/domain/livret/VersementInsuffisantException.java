package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;

public class VersementInsuffisantException extends RuntimeException {

    public VersementInsuffisantException(Montant montant) {
        super("Le montant d'un versement doit être strictement positif : " + montant.valeur() + " €");
    }
}
