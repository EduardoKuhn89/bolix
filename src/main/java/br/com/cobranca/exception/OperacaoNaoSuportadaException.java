package br.com.cobranca.exception;

/**
 * Lançada quando um banco não suporta um recurso do contrato unificado. Ex.: a
 * Caixa não possui Webhook (não há suporte a notificação push), então
 * {@code CaixaWebhookGateway} lança essa exceção em todos os métodos.
 */
public class OperacaoNaoSuportadaException extends BoletoApiException {

    public OperacaoNaoSuportadaException(String message) {
        super(message, -1, null);
    }
}
