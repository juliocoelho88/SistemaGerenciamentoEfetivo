package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.importacao.dto.ImportacaoResultadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Importação em massa do efetivo a partir de planilhas Excel. */
@RestController
@RequestMapping("/api/pessoas/importacao")
@PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
public class ImportacaoPessoaController {

    private static final String TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ImportacaoPessoaService importacaoPessoaService;
    private final ModeloPlanilhaPessoa modeloPlanilhaPessoa;

    public ImportacaoPessoaController(ImportacaoPessoaService importacaoPessoaService,
                                       ModeloPlanilhaPessoa modeloPlanilhaPessoa) {
        this.importacaoPessoaService = importacaoPessoaService;
        this.modeloPlanilhaPessoa = modeloPlanilhaPessoa;
    }

    @Operation(summary = "Importa pessoas a partir de uma ou mais planilhas Excel",
            description = "Aceita .xlsx e .xls. Rode antes com simulacao=true para conferir o relatório de erros "
                    + "sem gravar nada. Cada arquivo é processado na própria transação.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacaoResultadoResponse importar(
            @RequestParam("arquivos") List<MultipartFile> arquivos,
            @RequestParam(defaultValue = "false") boolean atualizarExistentes,
            @RequestParam(defaultValue = "false") boolean simulacao,
            @RequestParam(required = false) String unidadePadrao,
            @RequestParam(defaultValue = "Ativo") String situacaoPadrao) {
        OpcoesImportacao opcoes = new OpcoesImportacao(atualizarExistentes, simulacao, unidadePadrao, situacaoPadrao);
        return importacaoPessoaService.importar(arquivos, opcoes);
    }

    @Operation(summary = "Baixa a planilha modelo, já com os postos, unidades e situações aceitos pelo sistema")
    @GetMapping("/modelo")
    public ResponseEntity<ByteArrayResource> modelo() {
        byte[] planilha = modeloPlanilhaPessoa.gerar();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("modelo-importacao-efetivo.xlsx").build().toString())
                .contentType(MediaType.parseMediaType(TIPO_XLSX))
                .contentLength(planilha.length)
                .body(new ByteArrayResource(planilha));
    }
}
