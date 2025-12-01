package com.brunobarreto.condominio.config;

import com.brunobarreto.condominio.model.Unidade;
import com.brunobarreto.condominio.model.Usuario;
import com.brunobarreto.condominio.model.DespesaPadrao;
import com.brunobarreto.condominio.model.enums.Perfil;
import com.brunobarreto.condominio.repository.UnidadeRepository;
import com.brunobarreto.condominio.repository.UsuarioRepository;
import com.brunobarreto.condominio.repository.DespesaPadraoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class CargaInicial implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final DespesaPadraoRepository despesaPadraoRepository;
    private final PasswordEncoder passwordEncoder;

    public CargaInicial(UsuarioRepository usuarioRepository, UnidadeRepository unidadeRepository, DespesaPadraoRepository despesaPadraoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.despesaPadraoRepository = despesaPadraoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Síndica
        if (usuarioRepository.findByEmail("sindica@predio.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setEmail("sindica@predio.com");
            admin.setNome("Síndica");
            admin.setSenha(passwordEncoder.encode("123456")); 
            admin.setPerfil(Perfil.ADMIN);
            admin.setApartamento("101");
            usuarioRepository.save(admin);
        }

        // 2. Morador Teste
        if (usuarioRepository.findByEmail("morador@predio.com").isEmpty()) {
            Usuario morador = new Usuario();
            morador.setEmail("morador@predio.com");
            morador.setNome("Vizinho do 202");
            morador.setSenha(passwordEncoder.encode("123456")); 
            morador.setPerfil(Perfil.MORADOR);
            morador.setApartamento("202");
            usuarioRepository.save(morador);
        }

        // 3. UNIDADES PARA BOLETOS (Só cria se a tabela estiver vazia)
        if (unidadeRepository.count() == 0) {
            String[] listaAptos = {"11", "12", "21", "22", "31", "32", "41", "42", "51", "52", "61"};

            for (String numero : listaAptos) {
                Unidade u = new Unidade();
                u.setNomeUnidade("Apto " + numero);
                u.setNomeMorador(""); 
                unidadeRepository.save(u);
            }

            Unidade loja = new Unidade();
            loja.setNomeUnidade("Loja Térreo");
            loja.setNomeMorador("");
            unidadeRepository.save(loja);
            
            System.out.println(">>> UNIDADES PERSONALIZADAS CRIADAS <<<");
        }

        // 4. DESPESAS PADRÃO (Só cria se a tabela estiver vazia)
        if (despesaPadraoRepository.count() == 0) {
            DespesaPadrao embasa = new DespesaPadrao();
            embasa.setDescricao("EMBASA");
            embasa.setValorPadrao(null); // Valor variável
            embasa.setTipo(DespesaPadrao.TipoDespesaPadrao.VARIAVEL);
            embasa.setOrdemExibicao(1);
            embasa.setAtiva(true);
            despesaPadraoRepository.save(embasa);

            DespesaPadrao coelba = new DespesaPadrao();
            coelba.setDescricao("COELBA");
            coelba.setValorPadrao(null); // Valor variável
            coelba.setTipo(DespesaPadrao.TipoDespesaPadrao.VARIAVEL);
            coelba.setOrdemExibicao(2);
            coelba.setAtiva(true);
            despesaPadraoRepository.save(coelba);

            DespesaPadrao elevasol = new DespesaPadrao();
            elevasol.setDescricao("ELEVASOL");
            elevasol.setValorPadrao(new BigDecimal("610.00"));
            elevasol.setTipo(DespesaPadrao.TipoDespesaPadrao.FIXA);
            elevasol.setOrdemExibicao(3);
            elevasol.setAtiva(true);
            despesaPadraoRepository.save(elevasol);

            DespesaPadrao rapazLimpeza = new DespesaPadrao();
            rapazLimpeza.setDescricao("RAPAZ DA LIMPEZA");
            rapazLimpeza.setValorPadrao(new BigDecimal("500.00"));
            rapazLimpeza.setTipo(DespesaPadrao.TipoDespesaPadrao.FIXA);
            rapazLimpeza.setOrdemExibicao(4);
            rapazLimpeza.setAtiva(true);
            despesaPadraoRepository.save(rapazLimpeza);

            System.out.println(">>> DESPESAS PADRÃO CRIADAS <<<");
        }
    }
}