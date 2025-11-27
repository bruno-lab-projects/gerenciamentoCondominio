package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.dto.DadosRelatorio;
import com.brunobarreto.condominio.service.RelatorioService;
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

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/preparo")
    public String preparo(Model model) {
        model.addAttribute("dadosRelatorio", new DadosRelatorio());
        return "form-relatorio";
    }

    @PostMapping("/gerar")
    public ResponseEntity<byte[]> gerar(DadosRelatorio dadosRelatorio) {
        byte[] pdf = relatorioService.gerarPdfRelatorio(dadosRelatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-condominio.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
}
