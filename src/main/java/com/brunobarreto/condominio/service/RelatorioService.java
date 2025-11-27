package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.repository.DespesaRepository;
import com.lowagie.text.DocumentException;
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

    private final DespesaRepository repository;
    private final TemplateEngine templateEngine;

    public RelatorioService(DespesaRepository repository, TemplateEngine templateEngine) {
        this.repository = repository;
        this.templateEngine = templateEngine;
    }

    public byte[] gerarPdfRelatorio(DadosRelatorio dados) throws DocumentException {
        // 1. Definir o intervalo de datas baseado no Mês/Ano escolhido
        YearMonth anoMes = YearMonth.of(dados.getAno(), dados.getMes());
        LocalDate inicio = anoMes.atDay(1);
        LocalDate fim = anoMes.atEndOfMonth();

        // 2. Buscar despesas do banco
        List<Despesa> despesas = repository.findByDataBetween(inicio, fim);

        // 3. Calcular Totais
        
        // --- LÓGICA NOVA DE CÁLCULO ---
        
        // 3.1. Receita dos Apartamentos (Valor Base * Qtde Apts)
        BigDecimal receitaAptos = dados.getValorCondominio()
                .multiply(new BigDecimal(dados.getQtdePagantesApto()));

        // 3.2. Receita da Loja (Valor Base * 2 * Qtde Loja)
        // Multiplicamos por 2 pois a loja paga o dobro
        BigDecimal valorCotaLoja = dados.getValorCondominio().multiply(new BigDecimal("2"));
        BigDecimal receitaLoja = valorCotaLoja
                .multiply(new BigDecimal(dados.getQtdePagantesLoja()));

        // 3.3. Receita Total Somada
        BigDecimal receitaTotal = receitaAptos.add(receitaLoja);

        // --- FIM DA LÓGICA NOVA ---

        // Despesa Total = Soma de todas as despesas da lista
        BigDecimal despesaTotal = despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Saldo Atual = Saldo Anterior + Receita - Despesa
        BigDecimal saldoAtual = dados.getSaldoAnterior().add(receitaTotal).subtract(despesaTotal);

        // 4. Preparar o Contexto (Variáveis que vão para o HTML)
        Context context = new Context();
        context.setVariable("despesas", despesas);
        context.setVariable("dados", dados); // Passamos o DTO inteiro (tem mês, ano, saldo ant...)
        context.setVariable("receitaAptos", receitaAptos);
        context.setVariable("receitaLoja", receitaLoja);
        context.setVariable("receitaTotal", receitaTotal);
        context.setVariable("despesaTotal", despesaTotal);
        context.setVariable("saldoAtual", saldoAtual);
        context.setVariable("dataInicio", inicio);
        context.setVariable("dataFim", fim);

        // 5. Renderizar o HTML para String
        String htmlRenderizado = templateEngine.process("relatorio-pdf", context);

        // 6. Converter String HTML para PDF (Bytes)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlRenderizado);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}