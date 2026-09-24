# Handoff — Sistema de Gestão de Efetivo

Pacote para implementar o backend com **Claude Code**. Contém o esquema do banco pronto, o `pom.xml` com as dependências, e os protótipos de referência (HTML) das telas e do modelo relacional.

## Como usar com Claude Code

1. Copie **todo o conteúdo desta pasta** para a raiz do seu repositório (ou crie o repo a partir dela).
2. Abra o Claude Code na pasta e peça, por exemplo:
   > "Implemente o backend Spring Boot a partir de `src/main/resources/db/migration/V1__schema.sql`. Gere as entidades JPA, repositories, services e controllers REST para cada tabela. Aplique a matriz de permissões da tabela `perfil_permissao` com Spring Security, e o filtro por unidade (`usuario.escopo_unidade_id`) para o perfil Gestor. As telas de referência estão em `design-reference/`."
3. O Claude Code lê o `pom.xml`, o SQL e os HTML de referência e implementa em cima disso.

> **Importante:** os arquivos em `design-reference/` são **protótipos em HTML** — mostram a aparência e o comportamento pretendidos das telas, **não são código de produção para copiar**. A tarefa é recriar essas telas no ambiente-alvo (o backend serve a API; o frontend, se houver, segue o layout dos protótipos). Fidelidade: **alta** (cores, tipografia e fluxo finais).

## O que já vem pronto

| Arquivo | O que é |
|---|---|
| `pom.xml` | Projeto Maven + Spring Boot 3.3, Java 21, com as dependências |
| `src/main/resources/db/migration/V1__schema.sql` | Migração Flyway com as 13 tabelas (aplica no boot) |
| `design-reference/Modelo Relacional.dc.html` | Diagrama ER (referência visual do banco) |
| `design-reference/Gestao de Efetivo.dc.html` | Telas: Efetivo, Ficha, Vencimentos, Relatórios, Acessos |

## Dependências (pom.xml) e por quê

**Essenciais**
- `spring-boot-starter-web` — API REST
- `spring-boot-starter-data-jpa` — mapeamento das 13 tabelas
- `postgresql` — driver do banco
- `flyway-core` + `flyway-database-postgresql` — versiona/aplica o schema

**Acesso / perfis**
- `spring-boot-starter-security` — perfis e autorização
- `spring-boot-starter-validation` — regras de campo (ex.: formato do RE)
- `jjwt` (comentado no pom) — habilite se o login for por token JWT

**Qualidade de vida (opcionais)**
- `lombok` — menos boilerplate nas entidades
- `springdoc-openapi` — Swagger UI automático

**Testes**
- `spring-boot-starter-test`, `spring-security-test`, `h2`

Comece com os essenciais + acesso; adicione o resto quando precisar.

## Modelo de dados (resumo)

Centro = **`pessoa`** (RE, CPF, posto, unidade, situação). Tudo o mais são registros datados ligados a ela.

**Núcleo:** `pessoa`
**Apoio (lookups):** `posto_graduacao`, `situacao_funcional`, `unidade` (auto-relação `unidade_pai_id` = hierarquia)
**Formação (catálogo + vínculo):** `curso`/`pessoa_curso`, `habilitacao`/`pessoa_habilitacao`, `pessoa_estagio`
**Acesso:** `usuario`, `perfil`, `modulo`, `perfil_permissao`

Regras de negócio já embutidas no SQL:
- **RE:** `VARCHAR(9)`, único, formato `XXX.XXX-D` onde `D` é `0-9` ou `A` — `CHECK (re ~ '^[0-9]{3}\.[0-9]{3}-[0-9A]$')`. Na entidade JPA use `@Pattern(regexp="\\d{3}\\.\\d{3}-[0-9A]")`.
- **Validade** (`data_validade` em cursos/habilitações) alimenta o painel de Vencimentos. Sugestão: um serviço agendado (`@Scheduled`) recalcula o `status` (VÁLIDO/VENCENDO/VENCIDO) comparando com a data atual (janela de 90 dias para "vencendo").
- **`usuario.pessoa_id` é nullable** — admin/gestor pode não ser do efetivo.

## Telas de referência (em `design-reference/`)

- **Efetivo** — lista com busca (nome/RE/posto), filtros por lotação e situação, semáforo de validades por policial.
- **Ficha individual** — dados funcionais + abas Cursos / Habilitações / Estágios (datas, validades, status).
- **Vencimentos** — vencidos e a vencer em ≤ 90 dias, ordenados por urgência.
- **Relatórios** — distribuição por lotação, situação e posto/graduação.
- **Acessos** — perfis, **matriz de permissões** (perfil × módulo) e usuários do sistema.

## Perfis e matriz de permissões

Perfis → roles do Spring Security: `ROLE_ADMIN`, `ROLE_GESTOR_UNIDADE`, `ROLE_INSTRUTOR`, `ROLE_POLICIAL`.

Níveis por módulo (valores da coluna `perfil_permissao.nivel`): `NENHUM` / `VER` / `EDITAR` / `TOTAL`.

| Perfil | Efetivo | Cursos & Estágios | Habilitações | Vencimentos | Relatórios | Gestão de Acessos |
|---|---|---|---|---|---|---|
| Administrador | TOTAL | TOTAL | TOTAL | TOTAL | TOTAL | TOTAL |
| Gestor de Unidade | EDITAR | EDITAR | EDITAR | VER | VER | NENHUM |
| Instrutor / Ensino | VER | TOTAL | TOTAL | VER | VER | NENHUM |
| Policial | VER | VER | VER | NENHUM | NENHUM | NENHUM |

Duas camadas de autorização:
1. **Por role** (o que o perfil pode fazer no módulo) — `@PreAuthorize` nos endpoints.
2. **Por dado / row-level** (o que ele pode ver): Gestor só a própria unidade (`usuario.escopo_unidade_id`); Policial só o próprio registro (`usuario.pessoa_id`). Roles não resolvem isso sozinhas — filtre nas queries pelo usuário logado.

## Importação em massa do efetivo (planilhas Excel)

`POST /api/pessoas/importacao` recebe uma ou mais planilhas `.xlsx`/`.xls` (campo `arquivos`, multipart) e
cadastra o efetivo em lote. Exige permissão **EDITAR** no módulo `EFETIVO`.

| Parâmetro | Padrão | Para que serve |
|---|---|---|
| `simulacao` | `false` | `true` valida tudo e devolve o relatório **sem gravar nada** |
| `atualizarExistentes` | `false` | `true` atualiza quem já existe (mesmo RE); `false` deixa como está e conta em `ignoradas` |
| `unidadePadrao` | — | Unidade das linhas sem coluna/valor de unidade (ex.: uma planilha por OPM) |
| `situacaoPadrao` | `Ativo` | Situação das linhas sem coluna/valor de situação |

`GET /api/pessoas/importacao/modelo` baixa a planilha modelo, com os cabeçalhos aceitos e uma segunda aba
listando os postos, unidades e situações que existem no banco.

**Colunas.** Identificadas pelo texto do cabeçalho, ignorando maiúsculas, acentos e pontuação, e aceitando
apelidos (`Lotação`/`OPM` → Unidade, `Matrícula` → RE, `Graduação` → Posto). Obrigatórias: RE, Nome, CPF e
Posto. O cabeçalho não precisa estar na primeira linha — título e linhas em branco antes dele são ignorados.

**O que é normalizado sozinho:** RE em qualquer formato vira `XXX.XXX-D` (inclusive repondo o zero à
esquerda que o Excel corta de célula numérica: `123456` → `012.345-6`); CPF vira mascarado; datas aceitam
`dd/mm/aaaa`, ISO e célula de data do Excel; `1o BPM`/`1º BPM` e `3o Sgt`/`3º Sgt` casam com o catálogo;
sexo aceita `M`/`F`/`Masculino`/`Feminino`.

**O que é recusado, linha a linha:** CPF com dígito verificador errado, data impossível (`31/02`), posto ou
unidade que não existe no sistema, RE/CPF repetido na própria planilha, CPF já cadastrado para outra pessoa
e e-mail inválido. A linha entra no relatório com **o número da linha como aparece no Excel** e o motivo; as
demais linhas seguem normalmente. Rode com `simulacao=true` primeiro para ver tudo antes de gravar.

**Row-level scoping:** o Gestor de Unidade só importa pessoas da própria unidade — as demais linhas são
recusadas com motivo explícito.

Cada arquivo é processado em uma transação própria: uma planilha corrompida não desfaz as anteriores.

### Por que a importação é rápida

Três decisões, todas verificadas com 2.000 linhas reais (~0,6 s de ponta a ponta):

1. **Catálogos carregados uma vez por arquivo** (`CatalogoEfetivo`) em vez de um SELECT por linha para
   resolver posto/unidade/situação.
2. **Existência de RE/CPF consultada por lote** (`findAllByReIn`/`findAllByCpfIn`) em vez de um `existsBy`
   por linha — o caminho do cadastro individual gasta 5 queries por pessoa.
3. **Inserts agrupados de verdade.** `pessoa.id` deixou de ser `IDENTITY` e passou a usar a sequence em
   blocos de 50 (`V3__sequencia_pessoa_em_lote.sql` + `@SequenceGenerator` em `Pessoa`), porque com
   IDENTITY o Hibernate precisa do id a cada linha e **desliga o batch de INSERT**. Somado a
   `reWriteBatchedInserts=true` na URL JDBC e ao `batch_size`, as 2.000 linhas viraram **28 comandos
   INSERT e 40 chamadas `nextval`**, em vez de 2.000 de cada.

> ⚠️ O `allocationSize` do `@SequenceGenerator` (50) precisa continuar igual ao `INCREMENT BY` da sequence.
> Alterar um sem o outro causa colisão de ids.

## Próximos passos sugeridos (não implementados)

- Histórico funcional (promoções/transferências) como nova tabela ligada a `pessoa`.
- Upload de certificados (`certificado_url` já existe em `pessoa_curso`).
- Auditoria (quem alterou o quê).
- Importação assíncrona (job + polling) se as planilhas passarem de ~50 mil linhas: hoje o processamento é
  síncrono e a leitura do `.xlsx` carrega a aba inteira em memória.
- Importação em massa de cursos, habilitações e estágios, nos mesmos moldes do efetivo.
