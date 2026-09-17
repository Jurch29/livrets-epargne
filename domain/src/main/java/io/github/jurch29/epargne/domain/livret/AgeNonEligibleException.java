package io.github.jurch29.epargne.domain.livret;

public class AgeNonEligibleException extends RuntimeException {

    public AgeNonEligibleException(TypeLivret type, int age) {
        super("Ouverture refusée : " + age + " ans, hors de la tranche éligible pour un " + type);
    }
}
