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
@Table(name = "habilitacao")
@Getter
@Setter
@NoArgsConstructor
public class Habilitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 60)
    private String categoria;

    @Column(name = "exige_validade", nullable = false)
    private boolean exigeValidade = true;

    @Column(name = "orgao_emissor", length = 120)
    private String orgaoEmissor;

    @Column(name = "validade_meses")
    private Integer validadeMeses;
}
