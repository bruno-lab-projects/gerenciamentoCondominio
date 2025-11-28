package com.brunobarreto.condominio.config;

import com.brunobarreto.condominio.model.Usuario;
import com.brunobarreto.condominio.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class ConfiguracaoGlobal {

    private final UsuarioRepository repository;

    public ConfiguracaoGlobal(UsuarioRepository repository) {
        this.repository = repository;
    }

    @ModelAttribute("nomeUsuario")
    public String nomeUsuario(Principal principal) {
        if (principal == null) {
            return null;
        }
        
        return repository.findByEmail(principal.getName())
                .map(Usuario::getNome)
                .orElse(principal.getName());
    }
}