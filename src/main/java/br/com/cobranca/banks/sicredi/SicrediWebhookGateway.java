package br.com.cobranca.banks.sicredi;

import br.com.cobranca.auth.TokenProvider;
import br.com.cobranca.exception.BoletoApiException;
import br.com.cobranca.factory.SicrediCredentials;
import br.com.cobranca.gateway.WebhookGateway;
import br.com.cobranca.model.ContratoWebhook;
import br.com.cobranca.model.enums.EventoWebhook;
import br.com.cobranca.model.enums.StatusUrlContrato;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação Sicredi de {@link WebhookGateway}, cobrindo os itens 23.2
 * (contratar), 23.3 (consultar) e 23.4 (alterar/inativar).
 */
public final class SicrediWebhookGateway implements WebhookGateway {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final TokenProvider tokenProvider;
    private final SicrediCredentials credentials;
    private final SicrediUrls urls;

    public SicrediWebhookGateway(HttpClient httpClient, ObjectMapper mapper,
            TokenProvider tokenProvider, SicrediCredentials credentials) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.tokenProvider = tokenProvider;
        this.credentials = credentials;
        this.urls = new SicrediUrls(credentials.isSandbox());
    }

    /* 23.2 - Realizar Contratação Webhook. */
    @Override
    public ContratoWebhook register(ContratoWebhook contrato) {
        ObjectNode body = toRequestJson(contrato);
        HttpRequest request = baseRequestBuilder(urls.webhookContrato())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        JsonNode json = sendAndParseJson(request, 201, 200);
        return toContrato(json);
    }

    /* 23.3 - Consulta de Contratos. */
    @Override
    public List<ContratoWebhook> list(String cooperativa, String posto, String codigoBeneficiario) {
        HttpRequest request = baseRequestBuilder(urls.webhookContratos(cooperativa, posto, codigoBeneficiario))
                .GET()
                .build();
        JsonNode json = sendAndParseJson(request, 200);
        List<ContratoWebhook> result = new ArrayList<>();
        if (json.isArray()) {
            json.forEach(item -> result.add(toContrato(item)));
        } else if (!json.isMissingNode() && !json.isNull()) {
            result.add(toContrato(json));
        }
        return result;
    }

    /*
     * 23.4 - Altera Contrato Webhook.
     * Para inativar/remover o contrato, reenvie os mesmos dados com
     * {@code contratoStatus(StatusUrlContrato.INATIVO)} (não há DELETE físico na API do Sicredi).
     */
    @Override
    public ContratoWebhook change(String idContrato, ContratoWebhook contratoAtualizado) {
        ObjectNode body = toRequestJson(contratoAtualizado);
        HttpRequest request = baseRequestBuilder(urls.webhookContrato(idContrato))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        JsonNode json = sendAndParseJson(request, 200, 202);
        return toContrato(json);
    }

    // ---- mapeamento JSON ----
    private ObjectNode toRequestJson(ContratoWebhook c) {
        ObjectNode root = mapper.createObjectNode();
        root.put("cooperativa", c.getCooperativa());
        root.put("posto", c.getPosto());
        root.put("codBeneficiario", c.getCodigoBeneficiario());
        var eventos = root.putArray("eventos");
        c.getEventos().forEach(e -> eventos.add(e.name()));
        root.put("url", c.getUrl());
        root.put("urlStatus", c.getUrlStatus().name());
        root.put("contratoStatus", c.getContratoStatus().name());
        if (c.getNomeResponsavel() != null) {
            root.put("nomeResponsavel", c.getNomeResponsavel());
        }
        if (c.getEmail() != null) {
            root.put("email", c.getEmail());
        }
        if (c.getTelefone() != null) {
            root.put("telefone", c.getTelefone());
        }
        if (c.getEnviarIdTituloEmpresa() != null) {
            root.put("enviarIdTituloEmpresa", c.getEnviarIdTituloEmpresa());
        }
        if (c.getHeader() != null) {
            root.put("header", c.getHeader());
        }
        if (c.getToken() != null) {
            root.put("token", c.getToken());
        }
        return root;
    }

    private ContratoWebhook toContrato(JsonNode json) {
        ContratoWebhook.Builder b = ContratoWebhook.builder()
                .idContrato(json.path("idContrato").asText(null))
                .cooperativa(json.path("cooperativa").asText(null))
                .posto(json.path("posto").asText(null))
                .codigoBeneficiario(json.path("codBeneficiario").asText(null))
                .url(json.path("url").asText(null))
                .urlStatus(StatusUrlContrato.valueOf(json.path("urlStatus").asText("ATIVO")))
                .contratoStatus(StatusUrlContrato.valueOf(json.path("contratoStatus").asText("ATIVO")))
                .nomeResponsavel(json.path("nomeResponsavel").asText(null))
                .email(json.path("email").asText(null))
                .telefone(json.path("telefone").asText(null))
                .header(json.path("header").asText(null))
                .token(json.path("token").asText(null));
        if (json.hasNonNull("enviarIdTituloEmpresa")) {
            b.enviarIdTituloEmpresa(json.get("enviarIdTituloEmpresa").asBoolean());
        }
        json.path("eventos").forEach(e -> b.comEvento(EventoWebhook.valueOf(e.asText())));
        return b.build();
    }

    private HttpRequest.Builder baseRequestBuilder(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("x-api-key", credentials.getClientApiKey())
                .header("Authorization", tokenProvider.getValidToken().getBearerHeader());
    }

    private JsonNode sendAndParseJson(HttpRequest request, int... acceptedStatus) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = false;
            for (int s : acceptedStatus) {
                if (s == response.statusCode()) {
                    ok = true;
                }
            }
            if (!ok) {
                throw new BoletoApiException(
                        "Falha na chamada de Webhook Sicredi (" + request.uri() + "): HTTP "
                        + response.statusCode() + " - " + response.body(),
                        response.statusCode(), "SICREDI");
            }
            return response.body() == null || response.body().isBlank()
                    ? mapper.createObjectNode()
                    : mapper.readTree(response.body());
        } catch (BoletoApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BoletoApiException("Erro de comunicação com o Webhook do Sicredi: " + e.getMessage(), e);
        }
    }
}
