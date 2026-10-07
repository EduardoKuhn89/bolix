package br.com.cobranca.auth;

import java.time.Instant;

/**
 * Token de autenticação OAuth2/JWT, no padrão do item 7.1 do manual Sicredi
 * (access_token / refresh_token / expires_in / refresh_expires_in). Bancos que
 * não usem OAuth2 (ex.: Caixa, cuja "autenticação" é um hash SHA-256 calculado
 * por requisição) simplesmente devolvem um AuthToken "sintético" sem expiração,
 * para manter o contrato único da interface.
 */
public final class AuthToken {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Instant expiresAt;
    private final Instant refreshExpiresAt;

    public AuthToken(String accessToken, String refreshToken, String tokenType,
            Instant expiresAt, Instant refreshExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isRefreshExpired() {
        return refreshExpiresAt != null && Instant.now().isAfter(refreshExpiresAt);
    }

    public String getBearerHeader() {
        return (tokenType == null ? "Bearer" : tokenType) + " " + accessToken;
    }
}
