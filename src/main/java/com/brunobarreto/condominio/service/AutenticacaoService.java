package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.model.Usuario;
import com.brunobarreto.condominio.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // <--- Voltamos a usar o User padrão
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        String role = "ROLE_" + usuario.getPerfil().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // Retornamos o objeto padrão simples do Spring
        return new User(usuario.getEmail(), usuario.getSenha(), List.of(authority));
    }
}