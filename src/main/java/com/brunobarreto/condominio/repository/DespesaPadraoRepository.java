package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.DespesaPadrao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespesaPadraoRepository extends JpaRepository<DespesaPadrao, Long> {
    
    List<DespesaPadrao> findByAtivaOrderByOrdemExibicao(Boolean ativa);
    
    List<DespesaPadrao> findAllByOrderByOrdemExibicao();
    
}
