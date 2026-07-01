-- ============================================================
-- Sistema de Gestão de Efetivo — Esquema do banco de dados
-- Dialeto: PostgreSQL (notas de portabilidade ao final)
-- Gerado a partir do Modelo Relacional (Modelo Relacional.dc.html)
-- ============================================================

-- ---------- TABELAS DE APOIO (lookups) ----------

CREATE TABLE posto_graduacao (
    id                 BIGSERIAL PRIMARY KEY,
    nome               VARCHAR(60)  NOT NULL,
    abreviacao         VARCHAR(12)  NOT NULL,
    ordem_hierarquica  INT          NOT NULL,
    circulo            VARCHAR(10)  NOT NULL CHECK (circulo IN ('PRACA','OFICIAL'))
);

CREATE TABLE situacao_funcional (
    id                   BIGSERIAL PRIMARY KEY,
    nome                 VARCHAR(40) NOT NULL,
    disponivel_operacao  BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE unidade (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    sigla           VARCHAR(20)  NOT NULL UNIQUE,
    tipo            VARCHAR(20)  NOT NULL CHECK (tipo IN ('BATALHAO','COMPANHIA','DIRETORIA','ESCOLA','OUTRO')),
    unidade_pai_id  BIGINT       REFERENCES unidade(id)   -- auto-relação: hierarquia de unidades
);

-- ---------- ACESSO ----------

CREATE TABLE perfil (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(60) NOT NULL UNIQUE,
    descricao  TEXT
);

CREATE TABLE modulo (
    id     BIGSERIAL PRIMARY KEY,
    nome   VARCHAR(60) NOT NULL,
    chave  VARCHAR(40) NOT NULL UNIQUE   -- ex.: 'EFETIVO', 'CURSOS', 'ACESSOS'
);

-- ---------- CATÁLOGOS DE FORMAÇÃO ----------

CREATE TABLE curso (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    instituicao     VARCHAR(120),
    carga_horaria   INT,
    exige_validade  BOOLEAN NOT NULL DEFAULT FALSE,
    validade_meses  INT                            -- null quando exige_validade = false
);

CREATE TABLE habilitacao (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    categoria       VARCHAR(60),
    exige_validade  BOOLEAN NOT NULL DEFAULT TRUE,
    orgao_emissor   VARCHAR(120),
    validade_meses  INT
);

-- ---------- NÚCLEO ----------

CREATE TABLE pessoa (
    id               BIGSERIAL PRIMARY KEY,
    re               VARCHAR(9)  NOT NULL UNIQUE
                     CHECK (re ~ '^[0-9]{3}\.[0-9]{3}-[0-9A]$'),   -- formato XXX.XXX-D (D = 0-9 ou A)
    nome             VARCHAR(150) NOT NULL,
    cpf              VARCHAR(14)  NOT NULL UNIQUE,
    data_nascimento  DATE,
    sexo             VARCHAR(1) CHECK (sexo IN ('M','F')),
    foto_url         VARCHAR(255),
    data_praca       DATE,                          -- data de ingresso na corporação
    posto_id         BIGINT NOT NULL REFERENCES posto_graduacao(id),
    unidade_id       BIGINT NOT NULL REFERENCES unidade(id),
    situacao_id      BIGINT NOT NULL REFERENCES situacao_funcional(id),
    telefone         VARCHAR(20),
    email            VARCHAR(150),
    criado_em        TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_pessoa_unidade  ON pessoa(unidade_id);
CREATE INDEX idx_pessoa_posto    ON pessoa(posto_id);
CREATE INDEX idx_pessoa_situacao ON pessoa(situacao_id);

CREATE TABLE usuario (
    id                 BIGSERIAL PRIMARY KEY,
    pessoa_id          BIGINT REFERENCES pessoa(id),          -- nullable: admin/gestor pode não ser do efetivo
    login              VARCHAR(60) NOT NULL UNIQUE,
    senha_hash         VARCHAR(255) NOT NULL,
    perfil_id          BIGINT NOT NULL REFERENCES perfil(id),
    escopo_unidade_id  BIGINT REFERENCES unidade(id),         -- gestor: unidade que ele enxerga (row-level)
    ativo              BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acesso      TIMESTAMP
);

CREATE TABLE perfil_permissao (
    id         BIGSERIAL PRIMARY KEY,
    perfil_id  BIGINT NOT NULL REFERENCES perfil(id) ON DELETE CASCADE,
    modulo_id  BIGINT NOT NULL REFERENCES modulo(id),
    nivel      VARCHAR(10) NOT NULL CHECK (nivel IN ('NENHUM','VER','EDITAR','TOTAL')),
    UNIQUE (perfil_id, modulo_id)      -- uma linha por perfil × módulo (a matriz)
);

-- ---------- VÍNCULOS DE FORMAÇÃO (pessoa × catálogo) ----------

CREATE TABLE pessoa_curso (
    id               BIGSERIAL PRIMARY KEY,
    pessoa_id        BIGINT NOT NULL REFERENCES pessoa(id) ON DELETE CASCADE,
    curso_id         BIGINT NOT NULL REFERENCES curso(id),
    data_conclusao   DATE,
    data_validade    DATE,
    certificado_url  VARCHAR(255),
    status           VARCHAR(12) NOT NULL DEFAULT 'CONCLUIDO'
                     CHECK (status IN ('EM_ANDAMENTO','CONCLUIDO','VENCIDO'))
);
CREATE INDEX idx_pcurso_pessoa   ON pessoa_curso(pessoa_id);
CREATE INDEX idx_pcurso_validade ON pessoa_curso(data_validade);

CREATE TABLE pessoa_habilitacao (
    id              BIGSERIAL PRIMARY KEY,
    pessoa_id       BIGINT NOT NULL REFERENCES pessoa(id) ON DELETE CASCADE,
    habilitacao_id  BIGINT NOT NULL REFERENCES habilitacao(id),
    numero          VARCHAR(40),
    data_emissao    DATE,
    data_validade   DATE,
    status          VARCHAR(12) NOT NULL DEFAULT 'VALIDO'
                    CHECK (status IN ('VALIDO','VENCENDO','VENCIDO'))
);
CREATE INDEX idx_phab_pessoa   ON pessoa_habilitacao(pessoa_id);
CREATE INDEX idx_phab_validade ON pessoa_habilitacao(data_validade);

CREATE TABLE pessoa_estagio (
    id           BIGSERIAL PRIMARY KEY,
    pessoa_id    BIGINT NOT NULL REFERENCES pessoa(id) ON DELETE CASCADE,
    nome         VARCHAR(120) NOT NULL,
    unidade_id   BIGINT REFERENCES unidade(id),
    supervisor   VARCHAR(150),
    data_inicio  DATE,
    data_fim     DATE,
    avaliacao    VARCHAR(60),
    status       VARCHAR(14) NOT NULL DEFAULT 'EM_ANDAMENTO'
                 CHECK (status IN ('EM_ANDAMENTO','CONCLUIDO','INTERROMPIDO'))
);
CREATE INDEX idx_pest_pessoa ON pessoa_estagio(pessoa_id);

-- ============================================================
-- Notas de portabilidade
--  • MySQL/MariaDB: troque BIGSERIAL por BIGINT AUTO_INCREMENT;
--    o CHECK de regex do RE vira REGEXP na constraint ou validação na app.
--  • H2 (testes): use IDENTITY no lugar de BIGSERIAL.
--  • Os CHECK de enum (circulo, nivel, status...) podem virar
--    tabelas de apoio se você quiser editá-los sem alterar o schema.
-- ============================================================
