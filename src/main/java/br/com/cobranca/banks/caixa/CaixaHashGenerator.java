package br.com.cobranca.banks.caixa;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Gera o campo &lt;AUTENTICACAO&gt; exigido pela Caixa (item 4.3 do manual
 * WEBSERVICE-XML-COBRANCA-BANCARIA): SHA-256, codificado em Base64, sobre a
 * concatenação de:
 * <p>
 * CÓDIGO DO BENEFICIÁRIO (7) + NOSSO NÚMERO (17) + DATA DE VENCIMENTO
 * (DDMMAAAA) + VALOR (15) + CPF/CNPJ DO BENEFICIÁRIO (14)
 * <p>
 * Diferente do Sicredi (OAuth2/JWT reutilizável), a Caixa não emite um token de
 * sessão: cada operação calcula seu próprio hash "descartável" a partir dos
 * dados daquela requisição específica.
 */
final class CaixaHashGenerator {

    private static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("ddMMyyyy");

    private CaixaHashGenerator() {
    }

    /*
     * Hash para INCLUI_BOLETO e ALTERA_BOLETO (todos os campos obrigatórios).
     */
    static String paraInclusaoOuAlteracao(String codigoBeneficiario, long nossoNumero,
            LocalDate dataVencimento, BigDecimal valor,
            String cpfCnpjBeneficiario) {
        String dados = pad(codigoBeneficiario, 7)
                + padNumero(nossoNumero, 17)
                + dataVencimento.format(DDMMYYYY)
                + formatValor(valor)
                + pad(cpfCnpjBeneficiario, 14);
        return sha256Base64(dados);
    }

    /*
     * Hash para CONSULTA_BOLETO e BAIXA_BOLETO (vencimento e valor zerados).
     */
    static String paraConsultaOuBaixa(String codigoBeneficiario, long nossoNumero, String cpfCnpjBeneficiario) {
        String dados = pad(codigoBeneficiario, 7)
                + padNumero(nossoNumero, 17)
                + "00000000"
                + formatValor(BigDecimal.ZERO)
                + pad(cpfCnpjBeneficiario, 14);
        return sha256Base64(dados);
    }

    private static String formatValor(BigDecimal valor) {
        // 15 posições, sem separador decimal (ex.: 1000.00 -> "000000000100000")
        long centavos = valor.movePointRight(2).longValueExact();
        return padNumero(centavos, 15);
    }

    private static String pad(String value, int length) {
        String v = value == null ? "" : value;
        if (v.length() >= length) {
            return v.substring(v.length() - length);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - v.length(); i++) {
            sb.append('0');
        }
        return sb.append(v).toString();
    }

    private static String padNumero(long value, int length) {
        return pad(Long.toString(value), length);
    }

    private static String sha256Base64(String dados) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(dados.getBytes(StandardCharsets.ISO_8859_1));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível na JVM", e);
        }
    }
}
