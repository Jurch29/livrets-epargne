package io.github.jurch29.epargne.domain.livret;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LivretTest {

    @Test
    void un_livret_ouvert_a_un_solde_nul() {
        Livret livret = Livret.ouvrir();

        assertThat(livret.solde()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void un_depot_augmente_le_solde_du_montant_depose() {
        Livret livret = Livret.ouvrir();

        livret.deposer(BigDecimal.valueOf(100));

        assertThat(livret.solde()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void deux_depots_successifs_s_additionnent() {
        Livret livret = Livret.ouvrir();

        livret.deposer(new BigDecimal("100"));
        livret.deposer(new BigDecimal("50.25"));

        assertThat(livret.solde()).isEqualByComparingTo(new BigDecimal("150.25"));
    }
}
