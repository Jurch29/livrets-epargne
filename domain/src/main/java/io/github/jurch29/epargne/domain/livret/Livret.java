package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;
import java.util.Objects;

public final class Livret {

    private final TypeLivret type;
    private Montant solde = Montant.ZERO;

    private Livret(TypeLivret type) {
        this.type = type;
    }

    public static Livret ouvrir(TypeLivret type) {
        return new Livret(Objects.requireNonNull(type, "type"));
    }

    public TypeLivret type() {
        return type;
    }

    public Montant solde() {
        return solde;
    }

    public void deposer(Montant montant) {
        exigerMouvementPositif(montant);
        // Le solde visé est calculé à part : tant qu'il n'est pas validé, le livret n'a pas bougé.
        Montant soldeVise = solde.ajouter(montant);
        if (type.plafond().estInferieurA(soldeVise)) {
            throw new PlafondDepasseException(type, soldeVise);
        }
        solde = soldeVise;
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
