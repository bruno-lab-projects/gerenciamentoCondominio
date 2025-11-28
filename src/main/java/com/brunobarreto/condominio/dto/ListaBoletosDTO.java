package com.brunobarreto.condominio.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ListaBoletosDTO {
    // Dados Comuns
    private Integer mes;
    private Integer ano;
    private LocalDate dataVencimento;
    private LocalDate dataLimite;
    private BigDecimal valorBase;
    private String nomeMesExtenso;

    // Lista de Apartamentos
    private List<BoletoIndividual> boletos = new ArrayList<>();

    @Data
    public static class BoletoIndividual {
        private String unidade;
        private String nomeMorador;
        private BigDecimal valor;
        private String valorPorExtenso;
        private boolean isento;
    }
}