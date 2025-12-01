package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.RelatorioMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelatorioMensalRepository extends JpaRepository<RelatorioMensal, Long> {
    List<RelatorioMensal> findAllByOrderByAnoDescMesDesc();
    Optional<RelatorioMensal> findByMesAndAno(Integer mes, Integer ano);
    
    /**
     * Busca o relatório mais recente de um determinado mês/ano
     * Útil quando há múltiplos relatórios gerados no mesmo período
     */
    @Query("SELECT r FROM RelatorioMensal r WHERE r.mes = :mes AND r.ano = :ano ORDER BY r.dataGeracao DESC LIMIT 1")
    Optional<RelatorioMensal> findMaisRecenteByMesAndAno(@Param("mes") Integer mes, @Param("ano") Integer ano);
}