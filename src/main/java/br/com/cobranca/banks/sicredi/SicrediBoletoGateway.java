package br.com.cobranca.banks.sicredi;

import br.com.cobranca.auth.TokenProvider;
import br.com.cobranca.exception.BoletoApiException;
import br.com.cobranca.exception.TituloNaoEncontradoException;
import br.com.cobranca.factory.SicrediCredentials;
import br.com.cobranca.gateway.BoletoBankGateway;
import br.com.cobranca.model.Boleto;
import br.com.cobranca.model.BoletoConsulta;
import br.com.cobranca.model.BoletoLiquidado;
import br.com.cobranca.model.BoletoRegistrado;
import br.com.cobranca.model.ComandoResponse;
import br.com.cobranca.model.PaginaBoletosLiquidados;
import br.com.cobranca.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Implementação Sicredi de {@link BoletoBankGateway}, cobrindo os itens 7.2,
 * 7.3, 7.4, 7.18, 7.19 e 7.20 do manual da API de Cobrança.
 */
public final class SicrediBoletoGateway implements BoletoBankGateway {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final TokenProvider tokenProvider;
    private final SicrediCredentials credentials;
    private final SicrediUrls urls;
    private final SicrediBoletoMapper boletoMapper;

    public SicrediBoletoGateway(HttpClient httpClient, ObjectMapper mapper, TokenProvider tokenProvider, SicrediCredentials credentials) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.tokenProvider = tokenProvider;
        this.credentials = credentials;
        this.urls = new SicrediUrls(credentials.isSandbox());
        this.boletoMapper = new SicrediBoletoMapper(mapper);
    }

    @Override
    public String getBankName() {
        return "SICREDI";
    }

    /*
     * 7.2 - Cadastro de Boletos.
     */
    @Override
    public BoletoRegistrado register(Boleto boleto) {
        ObjectNode body = boletoMapper.toRequestJson(boleto);
        HttpRequest request = baseRequestBuilder(urls.boletos())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        JsonNode json = sendAndParseJson(request, 201, 200);
        return boletoMapper.toBoletoRegistrado(json);
    }

    /*
     * 7.3 - Impressão de Boletos (retorna o PDF em bytes).
     */
    @Override
    public byte[] printByLinhaDigitavel(String linhaDigitavel) {
        String uri = urls.boletosPdf() + "?linhaDigitavel=" + linhaDigitavel;
        HttpRequest request = baseRequestBuilder(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 422) {
                throw new TituloNaoEncontradoException(
                        "Boleto não localizado para a linha digitável informada.", 422, "SICREDI");
            }
            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new BoletoApiException(
                        "Falha ao imprimir boleto: HTTP " + response.statusCode(), response.statusCode(), "SICREDI");
            }
            return response.body();
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação ao imprimir boleto no Sicredi", e);
        }
    }

    /*
     * 7.4 - Comando de Instrução - Pedido de Baixa.
     */
    @Override
    public ComandoResponse cancelByNossoNumero(String nossoNumero) {
        HttpRequest request = baseRequestBuilder(urls.baixa(nossoNumero))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        JsonNode json = sendAndParseJson(request, 202);
        return new ComandoResponse(
                json.path("transactionId").asText(null),
                json.path("nossoNumero").asText(null),
                json.path("statusComando").asText(null),
                json.hasNonNull("dataHoraRegistro")
                ? DateUtils.parseToOffsetDateTime(json.get("dataHoraRegistro").asText()) : null,
                json.path("tipoMensagem").asText(null)
        );
    }

    /*
     * 7.18 - Consulta de Boletos por Nosso Número.
     */
    @Override
    public Optional<BoletoConsulta> findByNossoNumero(String nossoNumero) {
        String uri = urls.consultaNossoNumero()
                + "?codigoBeneficiario=" + credentials.getCodigoBeneficiario()
                + "&nossoNumero=" + nossoNumero;
        HttpRequest request = baseRequestBuilder(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            if (response.statusCode() != 200) {
                throw new BoletoApiException(
                        "Falha ao consultar boleto por nosso número: " + response.body(), response.statusCode(), "SICREDI");
            }
            return Optional.of(boletoMapper.toBoletoConsulta(mapper.readTree(response.body())));
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação ao consultar boleto no Sicredi", e);
        }
    }

    /*
     * 7.19 - Consulta de Boletos Liquidados por Dia.
     */
    @Override
    public PaginaBoletosLiquidados findLiquidationByDate(LocalDate dia, String cpfCnpjBeneficiarioFinal, int pagina) {
        StringBuilder uri = new StringBuilder(urls.liquidadosPorDia())
                .append("?codigoBeneficiario=").append(credentials.getCodigoBeneficiario())
                .append("&dia=").append(String.format("%02d/%02d/%04d", dia.getDayOfMonth(), dia.getMonthValue(), dia.getYear()))
                .append("&pagina=").append(pagina);
        if (cpfCnpjBeneficiarioFinal != null) {
            uri.append("&cpfCnpjBeneficiarioFinal=").append(cpfCnpjBeneficiarioFinal);
        }
        HttpRequest request = baseRequestBuilder(uri.toString())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .GET()
                .build();
        JsonNode json = sendAndParseJson(request, 200);
        List<BoletoLiquidado> items = new ArrayList<>();
        json.path("items").forEach(item -> items.add(boletoMapper.toBoletoLiquidado(item)));
        return new PaginaBoletosLiquidados(items, json.path("hasNext").asBoolean(false));
    }

    /*
     * 7.20 - Consulta título cadastrado por "idEmpresa" (idTituloEmpresa) ou
     * "seuNumero".
     */
    @Override
    public List<BoletoConsulta> findByCompanyIdOrSeuNumero(String idTituloEmpresa, String seuNumero) {
        if ((idTituloEmpresa == null) == (seuNumero == null)) {
            throw new IllegalArgumentException(
                    "Informe exatamente um dos dois parâmetros: idTituloEmpresa ou seuNumero.");
        }
        StringBuilder uri = new StringBuilder(urls.cadastrados())
                .append("?codigoBeneficiario=").append(credentials.getCodigoBeneficiario());
        if (idTituloEmpresa != null) {
            uri.append("&idTituloEmpresa=").append(idTituloEmpresa);
        }
        if (seuNumero != null) {
            uri.append("&seuNumero=").append(seuNumero);
        }

        HttpRequest request = baseRequestBuilder(uri.toString())
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return List.of();
            }
            if (response.statusCode() != 200) {
                throw new BoletoApiException(
                        "Falha ao consultar título por idTituloEmpresa/seuNumero: " + response.body(),
                        response.statusCode(), "SICREDI");
            }
            JsonNode array = mapper.readTree(response.body());
            List<BoletoConsulta> result = new ArrayList<>();
            array.forEach(item -> result.add(boletoMapper.toBoletoConsulta(item)));
            return result;
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação ao consultar título no Sicredi", e);
        }
    }

    // ---- infraestrutura HTTP comum ----
    private HttpRequest.Builder baseRequestBuilder(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("x-api-key", credentials.getClientApiKey())
                .header("Authorization", tokenProvider.getValidToken().getBearerHeader())
                .header("cooperativa", credentials.getCooperativa())
                .header("posto", credentials.getPosto())
                .header("codigoBeneficiario", credentials.getCodigoBeneficiario());
    }

    private JsonNode sendAndParseJson(HttpRequest request, int... acceptedStatus) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = false;
            for (int s : acceptedStatus) {
                if (s == response.statusCode()) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                if (response.statusCode() == 404) {
                    throw new TituloNaoEncontradoException(response.body(), 404, "SICREDI");
                }
                throw new BoletoApiException(
                        "Falha na chamada Sicredi (" + request.uri() + "): HTTP " + response.statusCode()
                        + " - " + response.body(),
                        response.statusCode(), "SICREDI");
            }
            return response.body() == null || response.body().isBlank()
                    ? mapper.createObjectNode()
                    : mapper.readTree(response.body());
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação com o Sicredi: " + e.getMessage(), e);
        }
    }
}
