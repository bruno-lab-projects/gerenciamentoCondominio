package com.brunobarreto.condominio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Arquivos estáticos (CSS, JS) são públicos
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // Página de login é pública
                .requestMatchers("/login").permitAll()
                
                // Só ADMIN pode mexer em despesas
                .requestMatchers("/despesas/**").hasRole("ADMIN")
                
                // Ambos podem ver relatórios
                .requestMatchers("/relatorios/**").hasAnyRole("ADMIN", "MORADOR")
                
                // Qualquer outra coisa precisa estar logado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define que as senhas serão criptografadas com BCrypt (Padrão de mercado)
        return new BCryptPasswordEncoder();
    }
}