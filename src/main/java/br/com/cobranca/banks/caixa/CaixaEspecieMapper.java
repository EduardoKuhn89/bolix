package br.com.cobranca.banks.caixa;

import br.com.cobranca.model.enums.EspecieDocumento;

import java.util.EnumMap;
import java.util.Map;

/**
 * De-para entre {@link EspecieDocumento} (nomes Sicredi) e os códigos numéricos
 * da Caixa (NE006).
 */
final class CaixaEspecieMapper {

    private static final Map<EspecieDocumento, String> MAP = new EnumMap<>(EspecieDocumento.class);

    static {
        MAP.put(EspecieDocumento.CHEQUE, "01");
        MAP.put(EspecieDocumento.DUPLICATA_MERCANTIL, "02");
        MAP.put(EspecieDocumento.DUPLICATA_MERCANTIL_INDICACAO, "03");
        MAP.put(EspecieDocumento.DUPLICATA_SERVICO, "04");
        MAP.put(EspecieDocumento.DUPLICATA_SERVICO_INDICACAO, "05");
        MAP.put(EspecieDocumento.DUPLICATA_RURAL, "06");
        MAP.put(EspecieDocumento.LETRA_CAMBIO, "07");
        MAP.put(EspecieDocumento.NOTA_PROMISSORIA, "12");
        MAP.put(EspecieDocumento.NOTA_PROMISSORIA_RURAL, "13");
        MAP.put(EspecieDocumento.TRIPLICATA_MERCANTIL, "14");
        MAP.put(EspecieDocumento.TRIPLICATA_SERVICO, "15");
        MAP.put(EspecieDocumento.NOTA_SEGURO, "16");
        MAP.put(EspecieDocumento.RECIBO, "17");
        MAP.put(EspecieDocumento.FATURA, "18");
        MAP.put(EspecieDocumento.NOTA_DEBITO, "19");
        MAP.put(EspecieDocumento.APOLICE_SEGURO, "20");
        MAP.put(EspecieDocumento.MENSALIDADE_ESCOLAR, "21");
        MAP.put(EspecieDocumento.PARCELA_CONSORCIO, "22");
        MAP.put(EspecieDocumento.NOTA_FISCAL, "23");
        MAP.put(EspecieDocumento.DOCUMENTO_DIVIDA, "24");
        MAP.put(EspecieDocumento.CEDULA_PRODUTO_RURAL, "25");
        MAP.put(EspecieDocumento.CARTAO_CREDITO, "31");
        MAP.put(EspecieDocumento.BOLETO_PROPOSTA, "32");
        MAP.put(EspecieDocumento.BOLETO_DEPOSITO_APORTE, "33");
        MAP.put(EspecieDocumento.OUTROS, "99");
    }

    private CaixaEspecieMapper() {
    }

    static String toCaixaCodigo(EspecieDocumento especie) {
        return MAP.getOrDefault(especie, "99");
    }
}
