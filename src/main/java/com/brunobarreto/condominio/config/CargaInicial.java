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
        // Verifica se já existe a síndica. Se não, cria.
        if (repository.findByEmail("sindica@predio.com").isEmpty()) {
            
            Usuario admin = new Usuario();
            admin.setEmail("sindica@predio.com");
            admin.setNome("Síndica");
            // A senha será criptografada antes de salvar no banco
            admin.setSenha(passwordEncoder.encode("123456")); 
            admin.setPerfil(Perfil.ADMIN);
            admin.setApartamento("101");
            
            repository.save(admin);
            
            System.out.println("----------------------------------------------------------");
            System.out.println(">>> USUÁRIO ADMIN CRIADO: sindica@predio.com / 123456 <<<");
            System.out.println("----------------------------------------------------------");
        }
    }
}