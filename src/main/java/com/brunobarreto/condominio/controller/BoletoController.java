package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.dto.ListaBoletosDTO;
import com.brunobarreto.condominio.service.BoletoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/boletos")
public class BoletoController {

    private final BoletoService boletoService;

    // O Spring injeta o Service aqui automaticamente
    public BoletoController(BoletoService boletoService) {
        this.boletoService = boletoService;
    }

    @GetMapping("/preparo-em-lote")
    public String preparoEmLote(Model model) {
        model.addAttribute("formBoletos", boletoService.prepararListaParaEdicao());
        return "form-boletos-lote";
    }

    @PostMapping("/gerar-lote")
    public ResponseEntity<byte[]> gerarLote(@ModelAttribute ListaBoletosDTO form) {
        try {
            byte[] pdf = boletoService.gerarPdfLote(form);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibos_condominio.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o erro no console se der ruim
            return ResponseEntity.internalServerError().build();
        }
    }
}