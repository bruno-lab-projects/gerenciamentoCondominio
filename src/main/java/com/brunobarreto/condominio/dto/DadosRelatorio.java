package com.brunobarreto.condominio.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DadosRelatorio {
    
    private BigDecimal saldoAnterior;
    private BigDecimal valorCondominio;
    
    private Integer qtdePagantesApto;
    private Integer qtdePagantesLoja;
    
    private Integer mes;
    private Integer ano;
    
}
