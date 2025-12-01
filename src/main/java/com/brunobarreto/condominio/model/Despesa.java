package com.brunobarreto.condominio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "despesa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Despesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String descricao;

	private BigDecimal valor;

	private Integer mesReferencia;
	
	private Integer anoReferencia;

}
