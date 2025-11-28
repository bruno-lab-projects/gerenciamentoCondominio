package com.brunobarreto.condominio.config;

import com.brunobarreto.condominio.model.Usuario;
import com.brunobarreto.condominio.model.enums.Perfil;
import com.brunobarreto.condominio.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CargaInicial implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CargaInicial(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // --- 1. CRIAÇÃO DA SÍNDICA (Se não existir) ---
        if (repository.findByEmail("sindica@predio.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setEmail("sindica@predio.com");
            admin.setNome("Síndica");
            admin.setSenha(passwordEncoder.encode("123456")); 
            admin.setPerfil(Perfil.ADMIN);
            admin.setApartamento("101");
            
            repository.save(admin);
            
            System.out.println("----------------------------------------------------------");
            System.out.println(">>> ADMIN CRIADO: sindica@predio.com / 123456 <<<");
        }

        // --- 2. CRIAÇÃO DO MORADOR (Se não existir) ---
        if (repository.findByEmail("morador@predio.com").isEmpty()) {
            Usuario morador = new Usuario();
            morador.setEmail("morador@predio.com");
            morador.setNome("Vizinho do 202"); // Pode mudar o nome aqui
            morador.setSenha(passwordEncoder.encode("123456")); 
            morador.setPerfil(Perfil.MORADOR); // <--- IMPORTANTE: Perfil de Morador
            morador.setApartamento("202");
            
            repository.save(morador);
            
            System.out.println(">>> MORADOR CRIADO: morador@predio.com / 123456 <<<");
            System.out.println("----------------------------------------------------------");
        }
    }
}