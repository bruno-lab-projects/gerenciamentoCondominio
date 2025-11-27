package com.brunobarreto.condominio.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DadosRelatorio {
    
    private BigDecimal saldoAnterior;
    private BigDecimal valorCondominio;
    private Integer quantidadePagantes;
    
    // Mudamos para ficar mais fácil para sua mãe
    private Integer mes; // 1 a 12
    private Integer ano; // 2025
    
}
