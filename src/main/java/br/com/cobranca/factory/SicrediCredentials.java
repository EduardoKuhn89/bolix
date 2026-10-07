package br.com.cobranca.factory;

/**
 * Credenciais/parâmetros de conexão exigidos pela API do Sicredi (item 4/6 do
 * manual).
 */
public final class SicrediCredentials {

    private final String cooperativa;
    private final String posto;
    private final String codigoBeneficiario;
    private final String clientApiKey;   // x-api-key, obtido no Portal do Desenvolvedor
    private final String username;       // Beneficiário + Cooperativa
    private final String password;       // Código de Acesso gerado no Internet Banking
    private final boolean sandbox;

    public SicrediCredentials(String cooperativa, String posto, String codigoBeneficiario,
            String clientApiKey, String username, String password, boolean sandbox) {
        this.cooperativa = cooperativa;
        this.posto = posto;
        this.codigoBeneficiario = codigoBeneficiario;
        this.clientApiKey = clientApiKey;
        this.username = username;
        this.password = password;
        this.sandbox = sandbox;
    }

    public String getCooperativa() {
        return cooperativa;
    }

    public String getPosto() {
        return posto;
    }

    public String getCodigoBeneficiario() {
        return codigoBeneficiario;
    }

    public String getClientApiKey() {
        return clientApiKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    @Override
    public String toString() {
        return "SicrediCredentials{" + "posto=" + posto + ", codigoBeneficiario=" + codigoBeneficiario + ", sandbox=" + sandbox + '}';
    }

}
