package com.brunobarreto.condominio.controller;

import com.brunobarreto.condominio.model.RelatorioMensal;
import com.brunobarreto.condominio.repository.RelatorioMensalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RelatorioApiController {

    @Autowired
    private RelatorioMensalRepository relatorioRepository;

    /**
     * Busca o saldo do mês anterior com base no mês/ano informado
     * Se houver múltiplos relatórios no mesmo mês, retorna o mais recente
     * @param mes Mês do relatório atual (1-12)
     * @param ano Ano do relatório atual
     * @return Saldo atual do mês anterior ou 204 No Content se não encontrado
     */
    @GetMapping("/saldo-anterior")
    public ResponseEntity<BigDecimal> buscarSaldoAnterior(
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        
        // Cria data do mês atual e subtrai 1 mês
        LocalDate dataAtual = LocalDate.of(ano, mes, 1);
        LocalDate dataAnterior = dataAtual.minusMonths(1);
        
        Integer mesAnterior = dataAnterior.getMonthValue();
        Integer anoAnterior = dataAnterior.getYear();
        
        // Busca o relatório MAIS RECENTE do mês anterior
        // Importante: se houver múltiplos relatórios, pega o último gerado
        Optional<RelatorioMensal> relatorioAnterior = 
                relatorioRepository.findMaisRecenteByMesAndAno(mesAnterior, anoAnterior);
        
        // Se encontrou, retorna o saldoAtual, senão retorna No Content
        return relatorioAnterior
                .map(relatorio -> ResponseEntity.ok(relatorio.getSaldoAtual()))
                .orElse(ResponseEntity.noContent().build());
    }
}
