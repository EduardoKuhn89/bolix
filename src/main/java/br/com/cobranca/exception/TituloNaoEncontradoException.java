package br.com.cobranca.exception;

/**
 * Título não localizado na consulta/instrução (nosso número, seu número, etc).
 */
public class TituloNaoEncontradoException extends BoletoApiException {

    public TituloNaoEncontradoException(String message, int statusHttp, String codigoBanco) {
        super(message, statusHttp, codigoBanco);
    }
}
