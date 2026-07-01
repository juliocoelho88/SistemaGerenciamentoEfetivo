package br.gov.pm.gestaoefetivo.efetivo;

import org.springframework.data.jpa.domain.Specification;

public final class PessoaSpecifications {

    private PessoaSpecifications() {
    }

    /** Busca livre por nome, RE ou posto (case-insensitive, "contém"). */
    public static Specification<Pessoa> textoLivre(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String termo = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nome")), termo),
                cb.like(cb.lower(root.get("re")), termo),
                cb.like(cb.lower(root.join("posto").get("nome")), termo));
    }

    public static Specification<Pessoa> daUnidade(Long unidadeId) {
        if (unidadeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("unidade").get("id"), unidadeId);
    }

    public static Specification<Pessoa> daSituacao(Long situacaoId) {
        if (situacaoId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("situacao").get("id"), situacaoId);
    }

    public static Specification<Pessoa> comId(Long pessoaId) {
        return (root, query, cb) -> cb.equal(root.get("id"), pessoaId);
    }
}
