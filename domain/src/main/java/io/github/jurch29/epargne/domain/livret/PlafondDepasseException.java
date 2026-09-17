package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;

public class PlafondDepasseException extends RuntimeException {

    public PlafondDepasseException(TypeLivret type, Montant soldeVise) {
        super("Dépôt refusé : il porterait le solde à " + soldeVise.valeur() + " € pour un plafond de "
                + type.plafond().valeur() + " € (" + type + ")");
    }
}
