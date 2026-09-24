package br.gov.pm.gestaoefetivo.efetivo;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pessoa")
@Getter
@Setter
@NoArgsConstructor
public class Pessoa {

    /**
     * Ids alocados em blocos de 50 (SEQUENCE + optimizer pooled) em vez de IDENTITY: com IDENTITY o
     * Hibernate precisa do id de volta a cada linha e desliga o batch de INSERT, o que inviabiliza a
     * importação em massa de planilhas. O allocationSize precisa continuar igual ao INCREMENT BY da
     * sequence (ver V3__sequencia_pessoa_em_lote.sql).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pessoa_seq")
    @SequenceGenerator(name = "pessoa_seq", sequenceName = "pessoa_id_seq", allocationSize = 50)
    private Long id;

    /** Formato XXX.XXX-D — validado também no DTO com @Pattern; o CHECK do banco é a garantia final. */
    @Column(nullable = false, unique = true, length = 9)
    private String re;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(1)")
    private SexoPessoa sexo;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "data_praca")
    private LocalDate dataPraca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posto_id", nullable = false)
    private PostoGraduacao posto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "situacao_id", nullable = false)
    private SituacaoFuncional situacao;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
