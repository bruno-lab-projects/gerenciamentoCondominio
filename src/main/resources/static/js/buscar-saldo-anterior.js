/**
 * Módulo para buscar saldo do mês anterior automaticamente
 */

document.addEventListener('DOMContentLoaded', function() {
    const checkbox = document.getElementById('checkPuxarSaldo');
    const campoMes = document.getElementById('mes');
    const campoAno = document.getElementById('ano');
    
    if (!checkbox) return; // Se não existir checkbox, sai (não é a página de relatório)
    
    // Listener principal do checkbox
    checkbox.addEventListener('change', function() {
        if (this.checked) {
            buscarSaldoAnterior();
        } else {
            limparCampoSaldo();
        }
    });
    
    // Listener para desmarcar checkbox se mudar mês/ano após buscar
    campoMes.addEventListener('change', function() {
        if (checkbox.checked) {
            // Avisa que precisa buscar novamente
            if (confirm('O mês foi alterado. Deseja buscar o saldo novamente?')) {
                buscarSaldoAnterior();
            } else {
                checkbox.checked = false;
                limparCampoSaldo();
            }
        }
    });
    
    campoAno.addEventListener('change', function() {
        if (checkbox.checked) {
            // Avisa que precisa buscar novamente
            if (confirm('O ano foi alterado. Deseja buscar o saldo novamente?')) {
                buscarSaldoAnterior();
            } else {
                checkbox.checked = false;
                limparCampoSaldo();
            }
        }
    });
});

function buscarSaldoAnterior() {
    const mes = document.getElementById('mes').value;
    const ano = document.getElementById('ano').value;

    if (!mes || !ano) {
        alert('Por favor, selecione o mês e ano antes de buscar o saldo anterior.');
        document.getElementById('checkPuxarSaldo').checked = false;
        return;
    }

    // Mostra qual mês será buscado ANTES de fazer a requisição
    const nomesMeses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 
                        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    
    const mesAtual = parseInt(mes);
    const anoAtual = parseInt(ano);
    
    // Calcula mês anterior
    let mesAnterior = mesAtual - 1;
    let anoAnterior = anoAtual;
    
    if (mesAnterior === 0) {
        mesAnterior = 12;
        anoAnterior = anoAtual - 1;
    }
    
    const nomeMesAnterior = nomesMeses[mesAnterior - 1];
    
    // Confirmação CLARA do que será buscado
    const confirma = confirm(
        `Você está gerando o relatório de ${nomesMeses[mesAtual - 1]}/${anoAtual}.\n\n` +
        `Deseja buscar o saldo de ${nomeMesAnterior}/${anoAnterior}?`
    );
    
    if (!confirma) {
        document.getElementById('checkPuxarSaldo').checked = false;
        return;
    }

    // Mostra feedback visual
    const campoDisplay = document.getElementById('saldoAnteriorDisplay');
    const valorOriginal = campoDisplay.value;
    campoDisplay.value = 'Buscando...';
    campoDisplay.disabled = true;
    
    // Desabilita checkbox durante busca
    document.getElementById('checkPuxarSaldo').disabled = true;

    fetch(`/api/saldo-anterior?mes=${mes}&ano=${ano}`)
        .then(response => {
            if (response.status === 204) {
                // Nenhum relatório encontrado
                alert(
                    `Nenhum fechamento encontrado para ${nomeMesAnterior}/${anoAnterior}.\n\n` +
                    `Você precisará inserir o saldo manualmente.`
                );
                campoDisplay.value = valorOriginal;
                campoDisplay.disabled = false;
                document.getElementById('checkPuxarSaldo').checked = false;
                document.getElementById('checkPuxarSaldo').disabled = false;
                return null;
            }
            if (!response.ok) {
                throw new Error('Erro ao buscar saldo anterior');
            }
            return response.json();
        })
        .then(saldo => {
            if (saldo !== null) {
                // Preenche o campo oculto com o valor limpo
                document.getElementById('saldoAnterior').value = saldo;
                
                // Formata e preenche o campo visível
                campoDisplay.value = saldo.toString().replace('.', ',');
                
                // Reaplica a máscara usando SimpleMaskMoney
                SimpleMaskMoney.formatToMask(saldo.toString(), campoDisplay);
                
                campoDisplay.disabled = false;
                document.getElementById('checkPuxarSaldo').disabled = false;
                
                // Confirmação visual de sucesso
                alert(
                    `✓ Saldo de ${nomeMesAnterior}/${anoAnterior} carregado com sucesso!\n\n` +
                    `Valor: ${campoDisplay.value}\n\n` +
                    `📌 Nota: Se houver mais de um relatório desse mês, o sistema busca o mais recente.`
                );
            }
        })
        .catch(error => {
            console.error('Erro:', error);
            alert('Erro ao buscar saldo anterior. Verifique sua conexão e tente novamente.');
            campoDisplay.value = valorOriginal;
            campoDisplay.disabled = false;
            document.getElementById('checkPuxarSaldo').checked = false;
            document.getElementById('checkPuxarSaldo').disabled = false;
        });
}

function limparCampoSaldo() {
    const campoDisplay = document.getElementById('saldoAnteriorDisplay');
    const campoOculto = document.getElementById('saldoAnterior');
    
    campoDisplay.value = '';
    campoOculto.value = '';
    campoDisplay.disabled = false;
}
