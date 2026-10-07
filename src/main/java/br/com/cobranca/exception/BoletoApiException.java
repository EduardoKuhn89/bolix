package br.com.cobranca.exception;

/**
 * Exceção base para qualquer erro de integração com o banco.
 */
public class BoletoApiException extends RuntimeException {

    private final int statusHttp;
    private final String codigoBanco;

    public BoletoApiException(String message, int statusHttp, String codigoBanco) {
        super(message);
        this.statusHttp = statusHttp;
        this.codigoBanco = codigoBanco;
    }

    public BoletoApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusHttp = -1;
        this.codigoBanco = null;
    }

    public int getStatusHttp() {
        return statusHttp;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }
}
