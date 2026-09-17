package io.github.jurch29.epargne.domain.livret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jurch29.epargne.domain.commun.Montant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LivretTest {

    @Test
    void un_livret_ouvert_a_un_solde_nul() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);

        assertThat(livret.solde()).isEqualTo(Montant.ZERO);
    }

    @Test
    void un_depot_augmente_le_solde_du_montant_depose() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);

        livret.deposer(Montant.de("100"));

        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void deux_depots_successifs_s_additionnent() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);

        livret.deposer(Montant.de("100"));
        livret.deposer(Montant.de("50.25"));

        assertThat(livret.solde()).isEqualTo(Montant.de("150.25"));
    }

    @Test
    void refuse_un_depot_nul_et_laisse_le_solde_inchange() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);
        livret.deposer(Montant.de("100"));

        assertThatThrownBy(() -> livret.deposer(Montant.ZERO)).isInstanceOf(MouvementNulException.class);
        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void un_retrait_diminue_le_solde_du_montant_retire() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);
        livret.deposer(Montant.de("150.25"));

        livret.retirer(Montant.de("50.25"));

        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void un_retrait_egal_au_solde_est_autorise_et_laisse_un_solde_nul() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);
        livret.deposer(Montant.de("100"));

        livret.retirer(Montant.de("100"));

        assertThat(livret.solde()).isEqualTo(Montant.ZERO);
    }

    @Test
    void refuse_un_retrait_superieur_au_solde_et_laisse_le_solde_inchange() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);
        livret.deposer(Montant.de("100"));

        assertThatThrownBy(() -> livret.retirer(Montant.de("100.01"))).isInstanceOf(SoldeInsuffisantException.class);
        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @Test
    void refuse_un_retrait_nul_et_laisse_le_solde_inchange() {
        Livret livret = Livret.ouvrir(TypeLivret.LIVRET_A);
        livret.deposer(Montant.de("100"));

        assertThatThrownBy(() -> livret.retirer(Montant.ZERO)).isInstanceOf(MouvementNulException.class);
        assertThat(livret.solde()).isEqualTo(Montant.de("100"));
    }

    @ParameterizedTest
    @CsvSource({"LIVRET_A, 22950", "LDDS, 12000", "LIVRET_JEUNE, 1600"})
    void un_depot_amenant_le_solde_au_plafond_est_autorise(TypeLivret type, String plafond) {
        Livret livret = Livret.ouvrir(type);

        livret.deposer(Montant.de(plafond));

        assertThat(livret.solde()).isEqualTo(Montant.de(plafond));
    }

    @ParameterizedTest
    @CsvSource({"LIVRET_A, 22950", "LDDS, 12000", "LIVRET_JEUNE, 1600"})
    void refuse_un_depot_qui_depasse_le_plafond_et_laisse_le_solde_inchange(TypeLivret type, String plafond) {
        Livret livret = Livret.ouvrir(type);
        livret.deposer(Montant.de(plafond));

        assertThatThrownBy(() -> livret.deposer(Montant.de("0.01"))).isInstanceOf(PlafondDepasseException.class);
        assertThat(livret.solde()).isEqualTo(Montant.de(plafond));
    }
}
