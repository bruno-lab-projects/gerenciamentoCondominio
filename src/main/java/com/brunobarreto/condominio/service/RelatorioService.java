package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.repository.DespesaRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class RelatorioService {

    private final TemplateEngine templateEngine;
    private final DespesaRepository despesaRepository;

    public RelatorioService(TemplateEngine templateEngine, DespesaRepository despesaRepository) {
        this.templateEngine = templateEngine;
        this.despesaRepository = despesaRepository;
    }

    public byte[] gerarPdfRelatorio(DadosRelatorio dadosRelatorio) {
        try {
            // 1. A mágica da conversão
            YearMonth anoMes = YearMonth.of(dadosRelatorio.getAno(), dadosRelatorio.getMes());
            LocalDate inicio = anoMes.atDay(1); // Dia 01
            LocalDate fim = anoMes.atEndOfMonth(); // Dia 30 ou 31

            // 2. Buscar despesas entre dataInicio e dataFim
            List<Despesa> despesas = despesaRepository.findAll().stream()
                    .filter(d -> !d.getData().isBefore(inicio) 
                            && !d.getData().isAfter(fim))
                    .toList();

            // Calcular receitaTotal
            BigDecimal receitaTotal = dadosRelatorio.getValorCondominio()
                    .multiply(new BigDecimal(dadosRelatorio.getQuantidadePagantes()));

            // Calcular despesaTotal
            BigDecimal despesaTotal = despesas.stream()
                    .map(Despesa::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular saldoAtual
            BigDecimal saldoAtual = dadosRelatorio.getSaldoAnterior()
                    .add(receitaTotal)
                    .subtract(despesaTotal);

            // Criar Context do Thymeleaf
            Context context = new Context();
            context.setVariable("dadosRelatorio", dadosRelatorio);
            context.setVariable("despesas", despesas);
            context.setVariable("receitaTotal", receitaTotal);
            context.setVariable("despesaTotal", despesaTotal);
            context.setVariable("saldoAtual", saldoAtual);
            context.setVariable("dataInicio", inicio);
            context.setVariable("dataFim", fim);

            // Processar template HTML
            String html = templateEngine.process("relatorio-pdf", context);

            // Gerar PDF usando ITextRenderer
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do relatório", e);
        }
    }
    
}
