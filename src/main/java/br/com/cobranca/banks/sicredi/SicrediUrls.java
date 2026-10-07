package br.com.cobranca.banks.sicredi;

/**
 * Centraliza as URLs REST do Sicredi (produção e sandbox), conforme cada item
 * do manual.
 */
final class SicrediUrls {

    private static final String PROD = "https://api-parceiro.sicredi.com.br";
    private static final String SANDBOX = "https://api-parceiro.sicredi.com.br/sb";

    private final boolean sandbox;

    SicrediUrls(boolean sandbox) {
        this.sandbox = sandbox;
    }

    private String base() {
        return sandbox ? SANDBOX : PROD;
    }

    // 7.1
    String token() {
        return base() + "/auth/openapi/token";
    }

    // 7.2 (cadastro) - v1 tradicional/híbrido/split
    String boletos() {
        return base() + "/cobranca/boleto/v1/boletos";
    }

    // 7.3
    String boletosPdf() {
        return base() + "/cobranca/boleto/v1/boletos/pdf";
    }

    // 7.4
    String baixa(String nossoNumero) {
        return base() + "/cobranca/boleto/v1/boletos/" + nossoNumero + "/baixa";
    }

    // 7.18 - GET .../boletos?codigoBeneficiario=...&nossoNumero=...
    // (a "v2", com o header data-movimento, não possui ambiente sandbox)
    String consultaNossoNumero() {
        return base() + "/cobranca/boleto/v1/boletos";
    }

    // 7.19
    String liquidadosPorDia() {
        return base() + "/cobranca/boleto/v1/boletos/liquidados/dia";
    }

    // 7.20
    String cadastrados() {
        return base() + "/cobranca/boleto/v1/boletos/cadastrados";
    }

    // 23.2 / 23.4
    String webhookContrato() {
        return "https://api-parceiro.sicredi.com.br/cobranca/boleto/v1/webhook/contrato/";
    }

    String webhookContrato(String id) {
        return "https://api-parceiro.sicredi.com.br/cobranca/boleto/v1/webhook/contrato/" + id;
    }

    // 23.3
    String webhookContratos(String cooperativa, String posto, String beneficiario) {
        return "https://api-parceiro.sicredi.com.br/cobranca/boleto/v1/webhook/contratos/?cooperativa="
                + cooperativa + "&posto=" + posto + "&beneficiario=" + beneficiario;
    }
}
