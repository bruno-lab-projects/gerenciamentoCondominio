package com.brunobarreto.condominio.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DadosRelatorio {
    
    private BigDecimal saldoAnterior;
    private BigDecimal valorCondominio; // Valor base (do apartamento)
    
    // Pagantes separados por tipo
    private Integer qtdePagantesApto; // Ex: 9, 10 ou 11
    private Integer qtdePagantesLoja; // Ex: 0 ou 1
    
    // Mudamos para ficar mais fácil para sua mãe
    private Integer mes; // 1 a 12
    private Integer ano; // 2025
    
}
