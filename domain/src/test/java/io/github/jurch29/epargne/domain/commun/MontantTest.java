package io.github.jurch29.epargne.domain.commun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MontantTest {

    @Test
    void deux_montants_de_meme_valeur_sont_egaux_quelle_que_soit_l_ecriture() {
        assertThat(Montant.de("100")).isEqualTo(Montant.de("100.00"));
    }

    @Test
    void refuse_un_montant_negatif() {
        assertThatThrownBy(() -> Montant.de("-0.01")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_un_montant_de_plus_de_deux_decimales() {
        assertThatThrownBy(() -> Montant.de("10.001")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void additionne_deux_montants() {
        assertThat(Montant.de("100").ajouter(Montant.de("50.25"))).isEqualTo(Montant.de("150.25"));
    }
}
