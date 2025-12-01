package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.repository.DespesaRepository;
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
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/despesas")
public class DespesaController {

    private final DespesaService despesaService;
    private final DespesaRepository repository;

    public DespesaController(DespesaService despesaService, DespesaRepository repository) {
        this.despesaService = despesaService;
        this.repository = repository;
    }

    @GetMapping
    public String listar(Model model,
                         @RequestParam(value = "mes", required = false) Integer mes,
                         @RequestParam(value = "ano", required = false) Integer ano) {
        
        // 1. Define o padrão: Se não veio nada na URL, usa a data de HOJE
        if (mes == null || ano == null) {
            LocalDate hoje = LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        // 2. Calcula o intervalo (Dia 1 até o último dia do mês)
        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDate dataInicio = anoMes.atDay(1);
        LocalDate dataFim = anoMes.atEndOfMonth();

        // 3. Busca as despesas do período
        List<Despesa> listaFiltrada = repository.findByDataBetween(dataInicio, dataFim);

        // 4. Manda tudo pra tela
        model.addAttribute("despesas", listaFiltrada);
        model.addAttribute("totalGastos", despesaService.calcularTotal(listaFiltrada));
        
        // Mandamos o mês/ano selecionado de volta pra tela (pra manter o dropdown marcado)
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "lista-despesas";
    }

    @GetMapping("/novo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("despesa", new Despesa());
        
        LocalDate hoje = LocalDate.now();
        
        model.addAttribute("mesPadrao", hoje.getMonthValue());
        model.addAttribute("anoPadrao", hoje.getYear());
        model.addAttribute("modoEdicao", false);
        
        return "form-despesa";
    }
    
    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id, Model model) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        Despesa despesa = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada: " + id));
        
        model.addAttribute("despesa", despesa);
        
        // Preenche mês e ano da despesa existente
        LocalDate dataDespesa = despesa.getData();
        model.addAttribute("mesPadrao", dataDespesa.getMonthValue());
        model.addAttribute("anoPadrao", dataDespesa.getYear());
        model.addAttribute("dataPagamento", dataDespesa.toString()); // Formato yyyy-MM-dd para input type="date"
        model.addAttribute("modoEdicao", true);
        
        return "form-despesa";
    }

    @PostMapping
    public String salvar(Despesa despesa,
                         @RequestParam("mesReferencia") Integer mes,
                         @RequestParam("anoReferencia") Integer ano,
                         @RequestParam(value = "dataPagamento", required = false) String dataPagamento) {
        
        if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
            return "redirect:/despesas?erro=data_invalida";
        }

        try {
            LocalDate dataFinal;
            
            // Se a data do pagamento foi informada, usa ela
            if (dataPagamento != null && !dataPagamento.isEmpty()) {
                dataFinal = LocalDate.parse(dataPagamento);
            } else {
                // Senão, usa o dia 1 do mês de referência
                dataFinal = LocalDate.of(ano, mes, 1);
            }
            
            despesa.setData(dataFinal);
            
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
