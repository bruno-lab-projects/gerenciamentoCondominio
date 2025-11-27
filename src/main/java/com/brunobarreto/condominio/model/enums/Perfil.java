package com.brunobarreto.condominio.model.enums;

public enum Perfil {
    
    ADMIN("Administrador"),
    MORADOR("Morador");
    
    private final String descricao;
    
    Perfil(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
}
