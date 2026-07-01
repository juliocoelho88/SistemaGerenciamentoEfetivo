package br.gov.pm.gestaoefetivo.efetivo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "situacao_funcional")
@Getter
@Setter
@NoArgsConstructor
public class SituacaoFuncional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String nome;

    @Column(name = "disponivel_operacao", nullable = false)
    private boolean disponivelOperacao = true;
}
