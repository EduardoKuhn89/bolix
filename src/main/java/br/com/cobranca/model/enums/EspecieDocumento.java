package br.com.cobranca.model.enums;

/**
 * Espécie do título. Os nomes seguem o padrão textual do Sicredi.
 * Cada implementação de {@link br.com.cobranca.gateway.BoletoBankGateway}
 * converte esse valor para o código exigido pelo banco (ex.: Caixa usa
 * códigos numéricos 01 a 33, conforme NE006 do manual
 * WEBSERVICE-XML-COBRANCA-BANCARIA).
 */
public enum EspecieDocumento {
    CHEQUE,
    DUPLICATA_MERCANTIL,
    DUPLICATA_MERCANTIL_INDICACAO,
    DUPLICATA_SERVICO,
    DUPLICATA_SERVICO_INDICACAO,
    DUPLICATA_RURAL,
    LETRA_CAMBIO,
    NOTA_PROMISSORIA,
    NOTA_PROMISSORIA_RURAL,
    TRIPLICATA_MERCANTIL,
    TRIPLICATA_SERVICO,
    NOTA_SEGURO,
    RECIBO,
    FATURA,
    NOTA_DEBITO,
    APOLICE_SEGURO,
    MENSALIDADE_ESCOLAR,
    PARCELA_CONSORCIO,
    NOTA_FISCAL,
    DOCUMENTO_DIVIDA,
    CEDULA_PRODUTO_RURAL,
    CARTAO_CREDITO,
    BOLETO_PROPOSTA,
    BOLETO_DEPOSITO_APORTE,
    OUTROS
}
