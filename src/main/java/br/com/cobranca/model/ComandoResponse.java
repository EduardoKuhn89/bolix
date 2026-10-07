package br.com.cobranca.model;

import java.time.OffsetDateTime;

/**
 * Retorno de um comando de instrução (ex.: pedido de baixa - item 7.4).
 */
public final class ComandoResponse {

    private final String transactionId;
    private final String nossoNumero;
    private final String statusComando;
    private final OffsetDateTime dataHoraRegistro;
    private final String tipoMensagem;

    public ComandoResponse(String transactionId, String nossoNumero, String statusComando,
            OffsetDateTime dataHoraRegistro, String tipoMensagem) {
        this.transactionId = transactionId;
        this.nossoNumero = nossoNumero;
        this.statusComando = statusComando;
        this.dataHoraRegistro = dataHoraRegistro;
        this.tipoMensagem = tipoMensagem;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public String getStatusComando() {
        return statusComando;
    }

    public OffsetDateTime getDataHoraRegistro() {
        return dataHoraRegistro;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    @Override
    public String toString() {
        return "ComandoResponse{" + "transactionId=" + transactionId + ", nossoNumero=" + nossoNumero + ", statusComando=" + statusComando + '}';
    }

}
