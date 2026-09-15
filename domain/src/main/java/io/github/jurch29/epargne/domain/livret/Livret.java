package io.github.jurch29.epargne.domain.livret;

import java.math.BigDecimal;

public final class Livret {

    private Livret() {}

    public static Livret ouvrir() {
        return new Livret();
    }

    public BigDecimal solde() {
        return BigDecimal.ZERO;
    }
}
