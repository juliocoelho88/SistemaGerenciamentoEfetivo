package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.efetivo.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pessoa_curso")
@Getter
@Setter
@NoArgsConstructor
public class PessoaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "certificado_url", length = 255)
    private String certificadoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusPessoaCurso status = StatusPessoaCurso.CONCLUIDO;
}
