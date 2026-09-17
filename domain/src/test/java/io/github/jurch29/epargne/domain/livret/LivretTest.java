package io.github.jurch29.epargne.domain.livret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void refuse_un_versement_nul_et_laisse_le_solde_inchange() {
        Livret livret = Livret.ouvrir();
        livret.deposer(Montant.de("100"));

        assertThatThrownBy(() -> livret.deposer(Montant.ZERO)).isInstanceOf(VersementInsuffisantException.class);
        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void un_retrait_diminue_le_solde_du_montant_retire() {
        Livret livret = Livret.ouvrir();
        livret.deposer(Montant.de("150.25"));

        livret.retirer(Montant.de("50.25"));

        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }
}
