package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.Pessoa;
import br.gov.pm.gestaoefetivo.efetivo.PessoaRepository;
import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacao;
import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacaoRepository;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncional;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncionalRepository;
import br.gov.pm.gestaoefetivo.efetivo.Unidade;
import br.gov.pm.gestaoefetivo.efetivo.UnidadeRepository;
import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import br.gov.pm.gestaoefetivo.importacao.dto.ImportacaoArquivoResponse;
import br.gov.pm.gestaoefetivo.importacao.dto.ImportacaoResultadoResponse;
import br.gov.pm.gestaoefetivo.importacao.dto.LinhaRejeitadaResponse;
import br.gov.pm.gestaoefetivo.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Importação em massa de pessoas a partir de planilhas Excel.
 *
 * <p>O ganho de desempenho vem de três decisões: os catálogos (posto/unidade/situação) são carregados
 * uma vez por arquivo em vez de um SELECT por linha; a existência de RE/CPF é consultada por lote em
 * vez de um existsBy por linha; e os inserts são agrupados pelo JDBC (ver o allocationSize da sequence
 * em {@link Pessoa} e o batch_size no application.yml).
 *
 * <p>Cada arquivo roda na própria transação: uma planilha problemática não desfaz as anteriores.
 */
@Service
public class ImportacaoPessoaService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoPessoaService.class);

    /** Linhas processadas entre cada flush/clear — mantém o persistence context pequeno em arquivos grandes. */
    private static final int TAMANHO_LOTE = 500;

    private final PlanilhaPessoaLeitor leitor;
    private final NormalizadorPessoa normalizador;
    private final PessoaRepository pessoaRepository;
    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final UnidadeRepository unidadeRepository;
    private final SituacaoFuncionalRepository situacaoFuncionalRepository;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public ImportacaoPessoaService(PlanilhaPessoaLeitor leitor,
                                    NormalizadorPessoa normalizador,
                                    PessoaRepository pessoaRepository,
                                    PostoGraduacaoRepository postoGraduacaoRepository,
                                    UnidadeRepository unidadeRepository,
                                    SituacaoFuncionalRepository situacaoFuncionalRepository,
                                    TransactionTemplate transactionTemplate) {
        this.leitor = leitor;
        this.normalizador = normalizador;
        this.pessoaRepository = pessoaRepository;
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.unidadeRepository = unidadeRepository;
        this.situacaoFuncionalRepository = situacaoFuncionalRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ImportacaoResultadoResponse importar(List<MultipartFile> arquivos, OpcoesImportacao opcoes) {
        if (arquivos == null || arquivos.isEmpty()) {
            throw new RegraNegocioException("Nenhum arquivo enviado.");
        }
        AuthenticatedUser usuario = AuthenticatedUser.atual();
        if (usuario != null && usuario.isVinculadoAPessoa()) {
            throw new AccessDeniedException("Este perfil só pode acessar o próprio registro.");
        }
        Long unidadeRestrita = usuario != null && usuario.isGestorDeUnidade() ? usuario.escopoUnidadeId() : null;

        List<ImportacaoArquivoResponse> detalhes = new ArrayList<>();
        for (MultipartFile arquivo : arquivos) {
            detalhes.add(processarComIsolamento(arquivo, opcoes, unidadeRestrita));
        }
        return ImportacaoResultadoResponse.consolidar(opcoes.simulacao(), detalhes);
    }

    private ImportacaoArquivoResponse processarComIsolamento(MultipartFile arquivo, OpcoesImportacao opcoes,
                                                              Long unidadeRestrita) {
        String nome = arquivo.getOriginalFilename() == null ? "(sem nome)" : arquivo.getOriginalFilename();
        try {
            return transactionTemplate.execute(status -> {
                ImportacaoArquivoResponse resultado = processar(nome, arquivo, opcoes, unidadeRestrita);
                if (opcoes.simulacao()) {
                    status.setRollbackOnly();
                }
                return resultado;
            });
        } catch (RegraNegocioException e) {
            return ImportacaoArquivoResponse.falha(nome, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Falha ao importar a planilha {}", nome, e);
            return ImportacaoArquivoResponse.falha(nome, "Erro inesperado ao processar o arquivo: " + e.getMessage());
        }
    }

    private ImportacaoArquivoResponse processar(String nome, MultipartFile arquivo, OpcoesImportacao opcoes,
                                                 Long unidadeRestrita) {
        PlanilhaPessoa planilha;
        try (InputStream entrada = arquivo.getInputStream()) {
            planilha = leitor.ler(entrada);
        } catch (IOException e) {
            throw new RegraNegocioException("Não consegui ler o arquivo enviado.");
        }

        CatalogoEfetivo catalogo = CatalogoEfetivo.carregar(postoGraduacaoRepository, unidadeRepository,
                situacaoFuncionalRepository);
        NormalizadorPessoa.Padroes padroes = resolverPadroes(catalogo, opcoes);

        List<LinhaRejeitadaResponse> rejeitadas = new ArrayList<>();
        List<PessoaNormalizada> validas = validar(planilha, catalogo, padroes, unidadeRestrita, rejeitadas);

        Contagem contagem = persistir(validas, opcoes, rejeitadas);

        return new ImportacaoArquivoResponse(nome, planilha.aba(),
                planilha.colunas().keySet().stream().map(ColunaPessoa::rotulo).toList(),
                planilha.colunasIgnoradas(),
                planilha.linhas().size(), contagem.criadas(), contagem.atualizadas(), contagem.ignoradas(),
                rejeitadas.size(), null, rejeitadas);
    }

    private NormalizadorPessoa.Padroes resolverPadroes(CatalogoEfetivo catalogo, OpcoesImportacao opcoes) {
        Long unidadeId = null;
        if (opcoes.unidadePadrao() != null && !opcoes.unidadePadrao().isBlank()) {
            unidadeId = catalogo.unidade(opcoes.unidadePadrao());
            if (unidadeId == null) {
                throw new RegraNegocioException("Unidade padrão \"" + opcoes.unidadePadrao() + "\" não existe. "
                        + "Valores aceitos: " + CatalogoEfetivo.amostra(catalogo.unidades()) + ".");
            }
        }
        Long situacaoId = null;
        if (opcoes.situacaoPadrao() != null && !opcoes.situacaoPadrao().isBlank()) {
            situacaoId = catalogo.situacao(opcoes.situacaoPadrao());
            if (situacaoId == null) {
                throw new RegraNegocioException("Situação padrão \"" + opcoes.situacaoPadrao() + "\" não existe. "
                        + "Valores aceitos: " + CatalogoEfetivo.amostra(catalogo.situacoes()) + ".");
            }
        }
        return new NormalizadorPessoa.Padroes(unidadeId, situacaoId);
    }

    /** Primeira passada: normaliza, valida e descarta as duplicatas internas do próprio arquivo. */
    private List<PessoaNormalizada> validar(PlanilhaPessoa planilha, CatalogoEfetivo catalogo,
                                             NormalizadorPessoa.Padroes padroes, Long unidadeRestrita,
                                             List<LinhaRejeitadaResponse> rejeitadas) {
        List<PessoaNormalizada> validas = new ArrayList<>();
        Set<String> resDoArquivo = new HashSet<>();
        Set<String> cpfsDoArquivo = new HashSet<>();

        for (LinhaPlanilha linha : planilha.linhas()) {
            try {
                PessoaNormalizada pessoa = normalizador.normalizar(linha, catalogo, padroes);
                if (unidadeRestrita != null && !unidadeRestrita.equals(pessoa.unidadeId())) {
                    throw new LinhaInvalidaException("Este perfil só pode importar pessoas da própria unidade.");
                }
                if (!resDoArquivo.add(pessoa.re())) {
                    throw new LinhaInvalidaException("RE " + pessoa.re() + " repetido nesta planilha.");
                }
                if (!cpfsDoArquivo.add(pessoa.cpf())) {
                    throw new LinhaInvalidaException("CPF " + pessoa.cpf() + " repetido nesta planilha.");
                }
                validas.add(pessoa);
            } catch (LinhaInvalidaException e) {
                rejeitadas.add(new LinhaRejeitadaResponse(linha.numero(), linha.valor(ColunaPessoa.RE),
                        linha.valor(ColunaPessoa.NOME), e.getMessage()));
            }
        }
        return validas;
    }

    /** Segunda passada: grava em lotes, consultando os já cadastrados uma vez por lote. */
    private Contagem persistir(List<PessoaNormalizada> validas, OpcoesImportacao opcoes,
                                List<LinhaRejeitadaResponse> rejeitadas) {
        int criadas = 0;
        int atualizadas = 0;
        int ignoradas = 0;

        for (int inicio = 0; inicio < validas.size(); inicio += TAMANHO_LOTE) {
            List<PessoaNormalizada> lote = validas.subList(inicio, Math.min(inicio + TAMANHO_LOTE, validas.size()));
            Map<String, Pessoa> porRe = indexarPorRe(lote);
            Map<String, Long> donoDoCpf = indexarDonoDoCpf(lote);

            for (PessoaNormalizada normalizada : lote) {
                Pessoa existente = porRe.get(normalizada.re());
                Long donoDoMesmoCpf = donoDoCpf.get(normalizada.cpf());
                boolean cpfDeOutraPessoa = donoDoMesmoCpf != null
                        && (existente == null || !donoDoMesmoCpf.equals(existente.getId()));
                if (cpfDeOutraPessoa) {
                    rejeitadas.add(new LinhaRejeitadaResponse(normalizada.linha(), normalizada.re(), normalizada.nome(),
                            "CPF " + normalizada.cpf() + " já está cadastrado para outra pessoa."));
                    continue;
                }
                if (existente == null) {
                    entityManager.persist(aplicar(new Pessoa(), normalizada));
                    criadas++;
                } else if (opcoes.atualizarExistentes()) {
                    aplicar(existente, normalizada);
                    atualizadas++;
                } else {
                    ignoradas++;
                }
            }
            entityManager.flush();
            entityManager.clear();
        }
        return new Contagem(criadas, atualizadas, ignoradas);
    }

    private Map<String, Pessoa> indexarPorRe(List<PessoaNormalizada> lote) {
        Set<String> res = lote.stream().map(PessoaNormalizada::re).collect(java.util.stream.Collectors.toSet());
        Map<String, Pessoa> indice = new HashMap<>();
        pessoaRepository.findAllByReIn(res).forEach(pessoa -> indice.put(pessoa.getRe(), pessoa));
        return indice;
    }

    private Map<String, Long> indexarDonoDoCpf(List<PessoaNormalizada> lote) {
        Set<String> cpfs = lote.stream().map(PessoaNormalizada::cpf).collect(java.util.stream.Collectors.toSet());
        Map<String, Long> indice = new HashMap<>();
        pessoaRepository.findAllByCpfIn(cpfs).forEach(pessoa -> indice.put(pessoa.getCpf(), pessoa.getId()));
        return indice;
    }

    /**
     * getReference em vez de findById: os ids vieram do catálogo já validado, então não há motivo para
     * pagar um SELECT por linha só para montar a chave estrangeira.
     */
    private Pessoa aplicar(Pessoa pessoa, PessoaNormalizada dados) {
        pessoa.setRe(dados.re());
        pessoa.setNome(dados.nome());
        pessoa.setCpf(dados.cpf());
        pessoa.setDataNascimento(dados.dataNascimento());
        pessoa.setSexo(dados.sexo());
        pessoa.setDataPraca(dados.dataPraca());
        pessoa.setTelefone(dados.telefone());
        pessoa.setEmail(dados.email());
        pessoa.setPosto(entityManager.getReference(PostoGraduacao.class, dados.postoId()));
        pessoa.setUnidade(entityManager.getReference(Unidade.class, dados.unidadeId()));
        pessoa.setSituacao(entityManager.getReference(SituacaoFuncional.class, dados.situacaoId()));
        return pessoa;
    }

    private record Contagem(int criadas, int atualizadas, int ignoradas) {
    }
}
