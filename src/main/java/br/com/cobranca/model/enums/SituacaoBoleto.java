package br.com.cobranca.model.enums;

/**
 * Situação normalizada de um título perante o banco. As implementações fazem o
 * de-para do status "cru" retornado pelo banco (texto no Sicredi, código
 * numérico na Caixa) para esse enum.
 */
public enum SituacaoBoleto {
    EM_ABERTO,
    EM_CARTEIRA_PIX,
    VENCIDO,
    LIQUIDADO,
    LIQUIDADO_CARTORIO,
    LIQUIDADO_REDE,
    LIQUIDADO_COMPE,
    LIQUIDADO_PIX,
    LIQUIDADO_CHEQUE,
    BAIXADO,
    BAIXADO_POR_DEVOLUCAO,
    BAIXADO_POR_ESTORNO,
    BAIXADO_POR_PROTESTO,
    PROTESTADO,
    EM_CARTORIO,
    NEGATIVADO,
    AGUARDANDO_ENTRADA_CARTORIO,
    AGUARDANDO_SUSTACAO_CARTORIO,
    REJEITADO,
    DESCONHECIDO
}
