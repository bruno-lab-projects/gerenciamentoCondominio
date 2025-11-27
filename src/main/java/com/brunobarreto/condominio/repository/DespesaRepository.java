package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    
}
