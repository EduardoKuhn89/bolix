package br.com.cobranca.factory;

import br.com.cobranca.auth.AuthToken;
import br.com.cobranca.auth.TokenProvider;
import br.com.cobranca.gateway.BoletoBankGateway;
import br.com.cobranca.gateway.WebhookGateway;
import br.com.cobranca.banks.caixa.CaixaBoletoGateway;
import br.com.cobranca.banks.caixa.CaixaWebhookGateway;
import br.com.cobranca.banks.sicredi.SicrediAuthClient;
import br.com.cobranca.banks.sicredi.SicrediBoletoGateway;
import br.com.cobranca.banks.sicredi.SicrediWebhookGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/**
 * Ponto único de entrada da biblioteca: dado um {@link BankType} e as
 * credenciais correspondentes, entrega os dois gateways do contrato unificado
 * ({@link BoletoBankGateway} e {@link WebhookGateway}) já ligados à
 * implementação correta do banco.
 * <p>
 * Adicionar um novo banco no futuro = criar um novo {@link BankType}, as
 * credenciais específicas, as duas implementações de gateway, e um novo método
 * {@code de(...)} aqui — nenhum código cliente precisa mudar, pois ele sempre
 * programa contra {@link BoletoBankGateway}/{@link WebhookGateway}.
 */
public final class BancoCobranca {

    private final BoletoBankGateway boletoGateway;
    private final WebhookGateway webhookGateway;

    private BancoCobranca(BoletoBankGateway boletoGateway, WebhookGateway webhookGateway) {
        this.boletoGateway = boletoGateway;
        this.webhookGateway = webhookGateway;
    }

    public BoletoBankGateway boletos() {
        return boletoGateway;
    }

    public WebhookGateway webhooks() {
        return webhookGateway;
    }

    /*
     * Cria a integração com o Sicredi (item 6 do manual "Iniciando a
     * integração").
     */
    public static BancoCobranca sicredi(SicrediCredentials credentials, AuthToken tokenInicial, java.util.function.Consumer<AuthToken> onTokenUpdated) {
        return sicredi(credentials, tokenInicial, onTokenUpdated, defaultHttpClient());
    }

    public static BancoCobranca sicredi(SicrediCredentials credentials, AuthToken tokenInicial, java.util.function.Consumer<AuthToken> onTokenUpdated, HttpClient httpClient) {
        Objects.requireNonNull(credentials, "credentials");
        ObjectMapper mapper = defaultObjectMapper();

        // Passa o token persistido e o callback para o client de autenticação
        TokenProvider tokenProvider = new SicrediAuthClient(httpClient, mapper, credentials, tokenInicial, onTokenUpdated);

        BoletoBankGateway boleto = new SicrediBoletoGateway(httpClient, mapper, tokenProvider, credentials);
        WebhookGateway webhook = new SicrediWebhookGateway(httpClient, mapper, tokenProvider, credentials);
        return new BancoCobranca(boleto, webhook);
    }

    /*
     * Cria a integração com a Caixa (Webservice XML de Cobrança Bancária).
     */
    public static BancoCobranca caixa(CaixaCredentials credentials) {
        return caixa(credentials, defaultHttpClient());
    }

    public static BancoCobranca caixa(CaixaCredentials credentials, HttpClient httpClient) {
        Objects.requireNonNull(credentials, "credentials");
        BoletoBankGateway boleto = new CaixaBoletoGateway(httpClient, credentials);
        WebhookGateway webhook = new CaixaWebhookGateway();
        return new BancoCobranca(boleto, webhook);
    }

    private static HttpClient defaultHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
