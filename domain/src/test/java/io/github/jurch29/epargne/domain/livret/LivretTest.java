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
}
