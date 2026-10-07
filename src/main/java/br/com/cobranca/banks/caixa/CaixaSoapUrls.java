package br.com.cobranca.banks.caixa;

/**
 * URLs fixas do Webservice XML da Caixa (item 4.1 do manual). Não há ambiente
 * sandbox documentado.
 */
final class CaixaSoapUrls {

    private CaixaSoapUrls() {
    }

    static final String CONSULTA = "https://barramento.caixa.gov.br/sibar/ConsultaCobrancaBancaria/Boleto";
    static final String MANUTENCAO = "https://barramento.caixa.gov.br/sibar/ManutencaoCobrancaBancaria/Boleto/Externo";
    static final String USUARIO_SERVICO = "SGCBS02P";
    static final String SISTEMA_ORIGEM = "SIGCB";
}
