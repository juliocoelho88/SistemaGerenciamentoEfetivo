package br.gov.pm.gestaoefetivo.efetivo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posto_graduacao")
@Getter
@Setter
@NoArgsConstructor
public class PostoGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 12)
    private String abreviacao;

    @Column(name = "ordem_hierarquica", nullable = false)
    private Integer ordemHierarquica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CirculoPosto circulo;
}
