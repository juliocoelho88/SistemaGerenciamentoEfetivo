package br.gov.pm.gestaoefetivo.formacao;

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
@Table(name = "curso")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 120)
    private String instituicao;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "exige_validade", nullable = false)
    private boolean exigeValidade = false;

    @Column(name = "validade_meses")
    private Integer validadeMeses;
}
