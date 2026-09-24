package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacao;
import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacaoRepository;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncional;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncionalRepository;
import br.gov.pm.gestaoefetivo.efetivo.Unidade;
import br.gov.pm.gestaoefetivo.efetivo.UnidadeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Os três catálogos (posto, unidade, situação) carregados de uma vez e indexados por texto normalizado.
 *
 * <p>É o que evita o N+1 da importação: sem isto cada linha da planilha custaria três SELECTs para
 * resolver as chaves estrangeiras. São tabelas pequenas (dezenas de linhas), então cabem em memória
 * durante o processamento do arquivo.
 */
public record CatalogoEfetivo(Map<String, Long> postos,
                              Map<String, Long> unidades,
                              Map<String, Long> situacoes) {

    public static CatalogoEfetivo carregar(PostoGraduacaoRepository postoRepository,
                                            UnidadeRepository unidadeRepository,
                                            SituacaoFuncionalRepository situacaoRepository) {
        return new CatalogoEfetivo(
                indexar(postoRepository.findAll(), PostoGraduacao::getId, PostoGraduacao::getNome, PostoGraduacao::getAbreviacao),
                indexar(unidadeRepository.findAll(), Unidade::getId, Unidade::getNome, Unidade::getSigla),
                indexar(situacaoRepository.findAll(), SituacaoFuncional::getId, SituacaoFuncional::getNome));
    }

    /** Indexa cada registro por todos os seus rótulos (nome, sigla/abreviação) e também pelo próprio id. */
    @SafeVarargs
    private static <T> Map<String, Long> indexar(List<T> registros, Function<T, Long> id, Function<T, String>... rotulos) {
        Map<String, Long> indice = new HashMap<>();
        for (T registro : registros) {
            Long valor = id.apply(registro);
            indice.putIfAbsent(String.valueOf(valor), valor);
            for (Function<T, String> rotulo : rotulos) {
                String chave = TextoPlanilha.chave(rotulo.apply(registro));
                if (!chave.isEmpty()) {
                    indice.putIfAbsent(chave, valor);
                }
            }
        }
        return indice;
    }

    public Long posto(String texto) {
        return postos.get(TextoPlanilha.chave(texto));
    }

    public Long unidade(String texto) {
        return unidades.get(TextoPlanilha.chave(texto));
    }

    public Long situacao(String texto) {
        return situacoes.get(TextoPlanilha.chave(texto));
    }

    /** Usado nas mensagens de erro: mostrar o que o sistema aceita poupa uma ida e volta com o usuário. */
    public static String amostra(Map<String, Long> indice) {
        List<String> rotulos = new ArrayList<>();
        for (Map.Entry<String, Long> entrada : indice.entrySet()) {
            if (!entrada.getKey().matches("\\d+")) {
                rotulos.add(entrada.getKey());
            }
        }
        rotulos.sort(String::compareTo);
        return String.join(", ", rotulos);
    }
}
