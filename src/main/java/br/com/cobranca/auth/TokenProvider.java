package br.com.cobranca.auth;

/**
 * Responsável por obter e renovar o {@link AuthToken} usado nas chamadas ao
 * banco. Implementações devem cachear o token em memória e usar o refresh_token
 * automaticamente quando o access_token expirar (7.1), evitando autenticar a
 * cada chamada.
 */
public interface TokenProvider {

    /*
     * Retorna um token válido, autenticando ou renovando conforme necessário.
     */
    AuthToken getValidToken();

    /*
     * Força uma nova autenticação (usuário + senha), ignorando qualquer cache.
     */
    AuthToken autenticar();
}
