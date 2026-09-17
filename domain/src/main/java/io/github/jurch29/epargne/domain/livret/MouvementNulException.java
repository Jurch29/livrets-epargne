package io.github.jurch29.epargne.domain.livret;

/**
 * Un montant ne pouvant pas être négatif, « nul » est le seul montant qu'un mouvement puisse porter à tort.
 */
public class MouvementNulException extends RuntimeException {

    public MouvementNulException() {
        super("Un mouvement doit porter sur un montant strictement positif");
    }
}
