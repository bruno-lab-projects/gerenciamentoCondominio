package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.model.DespesaPadrao;
import com.brunobarreto.condominio.repository.DespesaRepository;
import com.brunobarreto.condominio.service.DespesaService;
import com.brunobarreto.condominio.service.DespesaPadraoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/despesas")
@PreAuthorize("hasRole('ADMIN')")
public class DespesaController {

    private final DespesaService despesaService;
    private final DespesaRepository repository;
    private final DespesaPadraoService despesaPadraoService;

    public DespesaController(DespesaService despesaService, DespesaRepository repository, DespesaPadraoService despesaPadraoService) {
        this.despesaService = despesaService;
        this.repository = repository;
        this.despesaPadraoService = despesaPadraoService;
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

        // 2. Busca as despesas do período
        List<Despesa> listaFiltrada = despesaService.listarOrdenadasPorPrioridade(mes, ano);

        // 3. Manda tudo pra tela
        model.addAttribute("despesas", listaFiltrada);
        model.addAttribute("totalGastos", despesaService.calcularTotal(listaFiltrada));
        
        // Mandamos o mês/ano selecionado de volta pra tela (pra manter o dropdown marcado)
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "lista-despesas";
    }

    @GetMapping("/novo")
    public String mostrarFormulario(Model model,
                                     @RequestParam(value = "mes", required = false) Integer mes,
                                     @RequestParam(value = "ano", required = false) Integer ano) {
        model.addAttribute("despesa", new Despesa());
        
        // Se vier mes/ano na URL, usa eles; senão usa a data atual
        if (mes == null || ano == null) {
            LocalDate hoje = LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }
        
        model.addAttribute("mesPadrao", mes);
        model.addAttribute("anoPadrao", ano);
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
        model.addAttribute("mesPadrao", despesa.getMesReferencia());
        model.addAttribute("anoPadrao", despesa.getAnoReferencia());
        model.addAttribute("modoEdicao", true);
        
        return "form-despesa";
    }

    @PostMapping
    public String salvar(Despesa despesa,
                         @RequestParam("mesReferencia") Integer mes,
                         @RequestParam("anoReferencia") Integer ano) {
        
        if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
            return "redirect:/despesas?erro=data_invalida";
        }

        despesa.setMesReferencia(mes);
        despesa.setAnoReferencia(ano);
        
        despesaService.salvar(despesa);

        return "redirect:/despesas?mes=" + mes + "&ano=" + ano;
    }

    @GetMapping("/excluir/{id}")
    public String excluirDespesa(@PathVariable Long id,
                                  @RequestParam(value = "mes", required = false) Integer mes,
                                  @RequestParam(value = "ano", required = false) Integer ano) {
        despesaService.excluir(id);
        
        // Se tiver mes e ano na URL, mantém; senão redireciona para o mês atual
        if (mes != null && ano != null) {
            return "redirect:/despesas?mes=" + mes + "&ano=" + ano;
        }
        
        return "redirect:/despesas";
    }

    @GetMapping("/padroes")
    @ResponseBody
    public ResponseEntity<?> listarDespesasPadrao(@RequestParam Integer mes, @RequestParam Integer ano) {
        var despesasPadrao = despesaPadraoService.listarAtivas();
        
        // Para cada despesa padrão, verificar se já existe no mês
        var resultado = despesasPadrao.stream().map(dp -> {
            boolean jaExiste = repository.existsByDescricaoAndMesReferenciaAndAnoReferencia(
                dp.getDescricao(), mes, ano
            );
            
            return Map.of(
                "id", dp.getId(),
                "descricao", dp.getDescricao(),
                "valorPadrao", dp.getValorPadrao() != null ? dp.getValorPadrao() : 0,
                "tipo", dp.getTipo().name(),
                "jaExiste", jaExiste
            );
        }).toList();
        
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/criar-da-padrao")
    public String criarDespesaDaPadrao(
            @RequestParam("despesaPadraoId") Long despesaPadraoId,
            @RequestParam("valorHidden") BigDecimal valor,
            @RequestParam("mes") Integer mes,
            @RequestParam("ano") Integer ano,
            @RequestParam(value = "atualizarValorPadrao", required = false, defaultValue = "false") Boolean atualizarValorPadrao) {
        
        try {
            // Validações
            if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
                return "redirect:/despesas?mes=" + mes + "&ano=" + ano + "&erro=data_invalida";
            }

            // Buscar despesa padrão
            DespesaPadrao despesaPadrao = despesaPadraoService.buscarPorId(despesaPadraoId);

            // Verificar se já existe despesa com mesma descrição no mês
            boolean jaExiste = repository.existsByDescricaoAndMesReferenciaAndAnoReferencia(
                despesaPadrao.getDescricao(), mes, ano
            );

            if (jaExiste) {
                return "redirect:/despesas?mes=" + mes + "&ano=" + ano + "&erro=duplicada";
            }

            // Criar nova despesa
            Despesa novaDespesa = new Despesa();
            novaDespesa.setDescricao(despesaPadrao.getDescricao());
            novaDespesa.setValor(valor);
            novaDespesa.setMesReferencia(mes);
            novaDespesa.setAnoReferencia(ano);
            
            despesaService.salvar(novaDespesa);

            // Se for despesa fixa e pediu pra atualizar o valor padrão
            if (atualizarValorPadrao && 
                despesaPadrao.getTipo() == DespesaPadrao.TipoDespesaPadrao.FIXA) {
                despesaPadraoService.atualizarValorPadrao(despesaPadrao.getId(), valor);
            }

            return "redirect:/despesas?mes=" + mes + "&ano=" + ano + "&sucesso=true";

        } catch (Exception e) {
            return "redirect:/despesas?mes=" + mes + "&ano=" + ano + "&erro=generico";
        }
    }
    
    @GetMapping("/padroes/gerenciar")
    public String gerenciarDespesasPadrao(Model model) {
        model.addAttribute("despesasPadrao", despesaPadraoService.listarTodas());
        return "gerenciar-despesas-padrao";
    }
    
    @PostMapping("/padroes/toggle-ativa/{id}")
    public String toggleAtivaDespesaPadrao(@PathVariable Long id) {
        despesaPadraoService.toggleAtiva(id);
        return "redirect:/despesas/padroes/gerenciar";
    }
    
    @GetMapping("/padroes/novo")
    public String novaDespesaPadrao(Model model) {
        model.addAttribute("despesaPadrao", new DespesaPadrao());
        model.addAttribute("modoEdicao", false);
        return "form-despesa-padrao";
    }
    
    @GetMapping("/padroes/editar/{id}")
    public String editarDespesaPadrao(@PathVariable Long id, Model model) {
        DespesaPadrao despesaPadrao = despesaPadraoService.buscarPorId(id);
        model.addAttribute("despesaPadrao", despesaPadrao);
        model.addAttribute("modoEdicao", true);
        return "form-despesa-padrao";
    }
    
    @PostMapping("/padroes/salvar")
    public String salvarDespesaPadrao(DespesaPadrao despesaPadrao) {
        despesaPadraoService.salvar(despesaPadrao);
        return "redirect:/despesas/padroes/gerenciar";
    }
    
    @GetMapping("/padroes/excluir/{id}")
    public String excluirDespesaPadrao(@PathVariable Long id) {
        despesaPadraoService.excluir(id);
        return "redirect:/despesas/padroes/gerenciar";
    }
    
}
