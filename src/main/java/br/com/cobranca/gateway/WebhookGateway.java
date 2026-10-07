package br.com.cobranca.gateway;

import br.com.cobranca.model.ContratoWebhook;

import java.util.List;

/**
 * Contrato único para o módulo Webhook, modelado a partir dos recursos do
 * Sicredi:
 * <p>
 * 23.2 Realizar Contratação de Webhook (Registro)<br>
 * 23.3 Consulta de Contratos de Webhook<br>
 * 23.4 Alterar Contrato de Webhook (Inativar / Remover)
 * <p>
 * A Caixa não expõe webhook em sua API de Cobrança Bancária (o retorno de
 * liquidação é feito apenas via arquivo CNAB, conforme item 3.2 do manual
 * WEBSERVICE-XML-COBRANCA-BANCARIA), portanto sua implementação
 * ({@code CaixaWebhookGateway}) lança
 * {@link br.com.cobranca.exception.OperacaoNaoSuportadaException} em todos os
 * métodos.
 */
public interface WebhookGateway {

    /*
     * 23.2 - Realizar Contratação Webhook.
     */
    ContratoWebhook register(ContratoWebhook contrato);

    /*
     * 23.3 - Consulta de Contratos, filtrando por
     * cooperativa/posto/beneficiário.
     */
    List<ContratoWebhook> list(String cooperativa, String posto, String codigoBeneficiario);

    /*
     * 23.4 - Altera Contrato Webhook. Para inativar/remover um contrato, envie
     * o mesmo contrato com {@code contratoStatus = INATIVO} (ou
     * {@code urlStatus = INATIVO}), pois o Sicredi não expõe um DELETE físico.
     */
    ContratoWebhook change(String idContrato, ContratoWebhook contratoAtualizado);
}
