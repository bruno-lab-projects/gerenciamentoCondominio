package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    
    List<Despesa> findByDataBetween(LocalDate inicio, LocalDate fim);
    
}
