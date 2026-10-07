package br.com.cobranca.gateway;

import br.com.cobranca.model.Boleto;
import br.com.cobranca.model.BoletoConsulta;
import br.com.cobranca.model.BoletoRegistrado;
import br.com.cobranca.model.ComandoResponse;
import br.com.cobranca.model.PaginaBoletosLiquidados;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato único (bank-agnostic) para o módulo Boleto, modelado a partir dos
 * recursos do Sicredi:
 * <p>
 * 7.1 Autenticação<br>
 * 7.2 Cadastro de Boletos<br>
 * 7.3 Impressão de Boletos<br>
 * 7.4 Comando de Instrução - Pedido de Baixa<br>
 * 7.18 Consulta de Boletos por Nosso Número<br>
 * 7.19 Consulta de Boletos Liquidados por Dia<br>
 * 7.20 Consulta título cadastrado por idEmpresa/seuNumero
 * <p>
 * Cada banco tem sua própria implementação (ex.: {@code SicrediBoletoGateway},
 * {@code CaixaBoletoGateway}), traduzindo o modelo de domínio único para o
 * protocolo nativo do banco (REST/JSON no Sicredi, SOAP/XML na Caixa).
 */
public interface BoletoBankGateway {

    /*
     * 7.2 - Cadastro de Boletos (tradicional ou híbrido, conforme
     * {@code Boleto#getTipoCobranca()}).
     */
    BoletoRegistrado register(Boleto boleto);

    /*
     * 7.3 - Impressão de Boletos: retorna os bytes do PDF do boleto.
     */
    byte[] printByLinhaDigitavel(String linhaDigitavel);

    /*
     * 7.4 - Comando de Instrução - Pedido de Baixa.
     */
    ComandoResponse cancelByNossoNumero(String nossoNumero);

    /*
     * 7.18 - Consulta de Boletos por Nosso Número.
     */
    Optional<BoletoConsulta> findByNossoNumero(String nossoNumero);

    /*
     * 7.19 - Consulta de Boletos Liquidados por Dia.
     */
    PaginaBoletosLiquidados findLiquidationByDate(LocalDate date, String cpfCnpjBeneficiario, int page);

    /*
     * 7.20 - Consulta título cadastrado por "idEmpresa" (idTituloEmpresa) ou
     * "seuNumero".
     */
    List<BoletoConsulta> findByCompanyIdOrSeuNumero(String idEmpresa, String seuNumero);

    /*
     * Identifica de forma legível qual banco esta instância representa (ex.:
     * "SICREDI", "CAIXA").
     */
    String getBankName();
}
