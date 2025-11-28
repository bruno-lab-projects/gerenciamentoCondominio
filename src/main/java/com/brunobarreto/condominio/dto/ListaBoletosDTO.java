package com.brunobarreto.condominio.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ListaBoletosDTO {
    
    private Integer mes;
    private Integer ano;
    private String nomeMesExtenso;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataVencimento;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataLimite; 

    private BigDecimal valorBase;

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