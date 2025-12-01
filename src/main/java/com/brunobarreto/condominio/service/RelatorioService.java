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
import java.time.LocalDateTime;
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
        // 1. Buscar despesas do banco
        List<Despesa> despesas = despesaRepository.findByMesReferenciaAndAnoReferencia(dados.getMes(), dados.getAno());

        // 2. Calcular Totais
        BigDecimal receitaAptos = dados.getValorCondominio()
                .multiply(new BigDecimal(dados.getQtdePagantesApto()));

        BigDecimal valorCotaLoja = dados.getValorCondominio().multiply(new BigDecimal("2"));
        BigDecimal receitaLoja = valorCotaLoja
                .multiply(new BigDecimal(dados.getQtdePagantesLoja()));

        BigDecimal receitaTotal = receitaAptos.add(receitaLoja);

        BigDecimal despesaTotal = despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoAtual = dados.getSaldoAnterior().add(receitaTotal).subtract(despesaTotal);

        // 3. Preparar o Contexto
        Context context = new Context();
        context.setVariable("despesas", despesas);
        context.setVariable("dados", dados);
        context.setVariable("receitaAptos", receitaAptos);
        context.setVariable("receitaLoja", receitaLoja);
        context.setVariable("receitaTotal", receitaTotal);
        context.setVariable("despesaTotal", despesaTotal);
        context.setVariable("saldoAtual", saldoAtual);

        // 4. Renderizar o HTML para String
        String htmlRenderizado = templateEngine.process("relatorio-pdf", context);

        // 5. Converter String HTML para PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlRenderizado);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }

    public void processarESalvarRelatorio(DadosRelatorio dados) {
        List<Despesa> despesas = despesaRepository.findByMesReferenciaAndAnoReferencia(dados.getMes(), dados.getAno());

        BigDecimal receitaAptos = dados.getValorCondominio().multiply(new BigDecimal(dados.getQtdePagantesApto()));
        BigDecimal receitaLoja = dados.getValorCondominio().multiply(new BigDecimal("2")).multiply(new BigDecimal(dados.getQtdePagantesLoja()));
        BigDecimal receitaTotal = receitaAptos.add(receitaLoja);
        BigDecimal despesaTotal = despesas.stream().map(Despesa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAtual = dados.getSaldoAnterior().add(receitaTotal).subtract(despesaTotal);

        RelatorioMensal relatorio = relatorioRepository.findByMesAndAno(dados.getMes(), dados.getAno())
                .orElse(new RelatorioMensal());

        relatorio.setMes(dados.getMes());
        relatorio.setAno(dados.getAno());
        relatorio.setSaldoAnterior(dados.getSaldoAnterior());
        relatorio.setValorCondominio(dados.getValorCondominio());
        relatorio.setQtdePagantesApto(dados.getQtdePagantesApto());
        relatorio.setQtdePagantesLoja(dados.getQtdePagantesLoja());
        
        relatorio.setReceitaTotal(receitaTotal);
        relatorio.setDespesaTotal(despesaTotal);
        relatorio.setSaldoAtual(saldoAtual);
        relatorio.setDataGeracao(LocalDateTime.now());

        relatorioRepository.save(relatorio);
    }

    // Novo método para carregar os dados na tela de edição
    public DadosRelatorio buscarDadosParaEdicao(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        RelatorioMensal r = relatorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado com ID: " + id));
        
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

    public byte[] gerarPdfPorId(Long id) throws DocumentException {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        RelatorioMensal relatorio = relatorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado com ID: " + id));
        
        List<Despesa> despesas = despesaRepository.findByMesReferenciaAndAnoReferencia(relatorio.getMes(), relatorio.getAno());

        BigDecimal receitaAptos = relatorio.getValorCondominio().multiply(new BigDecimal(relatorio.getQtdePagantesApto()));
        BigDecimal receitaLoja = relatorio.getValorCondominio().multiply(new BigDecimal("2")).multiply(new BigDecimal(relatorio.getQtdePagantesLoja()));

        Context context = new Context();
        context.setVariable("dados", relatorio);
        context.setVariable("despesas", despesas);
        context.setVariable("receitaAptos", receitaAptos);
        context.setVariable("receitaLoja", receitaLoja);
        context.setVariable("receitaTotal", relatorio.getReceitaTotal());
        context.setVariable("despesaTotal", relatorio.getDespesaTotal());
        context.setVariable("saldoAtual", relatorio.getSaldoAtual());
        
        String html = templateEngine.process("relatorio-pdf", context);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(os);
        return os.toByteArray();
    }

    // Adicione este método no RelatorioService
    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (!relatorioRepository.existsById(id)) {
            throw new IllegalArgumentException("Relatório não encontrado com ID: " + id);
        }
        relatorioRepository.deleteById(id);
    }
}