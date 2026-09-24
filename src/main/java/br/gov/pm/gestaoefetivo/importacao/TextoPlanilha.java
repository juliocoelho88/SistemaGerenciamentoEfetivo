package br.gov.pm.gestaoefetivo.importacao;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalização dos textos que vêm da planilha. Planilha preenchida à mão traz o mesmo valor escrito
 * de várias formas ("3º Sgt", "3o SGT", "3 sgt"); aqui tudo vira uma chave única de comparação para
 * casar com os catálogos do banco (posto, unidade, situação) e com os cabeçalhos das colunas.
 */
public final class TextoPlanilha {

    private TextoPlanilha() {
    }

    /** Ex.: "1º BPM" e "1o bpm" → "1 BPM". Retorna "" para entrada nula ou em branco. */
    public static String chave(String bruto) {
        if (bruto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(bruto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String semOrdinal = semAcento.replace("º", " ").replace("ª", " ").replace("°", " ");
        String maiusculo = semOrdinal.toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Z]", " ");
        // "3O SGT" / "1A CIA" → "3 SGT" / "1 CIA": o marcador de ordinal digitado como letra some.
        String semOrdinalLetra = maiusculo.replaceAll("(?<=\\d)\\s*[OA]\\b", "");
        return semOrdinalLetra.replaceAll("\\s+", " ").trim();
    }

    public static String somenteDigitos(String bruto) {
        return bruto == null ? "" : bruto.replaceAll("\\D", "");
    }

    /** @return o texto sem espaços nas pontas, ou {@code null} se ficar vazio. */
    public static String limpar(String bruto) {
        if (bruto == null) {
            return null;
        }
        String limpo = bruto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
