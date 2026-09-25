-- ============================================================
-- Prepara a tabela pessoa para inserção em lote (importação de planilhas).
--
-- Motivo: com BIGSERIAL + GenerationType.IDENTITY o Hibernate precisa do id
-- gerado a cada linha e, por isso, desabilita o batch de INSERT — cada pessoa
-- vira um round-trip. Alocando os ids por blocos (pooled) o driver consegue
-- agrupar os inserts.
--
-- O INCREMENT BY precisa ser igual ao allocationSize do @SequenceGenerator
-- em Pessoa.java (50). Alterar um sem o outro causa colisão de ids.
-- ============================================================

ALTER SEQUENCE pessoa_id_seq INCREMENT BY 50;
