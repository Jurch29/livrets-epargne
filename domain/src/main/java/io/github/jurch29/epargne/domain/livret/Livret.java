package io.github.jurch29.epargne.domain.livret;

import java.math.BigDecimal;

public final class Livret {

    private BigDecimal solde = BigDecimal.ZERO;

    private Livret() {}

    public static Livret ouvrir() {
        return new Livret();
    }

    public BigDecimal solde() {
        return solde;
    }

    public void deposer(BigDecimal montant) {
        solde = solde.add(montant);
    }
}
