package com.brunobarreto.condominio.util;

import java.math.BigDecimal;

public class ConversorExtenso {

    private static final String[] UNIDADES = {"", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove"};
    private static final String[] DEZENAS = {"", "dez", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"};
    private static final String[] DEZ_A_DEZENOVE = {"dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"};
    private static final String[] CENTENAS = {"", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"};

    public static String escrever(BigDecimal valor) {
        if (valor == null) return "";
        
        int totalCentavos = valor.multiply(new BigDecimal("100")).intValue();
        int reais = totalCentavos / 100;
        int centavos = totalCentavos % 100;
        
        StringBuilder extenso = new StringBuilder();
        
        if (reais > 0) {
            extenso.append(converterReais(reais)).append(" reais");
        }
        
        if (centavos > 0) {
            if (reais > 0) extenso.append(" e ");
            extenso.append(converterAte999(centavos)).append(" centavos");
        }
        
        return extenso.toString();
    }

    private static String converterReais(int valor) {
        if (valor == 0) return "";
        
        int milhar = valor / 1000;
        int resto = valor % 1000;
        
        String texto = "";
        
        if (milhar > 0) {
            if (milhar == 1) texto += "um mil";
            else texto += converterAte999(milhar) + " mil";
            
            if (resto > 0) {
                if (resto < 100 || resto % 100 == 0) texto += " e ";
                else texto += ", ";
            }
        }
        
        if (resto > 0 || milhar == 0) {
            texto += converterAte999(resto);
        }
        
        return texto;
    }

    private static String converterAte999(int numero) {
        if (numero == 0) return "";
        if (numero == 100) return "cem";

        String texto = "";
        int c = numero / 100;
        int resto100 = numero % 100;
        int d = resto100 / 10;
        int u = resto100 % 10;

        if (c > 0) {
            texto += CENTENAS[c];
            if (resto100 > 0) texto += " e ";
        }

        if (resto100 >= 10 && resto100 <= 19) {
            texto += DEZ_A_DEZENOVE[resto100 - 10];
        } else {
            if (d > 0) {
                texto += DEZENAS[d];
                if (u > 0) texto += " e ";
            }
            if (u > 0) {
                texto += UNIDADES[u];
            }
        }
        return texto;
    }
}