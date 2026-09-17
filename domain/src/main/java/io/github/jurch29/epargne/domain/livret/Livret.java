package io.github.jurch29.epargne.domain.livret;

import io.github.jurch29.epargne.domain.commun.Montant;
import io.github.jurch29.epargne.domain.titulaire.Titulaire;
import java.time.LocalDate;
import java.util.Objects;

public final class Livret {

    private final TypeLivret type;
    private final Titulaire titulaire;
    private final LocalDate dateDOuverture;
    private Montant solde = Montant.ZERO;

    private Livret(TypeLivret type, Titulaire titulaire, LocalDate dateDOuverture) {
        this.type = type;
        this.titulaire = titulaire;
        this.dateDOuverture = dateDOuverture;
    }

    /**
     * La date d'ouverture est fournie par l'appelant : c'est un fait métier, et le domaine ne lit jamais l'heure
     * courante (ADR 0003).
     */
    public static Livret ouvrir(TypeLivret type, Titulaire titulaire, LocalDate dateDOuverture) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(titulaire, "titulaire");
        Objects.requireNonNull(dateDOuverture, "dateDOuverture");
        int age = titulaire.ageLe(dateDOuverture);
        if (!type.trancheDAge().contient(age)) {
            throw new AgeNonEligibleException(type, age);
        }
        return new Livret(type, titulaire, dateDOuverture);
    }

    public TypeLivret type() {
        return type;
    }

    public Titulaire titulaire() {
        return titulaire;
    }

    public LocalDate dateDOuverture() {
        return dateDOuverture;
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
