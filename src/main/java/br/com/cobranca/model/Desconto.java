package br.com.cobranca.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma das faixas de desconto (até 3 no boleto tradicional, 1 no híbrido).
 */
public final class Desconto {

    private final LocalDate data;
    private final BigDecimal valor;
    private final BigDecimal percentual;

    public Desconto(LocalDate data, BigDecimal valor, BigDecimal percentual) {
        this.data = data;
        this.valor = valor;
        this.percentual = percentual;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }
}
