package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.service.RelatorioService;

import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    // 1. TELA PRINCIPAL (Lista de Histórico)
    // Acessível por ADMIN e MORADOR
    @GetMapping
    public String listarRelatorios(Model model) {
        model.addAttribute("relatorios", service.listarTodos());
        return "lista-relatorios"; // Vamos criar esse HTML
    }

    // 2. FORMULÁRIO (Só ADMIN)
    @GetMapping("/novo")
    @PreAuthorize("hasRole('ADMIN')")
    public String novoRelatorio(Model model) {
        DadosRelatorio dadosRelatorio = new DadosRelatorio();
        
        // Busca o último valor de condomínio usado (se existir)
        service.listarTodos().stream()
            .findFirst()
            .ifPresentOrElse(
                ultimoRelatorio -> dadosRelatorio.setValorCondominio(ultimoRelatorio.getValorCondominio()),
                () -> dadosRelatorio.setValorCondominio(new java.math.BigDecimal("500.00")) // Padrão R$ 500
            );
        
        model.addAttribute("dadosRelatorio", dadosRelatorio);
        
        LocalDate hoje = LocalDate.now();
        model.addAttribute("mesPadrao", hoje.getMonthValue());
        model.addAttribute("anoPadrao", hoje.getYear());
        
        return "form-relatorio";
    }

    // 3. SALVAR (Só ADMIN) - Agora salva e volta pra lista
    @PostMapping("/salvar")
    @PreAuthorize("hasRole('ADMIN')")
    public String salvarRelatorio(DadosRelatorio dados) {
        service.processarESalvarRelatorio(dados);
        return "redirect:/relatorios";
    }

    // 4. DOWNLOAD PDF (Qualquer um)
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().build();
            }
            
            byte[] pdf = service.gerarPdfPorId(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.pdf");
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Rota para abrir o formulário de edição
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarRelatorio(@PathVariable Long id, Model model) {
        // Busca os dados antigos convertidos para DTO
        DadosRelatorio dadosAntigos = service.buscarDadosParaEdicao(id);
        
        model.addAttribute("dadosRelatorio", dadosAntigos);
        
        model.addAttribute("mesPadrao", dadosAntigos.getMes());
        model.addAttribute("anoPadrao", dadosAntigos.getAno());
        
        return "form-relatorio";
    }

    @GetMapping("/excluir/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluirRelatorio(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/relatorios";
    }
}