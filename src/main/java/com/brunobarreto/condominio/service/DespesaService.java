package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.model.Despesa;
import com.brunobarreto.condominio.repository.DespesaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
        return despesaRepository.save(despesa);
    }

    public void excluir(Long id) {
        despesaRepository.deleteById(id);
    }

    public BigDecimal calcularTotal(List<Despesa> despesas) {
        return despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
}
