package br.com.cobranca.model.enums;

/**
 * Eventos que podem ser recebidos via Webhook, conforme item 23.2 do manual
 * Sicredi.
 */
public enum EventoWebhook {
    LIQUIDACAO,
    LIQUIDACAO_PIX,
    LIQUIDACAO_COMPE_H5,
    LIQUIDACAO_COMPE_H6,
    LIQUIDACAO_COMPE_H8,
    LIQUIDACAO_REDE,
    LIQUIDACAO_CARTORIO,
    AVISO_PAGAMENTO_COMPE,
    ESTORNO_LIQUIDACAO_REDE
}
