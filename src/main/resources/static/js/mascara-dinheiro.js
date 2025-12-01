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
        
        // Se houver valor inicial do Thymeleaf no campo oculto, formata no campo display
        const campoOculto = document.getElementById(campo.id.replace('Display', ''));
        if (campoOculto && campoOculto.value) {
            // Formata o valor inicial para exibição
            campo.value = campoOculto.value;
            SimpleMaskMoney.formatToMask(campoOculto.value, campo);
        }
        
        // Atualiza o campo oculto a cada mudança no campo visível
        campo.addEventListener('input', function() {
            if (campoOculto) {
                // Remove formatação e atualiza campo oculto
                let valorLimpo = campo.value;
                valorLimpo = valorLimpo.replace('R$ ', '');
                valorLimpo = valorLimpo.replace(/\./g, '');
                valorLimpo = valorLimpo.replace(',', '.');
                campoOculto.value = valorLimpo;
            }
        });
    });
}

// Inicializa quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', aplicarMascaraDinheiro);
