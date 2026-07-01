-- ============================================================
-- Dados de seed — necessários para o primeiro boot (login, lookups, matriz de permissões)
-- ============================================================

-- ---------- Postos e graduações ----------
INSERT INTO posto_graduacao (nome, abreviacao, ordem_hierarquica, circulo) VALUES
    ('Soldado', 'Sd', 1, 'PRACA'),
    ('Cabo', 'Cb', 2, 'PRACA'),
    ('3º Sargento', '3º Sgt', 3, 'PRACA'),
    ('2º Sargento', '2º Sgt', 4, 'PRACA'),
    ('1º Sargento', '1º Sgt', 5, 'PRACA'),
    ('Subtenente', 'S Ten', 6, 'PRACA'),
    ('2º Tenente', '2º Ten', 7, 'OFICIAL'),
    ('1º Tenente', '1º Ten', 8, 'OFICIAL'),
    ('Capitão', 'Cap', 9, 'OFICIAL'),
    ('Major', 'Maj', 10, 'OFICIAL'),
    ('Tenente-Coronel', 'TC', 11, 'OFICIAL'),
    ('Coronel', 'Cel', 12, 'OFICIAL');

-- ---------- Situações funcionais ----------
INSERT INTO situacao_funcional (nome, disponivel_operacao) VALUES
    ('Ativo', TRUE),
    ('Férias', FALSE),
    ('Licença médica', FALSE),
    ('Afastado', FALSE),
    ('Reserva', FALSE);

-- ---------- Unidades ----------
INSERT INTO unidade (nome, sigla, tipo, unidade_pai_id) VALUES
    ('Comando-Geral', 'CG', 'DIRETORIA', NULL);

INSERT INTO unidade (nome, sigla, tipo, unidade_pai_id)
SELECT v.nome, v.sigla, v.tipo::varchar, cg.id
FROM (VALUES
    ('1º Batalhão de Polícia Militar', '1º BPM', 'BATALHAO'),
    ('5º Batalhão de Polícia Militar', '5º BPM', 'BATALHAO'),
    ('12º Batalhão de Polícia Militar', '12º BPM', 'BATALHAO'),
    ('Companhia de Policiamento de Trânsito', 'CPTran', 'COMPANHIA'),
    ('Rondas Ostensivas Tobias de Aguiar', 'ROTAM', 'COMPANHIA'),
    ('Batalhão de Operações Policiais Especiais', 'BOPE', 'BATALHAO'),
    ('Companhia de Polícia Ambiental', 'Cia Pol. Ambiental', 'COMPANHIA'),
    ('Rádio Patrulha', 'Rádio Patrulha', 'OUTRO'),
    ('Corregedoria', 'Corregedoria', 'DIRETORIA'),
    ('Academia de Polícia', 'Academia de Polícia', 'ESCOLA')
) AS v(nome, sigla, tipo)
CROSS JOIN (SELECT id FROM unidade WHERE sigla = 'CG') AS cg;

-- ---------- Módulos (menu / matriz de permissões) ----------
INSERT INTO modulo (nome, chave) VALUES
    ('Efetivo', 'EFETIVO'),
    ('Cursos & Estágios', 'CURSOS_ESTAGIOS'),
    ('Habilitações', 'HABILITACOES'),
    ('Vencimentos', 'VENCIMENTOS'),
    ('Relatórios', 'RELATORIOS'),
    ('Gestão de Acessos', 'ACESSOS');

-- ---------- Perfis ----------
INSERT INTO perfil (nome, descricao) VALUES
    ('Administrador', 'Controle total do sistema, incluindo gestão de acessos, configurações e todos os módulos.'),
    ('Gestor de Unidade', 'Gerencia o efetivo, validades e registros da própria lotação. Não acessa outras unidades.'),
    ('Instrutor / Ensino', 'Lança e atualiza cursos, estágios e habilitações do efetivo. Sem edição de dados funcionais.'),
    ('Policial', 'Consulta a própria ficha, validades e documentos pessoais. Somente leitura do próprio registro.');

-- ---------- Matriz de permissões (perfil x módulo) ----------
INSERT INTO perfil_permissao (perfil_id, modulo_id, nivel)
SELECT p.id, m.id, v.nivel
FROM (VALUES
    ('Administrador',      'EFETIVO',         'TOTAL'),
    ('Administrador',      'CURSOS_ESTAGIOS', 'TOTAL'),
    ('Administrador',      'HABILITACOES',    'TOTAL'),
    ('Administrador',      'VENCIMENTOS',     'TOTAL'),
    ('Administrador',      'RELATORIOS',      'TOTAL'),
    ('Administrador',      'ACESSOS',         'TOTAL'),

    ('Gestor de Unidade',  'EFETIVO',         'EDITAR'),
    ('Gestor de Unidade',  'CURSOS_ESTAGIOS', 'EDITAR'),
    ('Gestor de Unidade',  'HABILITACOES',    'EDITAR'),
    ('Gestor de Unidade',  'VENCIMENTOS',     'VER'),
    ('Gestor de Unidade',  'RELATORIOS',      'VER'),
    ('Gestor de Unidade',  'ACESSOS',         'NENHUM'),

    ('Instrutor / Ensino', 'EFETIVO',         'VER'),
    ('Instrutor / Ensino', 'CURSOS_ESTAGIOS', 'TOTAL'),
    ('Instrutor / Ensino', 'HABILITACOES',    'TOTAL'),
    ('Instrutor / Ensino', 'VENCIMENTOS',     'VER'),
    ('Instrutor / Ensino', 'RELATORIOS',      'VER'),
    ('Instrutor / Ensino', 'ACESSOS',         'NENHUM'),

    ('Policial',           'EFETIVO',         'VER'),
    ('Policial',           'CURSOS_ESTAGIOS', 'VER'),
    ('Policial',           'HABILITACOES',    'VER'),
    ('Policial',           'VENCIMENTOS',     'NENHUM'),
    ('Policial',           'RELATORIOS',      'NENHUM'),
    ('Policial',           'ACESSOS',         'NENHUM')
) AS v(perfil_nome, modulo_chave, nivel)
JOIN perfil p ON p.nome = v.perfil_nome
JOIN modulo m ON m.chave = v.modulo_chave;

-- ---------- Usuário administrador inicial ----------
-- login: admin / senha: admin123 (TROQUE no primeiro acesso — hash BCrypt abaixo)
INSERT INTO usuario (pessoa_id, login, senha_hash, perfil_id, escopo_unidade_id, ativo)
SELECT NULL, 'admin', '$2a$10$wEwfB7h8lC0LhhxXspzrWev0QxuHJh1e9Ik1FZja7D8LIjJ7kh/gq', p.id, NULL, TRUE
FROM perfil p WHERE p.nome = 'Administrador';
