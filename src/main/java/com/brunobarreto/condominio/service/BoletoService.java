package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.dto.ListaBoletosDTO;
import com.brunobarreto.condominio.model.Unidade;
import com.brunobarreto.condominio.repository.UnidadeRepository;
import com.brunobarreto.condominio.util.ConversorExtenso;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BoletoService {

    private final TemplateEngine templateEngine;
    private final UnidadeRepository unidadeRepository;
    
    @Value("${condominio.sindica.nome:}")
    private String nomeSindica;

    public BoletoService(TemplateEngine templateEngine, UnidadeRepository unidadeRepository) {
        this.templateEngine = templateEngine;
        this.unidadeRepository = unidadeRepository;
    }

    // --- CARREGA DO BANCO PRA TELA ---
    public ListaBoletosDTO prepararListaParaEdicao() {
        ListaBoletosDTO form = new ListaBoletosDTO();
        
        // Pega a data de HOJE
        LocalDate hoje = LocalDate.now();
        
        // 1. Mês de Referência = Mês Atual
        form.setMes(hoje.getMonthValue());
        form.setAno(hoje.getYear());
        
        // 2. Vencimento = Dia 01 do Mês Seguinte
        form.setDataVencimento(hoje.plusMonths(1).withDayOfMonth(1)); 
        
        // 3. Prazo Limite = Dia 05 do Mês Seguinte
        form.setDataLimite(hoje.plusMonths(1).withDayOfMonth(5)); 
        
        form.setValorBase(new BigDecimal("500.00")); 

        List<ListaBoletosDTO.BoletoIndividual> lista = new ArrayList<>();
        
        // Pega todas as unidades salvas no banco (Apto 11, 12... Loja)
        List<Unidade> unidadesSalvas = unidadeRepository.findAll();

        for (Unidade u : unidadesSalvas) {
            ListaBoletosDTO.BoletoIndividual boleto = new ListaBoletosDTO.BoletoIndividual();
            boleto.setUnidade(u.getNomeUnidade());
            boleto.setNomeMorador(u.getNomeMorador()); // Já vem com o nome salvo se tiver
            
            // Regra do valor (Se for loja, dobro)
            if (u.getNomeUnidade().contains("Loja")) {
                boleto.setValor(form.getValorBase().multiply(new BigDecimal("2")));
            } else {
                boleto.setValor(form.getValorBase());
            }
            
            lista.add(boleto);
        }

        form.setBoletos(lista);
        return form;
    }

    // --- SALVA NO BANCO E GERA PDF ---
    public byte[] gerarPdfLote(ListaBoletosDTO form) throws DocumentException {
        
        // 1. Converter número do mês para Nome (Ex: 11 -> NOVEMBRO)
        String nomeMes = Month.of(form.getMes())
                .getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"))
                .toUpperCase();
        form.setNomeMesExtenso(nomeMes);

        // 2. Atualizar nomes no banco e preparar dados
        List<Unidade> unidadesBanco = unidadeRepository.findAll();
        
        // Garante que não vai estourar o índice se tiver tamanhos diferentes
        int tamanho = Math.min(unidadesBanco.size(), form.getBoletos().size());

        for (int i = 0; i < tamanho; i++) {
            Unidade u = unidadesBanco.get(i);
            ListaBoletosDTO.BoletoIndividual formItem = form.getBoletos().get(i);
            
            // Salva nome se mudou (Persistência)
            if (!u.getNomeMorador().equals(formItem.getNomeMorador())) {
                u.setNomeMorador(formItem.getNomeMorador());
                unidadeRepository.save(u);
            }
            
            // --- LÓGICA DA ISENÇÃO (APTO 51) ---
            if (formItem.getUnidade().contains("51")) {
                formItem.setIsento(true);
                formItem.setValor(BigDecimal.ZERO);
                formItem.setValorPorExtenso("ISENTO");
            } else {
                formItem.setIsento(false);
                // Calcula extenso normal
                formItem.setValorPorExtenso(ConversorExtenso.escrever(formItem.getValor()));
            }
        }

        // 3. Processar HTML com Thymeleaf (Gera o HTML "Sujo")
        Context context = new Context();
        context.setVariable("form", form);
        
        // Define texto da assinatura: usa nome se configurado, senão usa texto padrão
        System.out.println("DEBUG - Valor de nomeSindica: [" + nomeSindica + "]");
        String textoAssinatura = (nomeSindica != null && !nomeSindica.trim().isEmpty()) 
            ? nomeSindica 
            : "Assinatura da síndica ou subsíndica";
        System.out.println("DEBUG - Texto assinatura definido: [" + textoAssinatura + "]");
        context.setVariable("textoAssinatura", textoAssinatura);
        
        String htmlSujo = templateEngine.process("template-recibos", context);

        // --- 4. A MÁGICA DA CORREÇÃO (JSOUP) ---
        // O Jsoup pega o HTML e fecha todas as tags, remove lixo e deixa perfeito pro PDF
        Document doc = Jsoup.parse(htmlSujo);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // Força modo XML estrito
        String htmlLimpo = doc.html();
        
        // 5. Gerar PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        
        // Registrar a fonte Allura personalizada
        try {
            String fontPath = new ClassPathResource("static/fonts/Allura-Regular.ttf").getURL().toString();
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            System.err.println("Erro ao carregar fonte Allura: " + e.getMessage());
        }
        
        // Agora passamos o htmlLimpo em vez do sujo
        renderer.setDocumentFromString(htmlLimpo);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}