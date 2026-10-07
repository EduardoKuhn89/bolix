package br.com.cobranca.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Item do retorno da consulta de boletos liquidados por dia (item 7.19).
 */
public final class BoletoLiquidado {

    private final String nossoNumero;
    private final String seuNumero;
    private final LocalDate dataPagamento;
    private final BigDecimal valor;
    private final BigDecimal valorLiquidado;

    private final BigDecimal jurosLiquido;
    private final BigDecimal descontoLiquido;
    private final BigDecimal multaLiquida;
    private final BigDecimal abatimentoLiquido;

    private final String tipoLiquidacao;

    public BoletoLiquidado(String nossoNumero, String seuNumero, LocalDate dataPagamento, BigDecimal valor, BigDecimal valorLiquidado,
            BigDecimal jurosLiquido, BigDecimal descontoLiquido, BigDecimal multaLiquida, BigDecimal abatimentoLiquido, String tipoLiquidacao) {
        this.nossoNumero = nossoNumero;
        this.seuNumero = seuNumero;
        this.dataPagamento = dataPagamento;
        this.valor = valor;
        this.valorLiquidado = valorLiquidado;

        this.jurosLiquido = jurosLiquido;
        this.descontoLiquido = descontoLiquido;
        this.multaLiquida = multaLiquida;
        this.abatimentoLiquido = abatimentoLiquido;

        this.tipoLiquidacao = tipoLiquidacao;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public String getSeuNumero() {
        return seuNumero;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getValorLiquidado() {
        return valorLiquidado;
    }

    public BigDecimal getJurosLiquido() {
        return jurosLiquido;
    }

    public BigDecimal getDescontoLiquido() {
        return descontoLiquido;
    }

    public BigDecimal getMultaLiquida() {
        return multaLiquida;
    }

    public BigDecimal getAbatimentoLiquido() {
        return abatimentoLiquido;
    }

    public String getTipoLiquidacao() {
        return tipoLiquidacao;
    }

    @Override
    public String toString() {
        return "BoletoLiquidado{" + "seuNumero=" + seuNumero + ", dataPagamento=" + dataPagamento + ", valor=" + valor + ", valorLiquidado=" + valorLiquidado + '}';
    }

}
