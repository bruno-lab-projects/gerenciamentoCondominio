package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.dto.ListaBoletosDTO;
import com.brunobarreto.condominio.model.Unidade;
import com.brunobarreto.condominio.repository.UnidadeRepository; // Import novo
import com.brunobarreto.condominio.util.ConversorExtenso;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class BoletoService {

    private final TemplateEngine templateEngine;
    private final UnidadeRepository unidadeRepository; // Injeta o banco

    public BoletoService(TemplateEngine templateEngine, UnidadeRepository unidadeRepository) {
        this.templateEngine = templateEngine;
        this.unidadeRepository = unidadeRepository;
    }

    // --- CARREGA DO BANCO PRA TELA ---
    public ListaBoletosDTO prepararListaParaEdicao() {
        ListaBoletosDTO form = new ListaBoletosDTO();
        LocalDate hoje = LocalDate.now();
        
        form.setMes(hoje.getMonthValue());
        form.setAno(hoje.getYear());
        form.setDataVencimento(hoje.plusMonths(1).withDayOfMonth(1));
        form.setDataLimite(hoje.plusMonths(1).withDayOfMonth(5));
        form.setValorBase(new BigDecimal("500.00")); 

        List<ListaBoletosDTO.BoletoIndividual> lista = new ArrayList<>();
        
        // Pega todos as unidades salvas no banco (Apto 1... Loja)
        List<Unidade> unidadesSalvas = unidadeRepository.findAll();

        for (Unidade u : unidadesSalvas) {
            ListaBoletosDTO.BoletoIndividual boleto = new ListaBoletosDTO.BoletoIndividual();
            boleto.setUnidade(u.getNomeUnidade());
            boleto.setNomeMorador(u.getNomeMorador()); // JÁ VEM COM O NOME SALVO!
            
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
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
                .toUpperCase();
        form.setNomeMesExtenso(nomeMes);

        // 2. Atualizar nomes no banco e preparar dados
        List<Unidade> unidadesBanco = unidadeRepository.findAll();
        
        for (int i = 0; i < unidadesBanco.size(); i++) {
            Unidade u = unidadesBanco.get(i);
            ListaBoletosDTO.BoletoIndividual formItem = form.getBoletos().get(i);
            
            // Salva nome se mudou
            if (!u.getNomeMorador().equals(formItem.getNomeMorador())) {
                u.setNomeMorador(formItem.getNomeMorador());
                unidadeRepository.save(u);
            }
            
            // --- LÓGICA DA ISENÇÃO (APTO 51) ---
            // Verifica se a unidade é o "Apto 51" (ou o nome exato que você cadastrou)
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

        // 3. Gerar PDF
        Context context = new Context();
        context.setVariable("form", form); 

        String htmlRenderizado = templateEngine.process("template-recibos", context);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlRenderizado);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}