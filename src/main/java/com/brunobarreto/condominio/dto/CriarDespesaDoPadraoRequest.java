package com.brunobarreto.condominio.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CriarDespesaDoPadraoRequest {
    private Long despesaPadraoId;
    private BigDecimal valor;
    private Integer mes;
    private Integer ano;
    private Boolean atualizarValorPadrao = false;
}
