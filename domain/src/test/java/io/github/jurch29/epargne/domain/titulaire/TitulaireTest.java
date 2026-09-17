package io.github.jurch29.epargne.domain.titulaire;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TitulaireTest {

    @Test
    void l_age_augmente_le_jour_de_l_anniversaire_pas_la_veille() {
        Titulaire titulaire = new Titulaire(LocalDate.of(2000, 6, 15));

        assertThat(titulaire.ageLe(LocalDate.of(2026, 6, 14))).isEqualTo(25);
        assertThat(titulaire.ageLe(LocalDate.of(2026, 6, 15))).isEqualTo(26);
    }

    /**
     * Écart connu et assumé : l'usage juridique français fait tomber l'anniversaire au 28 février les années non
     * bissextiles, alors que {@link java.time.Period} attend le 1er mars. Un jour d'écart tous les quatre ans sur un
     * seuil d'âge — figé ici pour que la décision soit visible plutôt que découverte en production.
     */
    @Test
    void un_titulaire_ne_un_29_fevrier_ne_vieillit_que_le_1er_mars_les_annees_non_bissextiles() {
        Titulaire titulaire = new Titulaire(LocalDate.of(2004, 2, 29));

        assertThat(titulaire.ageLe(LocalDate.of(2026, 2, 28))).isEqualTo(21);
        assertThat(titulaire.ageLe(LocalDate.of(2026, 3, 1))).isEqualTo(22);
    }
}
