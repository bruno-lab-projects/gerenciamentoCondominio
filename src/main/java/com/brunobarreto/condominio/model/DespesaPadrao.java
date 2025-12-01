package com.brunobarreto.condominio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "despesa_padrao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespesaPadrao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String descricao;

    private BigDecimal valorPadrao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDespesaPadrao tipo;

    @Column(nullable = false)
    private Integer ordemExibicao;

    @Column(nullable = false)
    private Boolean ativa = true;

    public enum TipoDespesaPadrao {
        FIXA,
        VARIAVEL
    }
}
