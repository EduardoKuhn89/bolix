package br.com.cobranca.banks.caixa;

import br.com.cobranca.exception.OperacaoNaoSuportadaException;
import br.com.cobranca.gateway.WebhookGateway;
import br.com.cobranca.model.ContratoWebhook;

import java.util.List;

/**
 * A Caixa não expõe, no manual "WEBSERVICE-XML-COBRANCA-BANCARIA", nenhum
 * recurso de contratação/consulta/alteração de Webhook equivalente aos itens
 * 23.2/23.3/23.4 do Sicredi. A notificação de liquidação/baixa é feita
 * exclusivamente por arquivo de retorno CNAB 240 ou 400, gerado em lote
 * (consolidado ao final do dia e também a cada 15 minutos — item 3.2 do
 * manual), e não por chamada HTTP assíncrona (push) para uma URL do associado.
 * <p>
 * Esta classe existe para que {@code CAIXA} continue satisfazendo o contrato
 * único {@link WebhookGateway} (permitindo, por exemplo, que um mesmo código
 * cliente itere sobre todos os bancos configurados sem precisar de
 * {@code instanceof}), mas todo método aqui falha de forma explícita e
 * informativa — nunca falha silenciosamente nem simula um comportamento que o
 * banco não oferece.
 */
public final class CaixaWebhookGateway implements WebhookGateway {

    private static final String MOTIVO
            = "A Caixa não oferece Webhook em sua API de Cobrança Bancária. "
            + "O retorno de liquidação/baixa é recebido apenas via arquivo CNAB 240/400 "
            + "(consolidado diário e a cada 15 minutos), conforme item 3.2 do manual "
            + "WEBSERVICE-XML-COBRANCA-BANCARIA. Implemente a leitura desse arquivo de "
            + "retorno como mecanismo substituto de notificação para este banco.";

    @Override
    public ContratoWebhook register(ContratoWebhook contrato) {
        throw new OperacaoNaoSuportadaException(MOTIVO);
    }

    @Override
    public List<ContratoWebhook> list(String cooperativa, String posto, String codigoBeneficiario) {
        throw new OperacaoNaoSuportadaException(MOTIVO);
    }

    @Override
    public ContratoWebhook change(String idContrato, ContratoWebhook contratoAtualizado) {
        throw new OperacaoNaoSuportadaException(MOTIVO);
    }
}
