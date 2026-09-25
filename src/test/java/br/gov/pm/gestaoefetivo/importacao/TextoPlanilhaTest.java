package br.gov.pm.gestaoefetivo.importacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextoPlanilhaTest {

    @Test
    @DisplayName("iguala as variações de ordinal que aparecem em planilha digitada à mão")
    void normalizaOrdinais() {
        assertThat(TextoPlanilha.chave("3º Sgt")).isEqualTo("3 SGT");
        assertThat(TextoPlanilha.chave("3o sgt")).isEqualTo("3 SGT");
        assertThat(TextoPlanilha.chave("3 SGT")).isEqualTo("3 SGT");
        assertThat(TextoPlanilha.chave("1º BPM")).isEqualTo(TextoPlanilha.chave("1o bpm"));
        assertThat(TextoPlanilha.chave("1ª Cia")).isEqualTo(TextoPlanilha.chave("1a CIA"));
    }

    @Test
    void removeAcentosPontuacaoEEspacosRepetidos() {
        assertThat(TextoPlanilha.chave("Situação  Funcional")).isEqualTo("SITUACAO FUNCIONAL");
        assertThat(TextoPlanilha.chave("Cia Pol. Ambiental")).isEqualTo("CIA POL AMBIENTAL");
        assertThat(TextoPlanilha.chave("E-mail")).isEqualTo("E MAIL");
        assertThat(TextoPlanilha.chave(null)).isEmpty();
    }
}
