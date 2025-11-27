package com.brunobarreto.condominio.service;

import com.brunobarreto.condominio.model.Usuario;
import com.brunobarreto.condominio.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
        // 1. Busca o usuário no banco pelo email
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // 2. Define o papel dele (ADMIN ou MORADOR)
        // O "ROLE_" é um padrão obrigatório do Spring Security
        String role = "ROLE_" + usuario.getPerfil().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // 3. Retorna o usuário formatado para o Spring validar a senha
        return new User(usuario.getEmail(), usuario.getSenha(), List.of(authority));
    }
}
