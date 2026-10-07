package br.com.cobranca.model;

import br.com.cobranca.model.enums.SituacaoBoleto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Retorno da consulta de um título (itens 7.18 e 7.20).
 */
public final class BoletoConsulta {

    private final String linhaDigitavel;
    private final String codigoBarras;
    private final String qrCode;
    private final String seuNumero;
    private final String nossoNumero;
    private final String idTituloEmpresa;
    private final BigDecimal valorNominal;
    private final LocalDate dataEmissao;
    private final LocalDate dataVencimento;
    private final SituacaoBoleto situacao;
    private final String documentoPagador;
    private final String nomePagador;

    public BoletoConsulta(String linhaDigitavel, String codigoBarras, String qrCode, String seuNumero,
            String nossoNumero, String idTituloEmpresa, BigDecimal valorNominal,
            LocalDate dataEmissao, LocalDate dataVencimento, SituacaoBoleto situacao,
            String documentoPagador, String nomePagador) {
        this.linhaDigitavel = linhaDigitavel;
        this.codigoBarras = codigoBarras;
        this.qrCode = qrCode;
        this.seuNumero = seuNumero;
        this.nossoNumero = nossoNumero;
        this.idTituloEmpresa = idTituloEmpresa;
        this.valorNominal = valorNominal;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.situacao = situacao;
        this.documentoPagador = documentoPagador;
        this.nomePagador = nomePagador;
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getSeuNumero() {
        return seuNumero;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public String getIdTituloEmpresa() {
        return idTituloEmpresa;
    }

    public BigDecimal getValorNominal() {
        return valorNominal;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public SituacaoBoleto getSituacao() {
        return situacao;
    }

    public String getDocumentoPagador() {
        return documentoPagador;
    }

    public String getNomePagador() {
        return nomePagador;
    }
}
