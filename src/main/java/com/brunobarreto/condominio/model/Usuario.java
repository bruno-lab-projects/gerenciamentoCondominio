package com.brunobarreto.condominio.model;

import com.brunobarreto.condominio.model.enums.Perfil;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String senha; // Hash da senha
    
    private String nome;
    
    private String apartamento; // Ex: 101, Loja A
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;
    
}
