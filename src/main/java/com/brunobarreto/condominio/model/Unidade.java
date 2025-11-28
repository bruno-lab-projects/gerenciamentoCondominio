package com.brunobarreto.condominio.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Unidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeUnidade;
    private String nomeMorador;
}