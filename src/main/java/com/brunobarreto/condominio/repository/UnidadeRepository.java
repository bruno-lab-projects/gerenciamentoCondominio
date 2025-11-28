package com.brunobarreto.condominio.repository;

import com.brunobarreto.condominio.model.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}