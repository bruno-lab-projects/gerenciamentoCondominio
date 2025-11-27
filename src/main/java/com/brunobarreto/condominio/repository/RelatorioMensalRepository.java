package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.RelatorioMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RelatorioMensalRepository extends JpaRepository<RelatorioMensal, Long> {
    List<RelatorioMensal> findAllByOrderByAnoDescMesDesc();
}