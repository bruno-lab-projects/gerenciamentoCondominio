package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.service.RelatorioService;
import com.lowagie.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    // Abre a tela de formulário
    @GetMapping("/preparo")
    public String abrirFormulario(Model model) {
        model.addAttribute("dadosRelatorio", new DadosRelatorio());
        return "form-relatorio";
    }

    // Gera o PDF
    @PostMapping("/gerar")
    public ResponseEntity<byte[]> gerarRelatorio(DadosRelatorio dados) {
        try {
            byte[] pdfBytes = service.gerarPdfRelatorio(dados);

            // Configura o navegador para baixar o arquivo
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_condominio.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            // Se der erro no PDF, retorna erro 500 (em produção trataríamos melhor)
            return ResponseEntity.internalServerError().build();
        }
    }
}