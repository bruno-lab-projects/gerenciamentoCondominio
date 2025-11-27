package com.brunobarreto.condominio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class RelatorioMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer mes;
    private Integer ano;

    private BigDecimal saldoAnterior;
    private BigDecimal receitaTotal;
    private BigDecimal despesaTotal;
    private BigDecimal saldoAtual;
    
    private BigDecimal valorCondominio;
    private Integer qtdePagantesApto;
    private Integer qtdePagantesLoja;

    private LocalDateTime dataGeracao;
}