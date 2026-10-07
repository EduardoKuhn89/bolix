package br.com.cobranca.banks.sicredi;

import br.com.cobranca.auth.AuthToken;
import br.com.cobranca.auth.TokenProvider;
import br.com.cobranca.exception.AutenticacaoException;
import br.com.cobranca.factory.SicrediCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Item 7.1 - Autenticação. Implementa OAuth2 + JWT: gera o access_token via
 * usuário/senha (grant_type=password) e o renova via refresh_token, mantendo-o
 * em cache em memória, sem autenticar a cada chamada, conforme orientado no
 * manual.
 */
public final class SicrediAuthClient implements TokenProvider {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final SicrediUrls urls;
    private final SicrediCredentials credentials;
    private final java.util.function.Consumer<AuthToken> onTokenUpdated;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile AuthToken cached;

    public SicrediAuthClient(HttpClient httpClient, ObjectMapper mapper, SicrediCredentials credentials) {
        this(httpClient, mapper, credentials, null, null);
    }

    public SicrediAuthClient(HttpClient httpClient, ObjectMapper mapper, SicrediCredentials credentials,
            AuthToken tokenInicial, java.util.function.Consumer<AuthToken> onTokenUpdated) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.credentials = credentials;
        this.urls = new SicrediUrls(credentials.isSandbox());
        this.cached = tokenInicial;
        this.onTokenUpdated = onTokenUpdated;
    }

    /*
     * Retorna um token válido, reutilizando o cache em memória ou realizando a renovação (refresh) / autenticação se expirado.
     */
    @Override
    public AuthToken getValidToken() {
        lock.lock();
        try {
            if (cached == null || cached.isExpired()) {
                if (cached != null && cached.getRefreshToken() != null && !cached.isRefreshExpired()) {
                    cached = refresh(cached.getRefreshToken());
                } else {
                    cached = autenticar();
                }

                //mecanismo responsável por notificar a aplicação que um novo token foi gerado (ou renovado)
                if (onTokenUpdated != null) {
                    onTokenUpdated.accept(cached);
                }
            }
            return cached;
        } finally {
            lock.unlock();
        }
    }

    /*
     * Realiza a autenticação inicial utilizando as credenciais de usuário e
     * senha (grant_type=password).
     */
    @Override
    public AuthToken autenticar() {
        String form = "grant_type=password"
                + "&username=" + credentials.getUsername()
                + "&password=" + credentials.getPassword()
                + "&scope=cobranca";
        AuthToken token = doTokenRequest(form);
        this.cached = token;
        return token;
    }

    /*
     * Renova o token de acesso expirado utilizando o token de atualização
     * (grant_type=refresh_token).
     */
    private AuthToken refresh(String refreshToken) {
        String form = "grant_type=refresh_token&refresh_token=" + refreshToken;
        return doTokenRequest(form);
    }

    /*
     * Executa a requisição HTTP POST para o endpoint de token do Sicredi e
     * converte a resposta em um objeto AuthToken.
     */
    private AuthToken doTokenRequest(String form) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urls.token()))
                    .header("x-api-key", credentials.getClientApiKey())
                    .header("context", "COBRANCA")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AutenticacaoException(
                        "Falha ao autenticar no Sicredi: " + response.body(), response.statusCode(), "SICREDI");
            }

            JsonNode json = mapper.readTree(response.body());
            Instant now = Instant.now();
            return new AuthToken(
                    json.path("access_token").asText(),
                    json.path("refresh_token").asText(null),
                    json.path("token_type").asText("Bearer"),
                    now.plusSeconds(json.path("expires_in").asLong(300)),
                    now.plusSeconds(json.path("refresh_expires_in").asLong(1800))
            );
        } catch (AutenticacaoException e) {
            throw e;
        } catch (Exception e) {
            throw new AutenticacaoException("Erro de comunicação ao autenticar no Sicredi: " + e.getMessage(), -1, "SICREDI");
        }
    }
}
