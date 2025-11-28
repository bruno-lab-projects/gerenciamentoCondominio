package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.model.RelatorioMensal;
import com.brunobarreto.condominio.repository.DespesaRepository;
import com.brunobarreto.condominio.repository.RelatorioMensalRepository;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class RelatorioService {

    private final DespesaRepository despesaRepository;
    private final RelatorioMensalRepository relatorioRepository;
    private final TemplateEngine templateEngine;

    public RelatorioService(DespesaRepository despesaRepository, 
                           RelatorioMensalRepository relatorioRepository,
                           TemplateEngine templateEngine) {
        this.despesaRepository = despesaRepository;
        this.relatorioRepository = relatorioRepository;
        this.templateEngine = templateEngine;
    }

    public byte[] gerarPdfRelatorio(DadosRelatorio dados) throws DocumentException {
        // 1. Definir o intervalo de datas baseado no Mês/Ano escolhido
        YearMonth anoMes = YearMonth.of(dados.getAno(), dados.getMes());
        LocalDate inicio = anoMes.atDay(1);
        LocalDate fim = anoMes.atEndOfMonth();

        // 2. Buscar despesas do banco
        List<Despesa> despesas = despesaRepository.findByDataBetween(inicio, fim);

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

    // --- MÉTODO 1: SALVAR O FECHAMENTO (Síndica) ---
    public void processarESalvarRelatorio(DadosRelatorio dados) {
        // ... (Cálculos de datas e despesas continuam iguais) ...
        YearMonth anoMes = YearMonth.of(dados.getAno(), dados.getMes());
        LocalDate inicio = anoMes.atDay(1);
        LocalDate fim = anoMes.atEndOfMonth();
        List<Despesa> despesas = despesaRepository.findByDataBetween(inicio, fim);

        // ... (Cálculos matemáticos de receita/despesa continuam iguais) ...
        BigDecimal receitaAptos = dados.getValorCondominio().multiply(new BigDecimal(dados.getQtdePagantesApto()));
        BigDecimal receitaLoja = dados.getValorCondominio().multiply(new BigDecimal("2")).multiply(new BigDecimal(dados.getQtdePagantesLoja()));
        BigDecimal receitaTotal = receitaAptos.add(receitaLoja);
        BigDecimal despesaTotal = despesas.stream().map(Despesa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAtual = dados.getSaldoAnterior().add(receitaTotal).subtract(despesaTotal);

        // --- AQUI MUDA: Verificar se já existe ---
        RelatorioMensal relatorio = relatorioRepository.findByMesAndAno(dados.getMes(), dados.getAno())
                .orElse(new RelatorioMensal()); // Se não achar, cria um novo. Se achar, usa o existente (atualiza).

        // Atualiza os dados (seja novo ou velho)
        relatorio.setMes(dados.getMes());
        relatorio.setAno(dados.getAno());
        relatorio.setSaldoAnterior(dados.getSaldoAnterior());
        relatorio.setValorCondominio(dados.getValorCondominio());
        relatorio.setQtdePagantesApto(dados.getQtdePagantesApto());
        relatorio.setQtdePagantesLoja(dados.getQtdePagantesLoja());
        
        // Totais calculados
        relatorio.setReceitaTotal(receitaTotal);
        relatorio.setDespesaTotal(despesaTotal);
        relatorio.setSaldoAtual(saldoAtual);
        relatorio.setDataGeracao(LocalDateTime.now());

        relatorioRepository.save(relatorio);
    }

    // Novo método para carregar os dados na tela de edição
    public DadosRelatorio buscarDadosParaEdicao(Long id) {
        RelatorioMensal r = relatorioRepository.findById(id).orElseThrow();
        
        DadosRelatorio dados = new DadosRelatorio();
        dados.setMes(r.getMes());
        dados.setAno(r.getAno());
        dados.setSaldoAnterior(r.getSaldoAnterior());
        dados.setValorCondominio(r.getValorCondominio());
        dados.setQtdePagantesApto(r.getQtdePagantesApto());
        dados.setQtdePagantesLoja(r.getQtdePagantesLoja());
        
        return dados;
    }

    // --- MÉTODO 2: LISTAR TUDO (Para a tela do morador) ---
    public List<RelatorioMensal> listarTodos() {
        return relatorioRepository.findAllByOrderByAnoDescMesDesc();
    }

    // --- MÉTODO 3: GERAR O PDF A PARTIR DO BANCO (Download) ---
    public byte[] gerarPdfPorId(Long id) throws DocumentException {
        // Busca o relatório salvo
        RelatorioMensal relatorio = relatorioRepository.findById(id).orElseThrow();
        
        // Busca as despesas de novo (para listar no detalhe)
        YearMonth anoMes = YearMonth.of(relatorio.getAno(), relatorio.getMes());
        List<Despesa> despesas = despesaRepository.findByDataBetween(anoMes.atDay(1), anoMes.atEndOfMonth());

        // Recalcula a receita separada (apenas para exibição no PDF)
        BigDecimal receitaAptos = relatorio.getValorCondominio().multiply(new BigDecimal(relatorio.getQtdePagantesApto()));
        BigDecimal receitaLoja = relatorio.getValorCondominio().multiply(new BigDecimal("2")).multiply(new BigDecimal(relatorio.getQtdePagantesLoja()));

        // Monta o Context
        Context context = new Context();
        context.setVariable("dados", relatorio); // O HTML vai ler direto do objeto salvo
        context.setVariable("despesas", despesas);
        context.setVariable("receitaAptos", receitaAptos);
        context.setVariable("receitaLoja", receitaLoja);
        context.setVariable("receitaTotal", relatorio.getReceitaTotal());
        context.setVariable("despesaTotal", relatorio.getDespesaTotal());
        context.setVariable("saldoAtual", relatorio.getSaldoAtual());
        
        // Gera o PDF (igual antes)
        String html = templateEngine.process("relatorio-pdf", context);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(os);
        return os.toByteArray();
    }
}