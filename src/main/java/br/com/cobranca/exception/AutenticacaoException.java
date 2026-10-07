package br.com.cobranca.exception;

/**
 * Falha ao autenticar (usuário/senha ou hash inválido, token expirado, etc).
 */
public class AutenticacaoException extends BoletoApiException {

    public AutenticacaoException(String message, int statusHttp, String codigoBanco) {
        super(message, statusHttp, codigoBanco);
    }
}
