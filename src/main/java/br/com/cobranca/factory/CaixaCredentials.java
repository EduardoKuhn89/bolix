package br.com.cobranca.factory;

/**
 * Parâmetros exigidos pelo Webservice XML da Caixa (itens 4 e 4.3 do manual
 * WEBSERVICE-XML-COBRANCA-BANCARIA): não há OAuth2, a "autenticação" é um hash
 * SHA-256/Base64 calculado por requisição a partir do código do beneficiário,
 * nosso número, vencimento, valor e CPF/CNPJ.
 */
public final class CaixaCredentials {

    private final String codigoBeneficiario; // 7 posições
    private final String cnpjOuCpfBeneficiario; // 14 posições, com zeros à esquerda se CPF
    private final String unidade; // agência de relacionamento
    private final boolean sandbox;

    public CaixaCredentials(String codigoBeneficiario, String cnpjOuCpfBeneficiario, String unidade, boolean sandbox) {
        this.codigoBeneficiario = codigoBeneficiario;
        this.cnpjOuCpfBeneficiario = cnpjOuCpfBeneficiario;
        this.unidade = unidade;
        this.sandbox = sandbox;
    }

    public String getCodigoBeneficiario() {
        return codigoBeneficiario;
    }

    public String getCnpjOuCpfBeneficiario() {
        return cnpjOuCpfBeneficiario;
    }

    public String getUnidade() {
        return unidade;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    @Override
    public String toString() {
        return "CaixaCredentials{" + "codigoBeneficiario=" + codigoBeneficiario + ", cnpjOuCpfBeneficiario=" + cnpjOuCpfBeneficiario + ", unidade=" + unidade + ", sandbox=" + sandbox + '}';
    }

}
