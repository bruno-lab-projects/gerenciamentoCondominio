// Script para gerenciar despesas recorrentes

// Configurar máscaras monetárias
const configMoeda = {
    prefix: 'R$ ',
    suffix: '',
    fixed: true,
    fractionDigits: 2,
    decimalSeparator: ',',
    thousandsSeparator: '.',
    cursor: 'end'
};

// Inicializar máscaras quando o documento estiver pronto
document.addEventListener('DOMContentLoaded', function() {
    SimpleMaskMoney.setMask('.dinheiro-modal', configMoeda);
    carregarDespesasPadrao();
});

// Carregar despesas padrão
async function carregarDespesasPadrao() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const mes = urlParams.get('mes') || new Date().getMonth() + 1;
        const ano = urlParams.get('ano') || new Date().getFullYear();

        const response = await fetch(`/despesas/padroes?mes=${mes}&ano=${ano}`);
        const despesas = await response.json();
        
        const container = document.getElementById('despesasPadraoContainer');
        container.innerHTML = '';
        
        // Obter token CSRF do meta tag
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfParameterName = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Contar despesas pendentes (não criadas)
        let despesasPendentes = 0;

        despesas.forEach(dp => {
            const jaExiste = dp.jaExiste;
            if (!jaExiste) {
                despesasPendentes++;
            }
            
            const col = document.createElement('div');
            col.className = 'col-md-6 col-lg-3';
            
            const textoValor = dp.tipo === 'VARIAVEL' 
                ? '<small class="despesa-valor">Clique para preencher o valor</small>'
                : `<small class="despesa-valor">R$ ${formatarMoeda(dp.valorPadrao)}</small>`;
            
            const badgeJaExiste = jaExiste 
                ? '<span class="badge bg-success">✓ Criada</span>' 
                : '';

            col.innerHTML = `
                <form method="post" action="/despesas/criar-da-padrao" class="w-100">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="despesaPadraoId" value="${dp.id}">
                    <input type="hidden" name="mes" value="${mes}">
                    <input type="hidden" name="ano" value="${ano}">
                    <input type="hidden" name="valorHidden" class="valor-hidden-${dp.id}">
                    
                    <button type="button" 
                            class="btn btn-despesa-padrao w-100 ${jaExiste ? 'btn-outline-secondary' : 'btn-outline-primary'}" 
                            data-id="${dp.id}"
                            data-descricao="${dp.descricao}"
                            data-tipo="${dp.tipo}"
                            data-valor="${dp.valorPadrao}"
                            ${jaExiste ? 'disabled' : ''}
                            onclick="abrirModal(this, ${mes}, ${ano})">
                        ${badgeJaExiste}
                        <div class="despesa-titulo">${dp.descricao}</div>
                        ${textoValor}
                    </button>
                </form>
            `;
            
            container.appendChild(col);
        });

        // Exibir ou ocultar alerta de despesas pendentes
        const alerta = document.getElementById('alertaDespesasPendentes');
        const contador = document.getElementById('contadorPendentes');
        
        if (despesasPendentes > 0) {
            contador.textContent = despesasPendentes;
            alerta.classList.remove('d-none');
        } else {
            alerta.classList.add('d-none');
        }

    } catch (error) {
        console.error('Erro ao carregar despesas padrão:', error);
        alert('Erro ao carregar despesas recorrentes');
    }
}

function formatarMoeda(valor) {
    return new Intl.NumberFormat('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(valor);
}

function abrirModal(btn, mes, ano) {
    const despesaPadrao = {
        id: btn.dataset.id,
        descricao: btn.dataset.descricao,
        tipo: btn.dataset.tipo,
        valor: parseFloat(btn.dataset.valor) || 0,
        mes: mes,
        ano: ano
    };

    const modal = despesaPadrao.tipo === 'VARIAVEL' 
        ? new bootstrap.Modal(document.getElementById('modalDespesaVariavel'))
        : new bootstrap.Modal(document.getElementById('modalDespesaFixa'));

    if (despesaPadrao.tipo === 'VARIAVEL') {
        document.getElementById('modalTitulo').textContent = despesaPadrao.descricao;
        document.getElementById('valorVariavel').value = '';
        document.getElementById('valorVariavel').dataset.despesaId = despesaPadrao.id;
        SimpleMaskMoney.setMask('#valorVariavel', configMoeda);
    } else {
        document.getElementById('modalTituloFixa').textContent = despesaPadrao.descricao;
        document.getElementById('valorFixo').value = 'R$ ' + formatarMoeda(despesaPadrao.valor);
        document.getElementById('valorFixo').dataset.despesaId = despesaPadrao.id;
        document.getElementById('atualizarPadrao').checked = false;
        SimpleMaskMoney.setMask('#valorFixo', configMoeda);
    }

    modal.show();
}

function converterParaDecimal(valorFormatado) {
    return valorFormatado
        .replace('R$', '')
        .replace(/\./g, '')
        .replace(',', '.')
        .trim();
}

// Event listeners para os botões de confirmação
document.getElementById('btnConfirmarVariavel').addEventListener('click', () => {
    const input = document.getElementById('valorVariavel');
    const valor = input.value;
    const valorDecimal = parseFloat(converterParaDecimal(valor));
    const despesaId = input.dataset.despesaId;
    
    if (isNaN(valorDecimal) || valorDecimal <= 0) {
        alert('Por favor, digite um valor válido');
        return;
    }

    // Encontrar o formulário correto e submeter
    const form = document.querySelector(`input[name="despesaPadraoId"][value="${despesaId}"]`).closest('form');
    form.querySelector('.valor-hidden-' + despesaId).value = valorDecimal;
    form.submit();
});

document.getElementById('btnConfirmarFixa').addEventListener('click', () => {
    const input = document.getElementById('valorFixo');
    const valor = input.value;
    const valorDecimal = parseFloat(converterParaDecimal(valor));
    const despesaId = input.dataset.despesaId;
    const atualizar = document.getElementById('atualizarPadrao').checked;
    
    if (isNaN(valorDecimal) || valorDecimal <= 0) {
        alert('Por favor, digite um valor válido');
        return;
    }

    // Encontrar o formulário correto e submeter
    const form = document.querySelector(`input[name="despesaPadraoId"][value="${despesaId}"]`).closest('form');
    form.querySelector('.valor-hidden-' + despesaId).value = valorDecimal;
    
    // Adicionar campo para atualizar valor padrão se necessário
    if (atualizar) {
        const inputAtualizar = document.createElement('input');
        inputAtualizar.type = 'hidden';
        inputAtualizar.name = 'atualizarValorPadrao';
        inputAtualizar.value = 'true';
        form.appendChild(inputAtualizar);
    }
    
    form.submit();
});
