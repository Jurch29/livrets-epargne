package io.github.jurch29.epargne.domain.livret;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jurch29.epargne.domain.commun.Montant;
import org.junit.jupiter.api.Test;

class LivretTest {

    @Test
    void un_livret_ouvert_a_un_solde_nul() {
        Livret livret = Livret.ouvrir();

        assertThat(livret.solde()).isEqualTo(Montant.ZERO);
    }

    @Test
    void un_depot_augmente_le_solde_du_montant_depose() {
        Livret livret = Livret.ouvrir();

        livret.deposer(Montant.de("100"));

        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void deux_depots_successifs_s_additionnent() {
        Livret livret = Livret.ouvrir();

        livret.deposer(Montant.de("100"));
        livret.deposer(Montant.de("50.25"));

        assertThat(livret.solde()).isEqualTo(Montant.de("150.25"));
    }
}
