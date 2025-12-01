/**
 * Módulo para aplicar máscara monetária em campos de valor
 * Utiliza SimpleMaskMoney para formatação brasileira (R$)
 */

function aplicarMascaraDinheiro() {
    const camposDinheiro = document.querySelectorAll('.dinheiro');
    
    // Aplica a máscara em todos os campos com classe 'dinheiro'
    camposDinheiro.forEach(function(campo) {
        SimpleMaskMoney.setMask(campo, {
            prefix: 'R$ ',
            suffix: '',
            fixed: true,
            fractionDigits: 2,
            decimalSeparator: ',',
            thousandsSeparator: '.',
            cursor: 'end'
        });
    });
    
    // Listener no submit para limpar formatação antes de enviar ao backend
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            camposDinheiro.forEach(function(campo) {
                // Remove formatação: tira 'R$ ', pontos e troca vírgula por ponto
                let valor = campo.value;
                valor = valor.replace('R$ ', '');
                valor = valor.replace(/\./g, ''); // Remove separador de milhar
                valor = valor.replace(',', '.'); // Troca vírgula por ponto
                campo.value = valor;
            });
        });
    }
}

// Inicializa quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', aplicarMascaraDinheiro);
