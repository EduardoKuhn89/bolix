package br.com.cobranca.model;

/**
 * Retorno do cadastro de um boleto (item 7.2).
 */
public final class BoletoRegistrado {

    private final String linhaDigitavel;
    private final String codigoBarras;
    private final String nossoNumero;
    private final String cooperativa; // "agência/coop" no Sicredi; unidade na Caixa
    private final String posto;
    private final String qrCode;      // presente somente em boletos HIBRIDO
    private final String urlBoleto;   // pdf/URL de impressão, quando o banco devolve

    public BoletoRegistrado(String linhaDigitavel, String codigoBarras, String nossoNumero,
            String cooperativa, String posto, String qrCode, String urlBoleto) {
        this.linhaDigitavel = linhaDigitavel;
        this.codigoBarras = codigoBarras;
        this.nossoNumero = nossoNumero;
        this.cooperativa = cooperativa;
        this.posto = posto;
        this.qrCode = qrCode;
        this.urlBoleto = urlBoleto;
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public String getCooperativa() {
        return cooperativa;
    }

    public String getPosto() {
        return posto;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getUrlBoleto() {
        return urlBoleto;
    }

    @Override
    public String toString() {
        return "BoletoRegistrado{nossoNumero='" + nossoNumero + "', linhaDigitavel='" + linhaDigitavel + "'}";
    }
}
