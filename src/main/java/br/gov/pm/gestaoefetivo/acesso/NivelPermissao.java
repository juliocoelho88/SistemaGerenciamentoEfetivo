package br.gov.pm.gestaoefetivo.acesso;

/** Ordem crescente de acesso: cada nível inclui os anteriores (TOTAL > EDITAR > VER > NENHUM). */
public enum NivelPermissao {
    NENHUM,
    VER,
    EDITAR,
    TOTAL;

    public boolean atende(NivelPermissao minimo) {
        return this.ordinal() >= minimo.ordinal();
    }
}
