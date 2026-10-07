package br.com.cobranca.banks.caixa;

import br.com.cobranca.exception.BoletoApiException;
import br.com.cobranca.exception.OperacaoNaoSuportadaException;
import br.com.cobranca.exception.TituloNaoEncontradoException;
import br.com.cobranca.factory.CaixaCredentials;
import br.com.cobranca.gateway.BoletoBankGateway;
import br.com.cobranca.model.*;
import br.com.cobranca.model.enums.SituacaoBoleto;
import br.com.cobranca.model.enums.TipoCobranca;
import br.com.cobranca.model.enums.TipoPessoa;
import br.com.cobranca.model.enums.TipoValor;
import org.w3c.dom.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Implementação Caixa de {@link BoletoBankGateway}.
 * <p>
 * A Caixa não expõe uma API REST/JSON como o Sicredi: a integração é feita via
 * Webservice SOAP/XML (manual "WEBSERVICE-XML-COBRANCA-BANCARIA"), com
 * operações INCLUI_BOLETO, BAIXA_BOLETO, ALTERA_BOLETO e CONSULTA_BOLETO, e
 * autenticação por hash SHA-256/Base64 calculado a cada requisição (item 4.3),
 * em vez de OAuth2. Esta classe é o adaptador responsável por:
 * <ol>
 * <li>Traduzir o modelo de domínio único {@link Boleto} (modelado no padrão
 * Sicredi) para o XML de entrada exigido pela Caixa;</li>
 * <li>Traduzir o XML de saída da Caixa de volta para os mesmos objetos de
 * retorno usados pela implementação Sicredi ({@link BoletoRegistrado},
 *       {@link BoletoConsulta}, {@link ComandoResponse}...), garantindo que o código
 * cliente trabalhe sempre com o mesmo contrato, independentemente do
 * banco.</li>
 * </ol>
 * <p>
 * <b>Limitações do banco</b> (documentadas, não escondidas do consumidor da
 * API):
 * <ul>
 * <li>{@link #consultarLiquidadosPorDia} não existe no Webservice XML da Caixa
 * (a liquidação é informada apenas via retorno CNAB 240/400, item 3.2 do
 * manual) — lança {@link OperacaoNaoSuportadaException};</li>
 * <li>{@link #consultarPorIdEmpresaOuSeuNumero} não é suportado nativamente
 * pela Caixa; é resolvido aqui por meio de um índice local (seuNumero →
 * nossoNumero) alimentado no momento do cadastro. Em uma implantação real esse
 * índice deve ser persistido (banco de dados), e não mantido apenas em memória
 * como nesta referência.</li>
 * </ul>
 */
public final class CaixaBoletoGateway implements BoletoBankGateway {

    private final HttpClient httpClient;
    private final CaixaCredentials credentials;

    /*
     * Índice local seuNumero -> nossoNumero, populado a cada cadastro (ver
     * limitação na javadoc da classe).
     */
    private final Map<String, String> indiceSeuNumero = new ConcurrentHashMap<>();
    /*
     * Índice local linhaDigitavel -> URL do PDF, populado a cada
     * cadastro/consulta.
     */
    private final Map<String, String> indiceUrlPdf = new ConcurrentHashMap<>();

    // Tamanhos máximos (coluna TAM) dos campos de texto livre, conforme item 5.2 do manual.
    private static final int TAM_NUMERO_DOCUMENTO = 11;
    private static final int TAM_NOME_RAZAO_SOCIAL = 40;
    private static final int TAM_LOGRADOURO = 40;
    private static final int TAM_BAIRRO = 15;
    private static final int TAM_CIDADE = 15;
    private static final int TAM_MENSAGEM = 40;

    public CaixaBoletoGateway(HttpClient httpClient, CaixaCredentials credentials) {
        this.httpClient = httpClient;
        this.credentials = credentials;
    }

    @Override
    public String getBankName() {
        return "CAIXA";
    }

    /*
     * Equivalente Caixa do item 7.2 (Sicredi) — operação INCLUI_BOLETO.
     */
    @Override
    public BoletoRegistrado register(Boleto boleto) {
        String envelope = buildIncluiBoletoEnvelope(boleto);
        Document response = send(CaixaSoapUrls.MANUTENCAO, envelope, "INCLUI_BOLETO");

        this.checkErro(response);

        String codigoBarras = CaixaXmlUtil.text(response, "CODIGO_BARRAS");
        String linhaDigitavel = CaixaXmlUtil.text(response, "LINHA_DIGITAVEL");
        String nossoNumero = CaixaXmlUtil.text(response, "NOSSO_NUMERO");
        String url = CaixaXmlUtil.text(response, "URL");
        String qrCode = CaixaXmlUtil.text(response, "QRCODE");

        if (boleto.getSeuNumero() != null && nossoNumero != null) {
            indiceSeuNumero.put(boleto.getSeuNumero(), nossoNumero);
        }
        if (linhaDigitavel != null && url != null) {
            indiceUrlPdf.put(linhaDigitavel, url);
        }

        return new BoletoRegistrado(
                linhaDigitavel,
                codigoBarras,
                nossoNumero,
                credentials.getUnidade(),
                null,
                qrCode,
                url
        );
    }

    /*
     * Equivalente Caixa do item 7.3 (Sicredi). A Caixa não expõe um endpoint
     * dedicado de impressão por linha digitável: a URL do PDF já é devolvida no
     * cadastro/consulta do título (campo &lt;URL&gt;), então aqui apenas
     * fazemos o download dos bytes a partir dessa URL.
     */
    @Override
    public byte[] printByLinhaDigitavel(String linhaDigitavel) {
        String url = indiceUrlPdf.get(linhaDigitavel);
        if (url == null) {
            throw new TituloNaoEncontradoException(
                    "Não há URL de boleto conhecida para a linha digitável informada. "
                    + "Realize o cadastro ou a consulta do título antes de imprimir.", 404, "CAIXA");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new BoletoApiException("Falha ao baixar PDF do boleto Caixa: HTTP "
                        + response.statusCode(), response.statusCode(), "CAIXA");
            }
            return response.body();
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação ao baixar PDF do boleto Caixa", e);
        }
    }

    /*
     * Equivalente Caixa do item 7.4 (Sicredi) — operação BAIXA_BOLETO.
     */
    @Override
    public ComandoResponse cancelByNossoNumero(String nossoNumero) {
        String hash = CaixaHashGenerator.paraConsultaOuBaixa(
                credentials.getCodigoBeneficiario(), Long.parseLong(nossoNumero), credentials.getCnpjOuCpfBeneficiario());

        String envelope = envelopeManutencaoAbertura("BAIXA_BOLETO", hash)
                + "<DADOS><BAIXA_BOLETO>"
                + "<CODIGO_BENEFICIARIO>" + credentials.getCodigoBeneficiario() + "</CODIGO_BENEFICIARIO>"
                + "<NOSSO_NUMERO>" + nossoNumero + "</NOSSO_NUMERO>"
                + "</BAIXA_BOLETO></DADOS>"
                + envelopeFechamento();

        Document response = send(CaixaSoapUrls.MANUTENCAO, envelope, "BAIXA_BOLETO");
        checkErro(response);

        return new ComandoResponse(
                UUID.randomUUID().toString(),
                nossoNumero,
                "MOVIMENTO_ENVIADO",
                OffsetDateTime.now(),
                "BAIXA"
        );
    }

    /*
     * Equivalente Caixa do item 7.18 (Sicredi) — operação CONSULTA_BOLETO.
     */
    @Override
    public Optional<BoletoConsulta> findByNossoNumero(String nossoNumero) {
        String hash = CaixaHashGenerator.paraConsultaOuBaixa(
                credentials.getCodigoBeneficiario(), Long.parseLong(nossoNumero), credentials.getCnpjOuCpfBeneficiario());

        String envelope = envelopeConsultaAbertura(hash)
                + "<DADOS><CONSULTA_BOLETO>"
                + "<CODIGO_BENEFICIARIO>" + credentials.getCodigoBeneficiario() + "</CODIGO_BENEFICIARIO>"
                + "<NOSSO_NUMERO>" + nossoNumero + "</NOSSO_NUMERO>"
                + "</CONSULTA_BOLETO></DADOS>"
                + envelopeFechamento();

        Document response = send(CaixaSoapUrls.CONSULTA, envelope, "CONSULTA_BOLETO");

        // Nível transporte/barramento (SIBAR) — erro aqui é sempre exceção, não "não encontrado".
        int codSibar = normalizaCodRetorno(CaixaXmlUtil.text(response, "COD_RETORNO"));
        if (codSibar != 0) {
            throw new BoletoApiException("Caixa (SIBAR) retornou erro (" + codSibar + ") ao consultar boleto: "
                    + firstNonNull(CaixaXmlUtil.text(response, "MSG_RETORNO"), "Erro não especificado")
                    + " | XML completo: " + CaixaXmlUtil.toXmlString(response), 422, "CAIXA");
        }

        // Nível negócio (SIGCB): na consulta, COD_RETORNO != 0 dentro de CONTROLE_NEGOCIAL
        // indica que o título não foi localizado para o nosso número informado.
        // (Antes esse valor era lido do nó errado — o COD_RETORNO do SIBAR — então esta
        // checagem praticamente nunca disparava, e uma consulta "não encontrada" acabava
        // virando um BoletoConsulta quase todo nulo dentro de um Optional.of(...).)
        int codNegocial = normalizaCodRetorno(CaixaXmlUtil.textUnder(response, "CONTROLE_NEGOCIAL", "COD_RETORNO"));
        if (codNegocial != 0) {
            return Optional.empty();
        }

        String linhaDigitavel = CaixaXmlUtil.text(response, "LINHA_DIGITAVEL");
        String url = CaixaXmlUtil.text(response, "URL");
        if (linhaDigitavel != null && url != null) {
            indiceUrlPdf.put(linhaDigitavel, url);
        }

        BoletoConsulta consulta = new BoletoConsulta(
                linhaDigitavel,
                CaixaXmlUtil.text(response, "CODIGO_BARRAS"),
                CaixaXmlUtil.text(response, "QRCODE"),
                CaixaXmlUtil.text(response, "NUMERO_DOCUMENTO"),
                nossoNumero,
                null,
                parseBigDecimal(CaixaXmlUtil.text(response, "VALOR")),
                parseDate(CaixaXmlUtil.text(response, "DATA_EMISSAO")),
                parseDate(CaixaXmlUtil.text(response, "DATA_VENCIMENTO")),
                mapSituacao(CaixaXmlUtil.textUnder(response, "MENSAGENS", "RETORNO")),
                CaixaXmlUtil.text(response, "CPF"),
                CaixaXmlUtil.text(response, "NOME")
        );
        return Optional.of(consulta);
    }

    /*
     * A Caixa não possui, no Webservice XML de Cobrança Bancária, uma operação
     * de "consulta de liquidados por dia" equivalente ao item 7.19 do Sicredi —
     * a liquidação é informada de forma consolidada via arquivo de retorno CNAB
     * (item 3.2 do manual). Portanto essa capacidade não pode ser oferecida de
     * forma fiel para este banco.
     */
    @Override
    public PaginaBoletosLiquidados findLiquidationByDate(LocalDate dia, String cpfCnpjBeneficiarioFinal, int pagina) {
        throw new OperacaoNaoSuportadaException(
                "A Caixa não expõe consulta de boletos liquidados por dia via Webservice XML; "
                + "essa informação chega apenas pelo arquivo de retorno CNAB 240/400 (item 3.2 do manual).");
    }

    /*
     * Não há, na Caixa, uma operação de busca por "idTituloEmpresa"/"seuNumero"
     * equivalente ao item 7.20 do Sicredi. Aqui resolvemos via um índice local
     * (seuNumero → nossoNumero) alimentado no cadastro, e então delegamos para
     * CONSULTA_BOLETO. Veja a limitação documentada na javadoc da classe.
     */
    @Override
    public List<BoletoConsulta> findByCompanyIdOrSeuNumero(String idTituloEmpresa, String seuNumero) {
        if (seuNumero == null) {
            throw new OperacaoNaoSuportadaException(
                    "A Caixa não possui campo idTituloEmpresa; utilize seuNumero (NUMERO_DOCUMENTO).");
        }
        String nossoNumero = indiceSeuNumero.get(seuNumero);
        if (nossoNumero == null) {
            return List.of();
        }
        return findByNossoNumero(nossoNumero).map(List::of).orElseGet(List::of);
    }

    // ---------------------------------------------------------------
    // Construção dos envelopes XML (mapeamento Boleto -> Caixa)
    // ---------------------------------------------------------------
    private String buildIncluiBoletoEnvelope(Boleto boleto) {
        long nossoNumero = boleto.getNossoNumero() == null ? 0L : boleto.getNossoNumero();
        String hash = CaixaHashGenerator.paraInclusaoOuAlteracao(
                credentials.getCodigoBeneficiario(), nossoNumero, boleto.getDataVencimento(),
                boleto.getValor(), credentials.getCnpjOuCpfBeneficiario());

        boolean hibrido = boleto.getTipoCobranca() == TipoCobranca.HIBRIDO;
        String versao = hibrido ? "3.2" : "3.0";

        StringBuilder xml = new StringBuilder();
        xml.append(envelopeManutencaoAbertura("INCLUI_BOLETO", hash, versao));
        xml.append("<DADOS><INCLUI_BOLETO>");
        xml.append(tag("CODIGO_BENEFICIARIO", credentials.getCodigoBeneficiario()));
        xml.append("<TITULO>");
        xml.append(tag("NOSSO_NUMERO", String.valueOf(nossoNumero)));
        if (hibrido) {
            xml.append(tag("TIPO", "HIBRIDO"));
        }
        xml.append(tagTexto("NUMERO_DOCUMENTO", boleto.getSeuNumero(), TAM_NUMERO_DOCUMENTO));
        xml.append(tag("DATA_VENCIMENTO", boleto.getDataVencimento().toString()));
        xml.append(tag("VALOR", boleto.getValor().setScale(2, RoundingMode.HALF_UP).toPlainString()));
        xml.append(tag("TIPO_ESPECIE", CaixaEspecieMapper.toCaixaCodigo(boleto.getEspecieDocumento())));
        xml.append(tag("FLAG_ACEITE", boleto.isAceite() ? "S" : "N"));
        xml.append(tag("DATA_EMISSAO", boleto.getDataEmissao().toString()));

        appendJuros(xml, boleto);

        if (boleto.getValorAbatimento() != null) {
            xml.append(tag("VALOR_ABATIMENTO", boleto.getValorAbatimento().toPlainString()));
        }

        // POS_VENCIMENTO não é modelado explicitamente em Boleto; assume-se DEVOLVER/0
        // (D+0) como padrão conservador. Exponha esse dado no domínio caso o
        // beneficiário precise de protesto automático.
        xml.append("<POS_VENCIMENTO>")
                .append(tag("ACAO", "DEVOLVER"))
                .append(tag("NUMERO_DIAS", "0"))
                .append("</POS_VENCIMENTO>");

        xml.append(tag("CODIGO_MOEDA", "9"));

        appendPagador(xml, boleto);

        if (boleto.getBeneficiarioFinal() != null) {
            appendSacadorAvalista(xml, boleto);
        }

        appendMulta(xml, boleto);
        appendDescontos(xml, boleto);

        if (!boleto.getInformativos().isEmpty()) {
            xml.append("<FICHA_COMPENSACAO><MENSAGENS>");
            boleto.getInformativos().forEach(m -> xml.append(tagTexto("MENSAGEM", m, TAM_MENSAGEM)));
            xml.append("</MENSAGENS></FICHA_COMPENSACAO>");
        }
        if (!boleto.getMensagens().isEmpty()) {
            xml.append("<RECIBO_PAGADOR><MENSAGENS>");
            boleto.getMensagens().forEach(m -> xml.append(tagTexto("MENSAGEM", m, TAM_MENSAGEM)));
            xml.append("</MENSAGENS></RECIBO_PAGADOR>");
        }

        xml.append("</TITULO></INCLUI_BOLETO></DADOS>");
        xml.append(envelopeFechamento());
        return xml.toString();
    }

    private void appendJuros(StringBuilder xml, Boleto boleto) {
        // JUROS_MORA é OBRIGATÓRIO no schema da Caixa (item 5.2 do manual) — mesmo quando
        // não há cobrança de juros, é preciso declarar explicitamente ISENTO/0.00.
        // Omitir o bloco inteiro quando boleto.getTipoJuros() == null quebra a validação
        // do barramento com "(BK76) ERRO NA FORMATACAO DA MENSAGEM".
        TipoValor tipo = boleto.getTipoJuros() != null ? boleto.getTipoJuros() : TipoValor.ISENTO;
        xml.append("<JUROS_MORA>");
        if (tipo == TipoValor.ISENTO) {
            xml.append(tag("TIPO", "ISENTO")).append(tag("VALOR", "0.00"));
        } else {
            String tipoCaixa = tipo == TipoValor.VALOR ? "VALOR_POR_DIA" : "TAXA_MENSAL";
            xml.append(tag("TIPO", tipoCaixa));
            if (boleto.getDataInicioJuros() != null) {
                xml.append(tag("DATA", boleto.getDataInicioJuros().toString()));
            }
            if (tipo == TipoValor.VALOR) {
                xml.append(tag("VALOR", boleto.getJuros().setScale(2, RoundingMode.HALF_UP).toPlainString()));
            } else {
                xml.append(tag("PERCENTUAL", boleto.getJuros().setScale(4, RoundingMode.HALF_UP).toPlainString()));
            }
        }
        xml.append("</JUROS_MORA>");
    }

    private void appendMulta(StringBuilder xml, Boleto boleto) {
        if (boleto.getTipoMulta() == null || boleto.getTipoMulta() == TipoValor.ISENTO) {
            return;
        }
        xml.append("<MULTA>");
        if (boleto.getDataInicioMulta() != null) {
            xml.append(tag("DATA", boleto.getDataInicioMulta().toString()));
        }
        if (boleto.getTipoMulta() == TipoValor.VALOR) {
            xml.append(tag("VALOR", boleto.getMulta().setScale(2, RoundingMode.HALF_UP).toPlainString()));
        } else {
            xml.append(tag("PERCENTUAL", boleto.getMulta().setScale(4, RoundingMode.HALF_UP).toPlainString()));
        }
        xml.append("</MULTA>");
    }

    private void appendDescontos(StringBuilder xml, Boleto boleto) {
        if (boleto.getDescontos().isEmpty()) {
            return;
        }
        xml.append("<DESCONTOS>");
        for (Desconto d : boleto.getDescontos()) {
            xml.append("<DESCONTO>");
            xml.append(tag("DATA", d.getData().toString()));
            if (d.getValor() != null) {
                xml.append(tag("VALOR", d.getValor().setScale(2, RoundingMode.HALF_UP).toPlainString()));
            } else if (d.getPercentual() != null) {
                xml.append(tag("PERCENTUAL", d.getPercentual().setScale(4, RoundingMode.HALF_UP).toPlainString()));
            }
            xml.append("</DESCONTO>");
        }
        xml.append("</DESCONTOS>");
    }

    private void appendPagador(StringBuilder xml, Boleto boleto) {
        Pagador p = boleto.getPagador();
        xml.append("<PAGADOR>");
        if (p.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            xml.append(tag("CPF", p.getDocumento()));
        } else {
            xml.append(tag("CNPJ", p.getDocumento()));
            xml.append(tagTexto("RAZAO_SOCIAL", p.getNome(), TAM_NOME_RAZAO_SOCIAL));
        }
        if (p.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            xml.append(tagTexto("NOME", p.getNome(), TAM_NOME_RAZAO_SOCIAL));
        }
        if (p.getEndereco() != null) {
            Endereco e = p.getEndereco();
            xml.append("<ENDERECO>");
            xml.append(tagTexto("LOGRADOURO", e.getLogradouro(), TAM_LOGRADOURO));
            xml.append(tagTexto("BAIRRO", e.getBairro(), TAM_BAIRRO));
            xml.append(tagTexto("CIDADE", e.getCidade(), TAM_CIDADE));
            xml.append(tag("UF", e.getUf()));
            xml.append(tag("CEP", e.getCep()));
            xml.append("</ENDERECO>");
        }
        xml.append("</PAGADOR>");
    }

    private void appendSacadorAvalista(StringBuilder xml, Boleto boleto) {
        BeneficiarioFinal bf = boleto.getBeneficiarioFinal();
        xml.append("<SACADOR_AVALISTA>");
        if (bf.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            xml.append(tag("CPF", bf.getDocumento()));
            xml.append(tagTexto("NOME", bf.getNome(), TAM_NOME_RAZAO_SOCIAL));
        } else {
            xml.append(tag("CNPJ", bf.getDocumento()));
            xml.append(tagTexto("RAZAO_SOCIAL", bf.getNome(), TAM_NOME_RAZAO_SOCIAL));
        }
        xml.append("</SACADOR_AVALISTA>");
    }

    // ---------------------------------------------------------------
    // Envelopes SOAP fixos
    // ---------------------------------------------------------------
    private String envelopeManutencaoAbertura(String operacao, String hash) {
        return envelopeManutencaoAbertura(operacao, hash, "3.0");
    }

    private String envelopeManutencaoAbertura(String operacao, String hash, String versao) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ext=\"http://caixa.gov.br/sibar/manutencao_cobranca_bancaria/boleto/externo\" "
                + "xmlns:sib=\"http://caixa.gov.br/sibar\">"
                + "<soapenv:Header/><soapenv:Body><ext:SERVICO_ENTRADA><sib:HEADER>"
                + tag("VERSAO", versao)
                + tag("AUTENTICACAO", hash)
                + tag("USUARIO_SERVICO", CaixaSoapUrls.USUARIO_SERVICO)
                + tag("OPERACAO", operacao)
                + tag("SISTEMA_ORIGEM", CaixaSoapUrls.SISTEMA_ORIGEM)
                + tag("UNIDADE", credentials.getUnidade())
                + tag("DATA_HORA", DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()))
                + "</sib:HEADER>";
    }

    private String envelopeConsultaAbertura(String hash) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ext=\"http://caixa.gov.br/sibar/consulta_cobranca_bancaria/boleto\" "
                + "xmlns:sib=\"http://caixa.gov.br/sibar\">"
                + "<soapenv:Header/><soapenv:Body><ext:SERVICO_ENTRADA><sib:HEADER>"
                + tag("VERSAO", "5.2")
                + tag("AUTENTICACAO", hash)
                + tag("USUARIO_SERVICO", CaixaSoapUrls.USUARIO_SERVICO)
                + tag("OPERACAO", "CONSULTA_BOLETO")
                + tag("SISTEMA_ORIGEM", CaixaSoapUrls.SISTEMA_ORIGEM)
                + tag("UNIDADE", credentials.getUnidade())
                + tag("DATA_HORA", DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()))
                + "</sib:HEADER>";
    }

    private String envelopeFechamento() {
        return "</ext:SERVICO_ENTRADA></soapenv:Body></soapenv:Envelope>";
    }

    // ---------------------------------------------------------------
    // Infra HTTP / parsing
    // ---------------------------------------------------------------
    private Document send(String url, String xmlBody, String operacao) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "text/xml; charset=UTF-8")
                    .header("SOAPAction", operacao)
                    .POST(HttpRequest.BodyPublishers.ofString(xmlBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new BoletoApiException("Falha HTTP na chamada Caixa " + operacao + ": "
                        + response.statusCode(), response.statusCode(), "CAIXA");
            }
            return CaixaXmlUtil.parse(response.body());
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação com o Webservice da Caixa (" + operacao + ")", e);
        }
    }

    private void checkErro(Document response) {
        // 1) Nível transporte/barramento (SIBAR) — ex.: (BK76) ERRO NA FORMATACAO DA MENSAGEM.
        int codSibar = normalizaCodRetorno(CaixaXmlUtil.text(response, "COD_RETORNO"));
        if (codSibar != 0) {
            throw new BoletoApiException("Caixa (SIBAR) retornou erro (" + codSibar + "): "
                    + firstNonNull(CaixaXmlUtil.text(response, "MSG_RETORNO"), "Erro não especificado")
                    + " | XML completo: " + CaixaXmlUtil.toXmlString(response), 422, "CAIXA");
        }

        // 2) Nível negócio (SIGCB), dentro de <DADOS><CONTROLE_NEGOCIAL>. O transporte
        // pode ter dado certo (COD_RETORNO=00 acima) e o título mesmo assim não ter
        // sido aceito — foi exatamente este caso que passava batido antes, porque
        // text("COD_RETORNO") sempre devolvia o PRIMEIRO <COD_RETORNO> do XML (o do
        // SIBAR), nunca o de CONTROLE_NEGOCIAL.
        int codNegocial = normalizaCodRetorno(CaixaXmlUtil.textUnder(response, "CONTROLE_NEGOCIAL", "COD_RETORNO"));
        if (codNegocial != 0) {
            String msg = firstNonNull(CaixaXmlUtil.textUnder(response, "MENSAGENS", "RETORNO"),
                    "Erro não especificado retornado pelo SIGCB");
            throw new BoletoApiException("Caixa (SIGCB) rejeitou a operação (" + codNegocial + "): " + msg
                    + " | XML completo: " + CaixaXmlUtil.toXmlString(response), 422, "CAIXA");
        }
    }

    /*
     * "00", "0", "000" etc. tratados como sucesso; qualquer não-numérico é
     * tratado como erro (nunca deixamos passar como sucesso silencioso).
     */
    private static int normalizaCodRetorno(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static SituacaoBoleto mapSituacao(String retorno) {
        if (retorno == null) {
            return SituacaoBoleto.DESCONHECIDO;
        }
        String upper = retorno.toUpperCase();
        if (upper.contains("EM ABERTO")) {
            return SituacaoBoleto.EM_ABERTO;
        }
        if (upper.contains("BAIXA POR DEVOLUCAO")) {
            return SituacaoBoleto.BAIXADO_POR_DEVOLUCAO;
        }
        if (upper.contains("BAIXA POR ESTORNO")) {
            return SituacaoBoleto.BAIXADO_POR_ESTORNO;
        }
        if (upper.contains("BAIXA POR PROTESTO")) {
            return SituacaoBoleto.BAIXADO_POR_PROTESTO;
        }
        if (upper.contains("LIQUIDADO NO CARTORIO")) {
            return SituacaoBoleto.LIQUIDADO_CARTORIO;
        }
        if (upper.contains("LIQUIDADO")) {
            return SituacaoBoleto.LIQUIDADO;
        }
        if (upper.contains("SUSTADO")) {
            return SituacaoBoleto.AGUARDANDO_SUSTACAO_CARTORIO;
        }
        if (upper.contains("SOMENTE PARA PROTESTO")) {
            return SituacaoBoleto.EM_CARTORIO;
        }
        if (upper.contains("ENVIADO AO CARTORIO")) {
            return SituacaoBoleto.AGUARDANDO_ENTRADA_CARTORIO;
        }
        return SituacaoBoleto.DESCONHECIDO;
    }

    private static String tag(String name, String value) {
        return "<" + name + ">" + CaixaXmlUtil.escape(value == null ? "" : value) + "</" + name + ">";
    }

    /*
     * Como {@link #tag}, mas normaliza o valor antes (item 2.3 do manual:
     * maiúsculas, sem acento, caracteres fora do conjunto admitido viram
     * espaço) e depois trunca para o tamanho máximo (coluna TAM da tabela 5.2)
     * definido pelo manual para aquele campo — nunca enviar mais caracteres do
     * que a Caixa aceita (ex.: NOME/RAZAO_SOCIAL/LOGRADOURO até 40,
     * BAIRRO/CIDADE até 15, NUMERO_DOCUMENTO até 11...).
     * <p>
     * Use apenas para campos de texto livre vindos do domínio (nome, endereço,
     * mensagens, número do documento) — nunca para códigos, enums, datas ou
     * valores numéricos.
     */
    private static String tagTexto(String name, String value, int tamanhoMaximo) {
        String normalizado = CaixaXmlUtil.normalizar(value);
        return tag(name, truncate(normalizado, tamanhoMaximo));
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    private static BigDecimal parseBigDecimal(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
    }
}
