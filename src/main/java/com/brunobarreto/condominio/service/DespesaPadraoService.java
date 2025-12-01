package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.model.DespesaPadrao;
import com.brunobarreto.condominio.repository.DespesaPadraoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DespesaPadraoService {

    private final DespesaPadraoRepository repository;

    public DespesaPadraoService(DespesaPadraoRepository repository) {
        this.repository = repository;
    }

    public List<DespesaPadrao> listarAtivas() {
        return repository.findByAtivaOrderByOrdemExibicao(true);
    }
    
    public List<DespesaPadrao> listarTodas() {
        return repository.findAllByOrderByOrdemExibicao();
    }

    public DespesaPadrao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa padrão não encontrada: " + id));
    }

    public void atualizarValorPadrao(Long id, BigDecimal novoValor) {
        DespesaPadrao despesaPadrao = buscarPorId(id);
        despesaPadrao.setValorPadrao(novoValor);
        repository.save(despesaPadrao);
    }
    
    public void toggleAtiva(Long id) {
        DespesaPadrao despesaPadrao = buscarPorId(id);
        despesaPadrao.setAtiva(!despesaPadrao.getAtiva());
        repository.save(despesaPadrao);
    }
    
    public DespesaPadrao salvar(DespesaPadrao despesaPadrao) {
        if (despesaPadrao.getDescricao() != null) {
            despesaPadrao.setDescricao(despesaPadrao.getDescricao().toUpperCase());
        }
        
        // Se não definiu ordem, busca a próxima disponível
        if (despesaPadrao.getOrdemExibicao() == null) {
            Integer maxOrdem = repository.findAll().stream()
                    .map(DespesaPadrao::getOrdemExibicao)
                    .max(Integer::compareTo)
                    .orElse(0);
            despesaPadrao.setOrdemExibicao(maxOrdem + 1);
        }
        
        return repository.save(despesaPadrao);
    }
    
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Despesa padrão não encontrada: " + id);
        }
        repository.deleteById(id);
    }
}
