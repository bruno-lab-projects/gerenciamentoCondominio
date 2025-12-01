package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.repository.DespesaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DespesaService {

    private final DespesaRepository despesaRepository;

    public DespesaService(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    public List<Despesa> listarTodas() {
        return despesaRepository.findAll();
    }

    public Despesa salvar(Despesa despesa) {
        if (despesa.getDescricao() != null) {
        despesa.setDescricao(despesa.getDescricao().toUpperCase());
    }
    
        return despesaRepository.save(despesa);
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (!despesaRepository.existsById(id)) {
            throw new IllegalArgumentException("Despesa não encontrada com ID: " + id);
        }
        despesaRepository.deleteById(id);
    }

    public BigDecimal calcularTotal(List<Despesa> despesas) {
        return despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public List<Despesa> listarOrdenadasPorPrioridade(Integer mes, Integer ano) {
        List<Despesa> despesas = despesaRepository.findByMesReferenciaAndAnoReferencia(mes, ano);
        
        // Define a ordem de prioridade das despesas recorrentes
        Map<String, Integer> ordemPrioridade = new HashMap<>();
        ordemPrioridade.put("EMBASA", 1);
        ordemPrioridade.put("COELBA", 2);
        ordemPrioridade.put("ELEVASOL", 3);
        ordemPrioridade.put("RAPAZ DA LIMPEZA", 4);
        
        return despesas.stream()
                .sorted((d1, d2) -> {
                    Integer prioridade1 = ordemPrioridade.getOrDefault(d1.getDescricao(), 999);
                    Integer prioridade2 = ordemPrioridade.getOrDefault(d2.getDescricao(), 999);
                    
                    // Se ambas têm prioridade definida, ordena por prioridade
                    if (!prioridade1.equals(999) && !prioridade2.equals(999)) {
                        return prioridade1.compareTo(prioridade2);
                    }
                    // Se só uma tem prioridade, ela vem primeiro
                    if (!prioridade1.equals(999)) return -1;
                    if (!prioridade2.equals(999)) return 1;
                    // Se nenhuma tem prioridade, ordena por ID (ordem de inserção)
                    return d1.getId().compareTo(d2.getId());
                })
                .collect(Collectors.toList());
    }
    
}
