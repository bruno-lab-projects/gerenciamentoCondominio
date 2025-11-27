package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.service.DespesaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/despesas")
public class DespesaController {

    private final DespesaService despesaService;

    public DespesaController(DespesaService despesaService) {
        this.despesaService = despesaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("despesas", despesaService.listarTodas());
        return "lista-despesas";
    }

    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("despesa", new Despesa());
        return "form-despesa";
    }

    @PostMapping
    public String salvar(Despesa despesa) {
        despesaService.salvar(despesa);
        return "redirect:/despesas";
    }
    
}
