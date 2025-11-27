package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.service.DespesaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DateTimeException;
import java.time.LocalDate;

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
        model.addAttribute("totalGastos", despesaService.calcularTotal());
        return "lista-despesas";
    }

    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("despesa", new Despesa());
        return "form-despesa";
    }

    @PostMapping
    public String salvar(Despesa despesa,
                         @RequestParam("mesReferencia") Integer mes,
                         @RequestParam("anoReferencia") Integer ano) {
        
        if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
            return "redirect:/despesas?erro=data_invalida";
        }

        try {
            LocalDate dataFormatada = LocalDate.of(ano, mes, 1);
            despesa.setData(dataFormatada);
            
            despesaService.salvar(despesa);
            
        } catch (DateTimeException e) {
            return "redirect:/despesas?erro=data_invalida";
        }

        return "redirect:/despesas";
    }

    @GetMapping("/excluir/{id}")
    public String excluirDespesa(@PathVariable Long id) {
        despesaService.excluir(id);
        return "redirect:/despesas";
    }
    
}
