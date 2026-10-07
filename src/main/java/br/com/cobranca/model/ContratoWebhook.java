package br.com.cobranca.model;

import br.com.cobranca.model.enums.EventoWebhook;
import br.com.cobranca.model.enums.StatusUrlContrato;
import java.util.Arrays;

import java.util.List;
import java.util.Objects;

/**
 * Contrato de Webhook, modelado no formato do Sicredi (item 23.2). Usado tanto
 * para a requisição de contratação/alteração quanto para as respostas de
 * consulta (campo idContrato só é preenchido no retorno).
 */
public final class ContratoWebhook {

    private final String idContrato;      // preenchido pelo banco na resposta
    private final String cooperativa;
    private final String posto;
    private final String codigoBeneficiario;
    private final List<EventoWebhook> eventos;
    private final String url;
    private final StatusUrlContrato urlStatus;
    private final StatusUrlContrato contratoStatus;
    private final String nomeResponsavel;
    private final String email;
    private final String telefone;
    private final Boolean enviarIdTituloEmpresa;
    private final String header; // secret-id, se a API do associado exigir autenticação
    private final String token;  // senha correspondente ao header

    private ContratoWebhook(Builder b) {
        this.idContrato = b.idContrato;
        this.cooperativa = b.cooperativa;
        this.posto = b.posto;
        this.codigoBeneficiario = b.codigoBeneficiario;
        this.eventos = List.copyOf(b.eventos);
        this.url = b.url;
        this.urlStatus = b.urlStatus;
        this.contratoStatus = b.contratoStatus;
        this.nomeResponsavel = b.nomeResponsavel;
        this.email = b.email;
        this.telefone = b.telefone;
        this.enviarIdTituloEmpresa = b.enviarIdTituloEmpresa;
        this.header = b.header;
        this.token = b.token;
    }

    public String getIdContrato() {
        return idContrato;
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

    public List<EventoWebhook> getEventos() {
        return eventos;
    }

    public String getUrl() {
        return url;
    }

    public StatusUrlContrato getUrlStatus() {
        return urlStatus;
    }

    public StatusUrlContrato getContratoStatus() {
        return contratoStatus;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Boolean getEnviarIdTituloEmpresa() {
        return enviarIdTituloEmpresa;
    }

    public String getHeader() {
        return header;
    }

    public String getToken() {
        return token;
    }

    /**
     * Cria um builder de alteração pré-preenchido com os dados deste contrato.
     */
    public Builder toBuilder() {
        Builder b = new Builder()
                .idContrato(idContrato)
                .cooperativa(cooperativa)
                .posto(posto)
                .codigoBeneficiario(codigoBeneficiario)
                .url(url)
                .urlStatus(urlStatus)
                .contratoStatus(contratoStatus)
                .nomeResponsavel(nomeResponsavel)
                .email(email)
                .telefone(telefone)
                .enviarIdTituloEmpresa(enviarIdTituloEmpresa)
                .header(header)
                .token(token);
        eventos.forEach(b::comEvento);
        return b;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String idContrato;
        private String cooperativa;
        private String posto;
        private String codigoBeneficiario;
        private final List<EventoWebhook> eventos = new java.util.ArrayList<>();
        private String url;
        private StatusUrlContrato urlStatus = StatusUrlContrato.ATIVO;
        private StatusUrlContrato contratoStatus = StatusUrlContrato.ATIVO;
        private String nomeResponsavel;
        private String email;
        private String telefone;
        private Boolean enviarIdTituloEmpresa;
        private String header;
        private String token;

        public Builder idContrato(String v) {
            this.idContrato = v;
            return this;
        }

        public Builder cooperativa(String v) {
            this.cooperativa = v;
            return this;
        }

        public Builder posto(String v) {
            this.posto = v;
            return this;
        }

        public Builder codigoBeneficiario(String v) {
            this.codigoBeneficiario = v;
            return this;
        }

        public Builder comEvento(EventoWebhook v) {
            this.eventos.add(v);
            return this;
        }

        public Builder comEventos(EventoWebhook... list) {
            this.eventos.clear();
            this.eventos.addAll(Arrays.asList(list));
            return this;
        }

        public Builder url(String v) {
            this.url = v;
            return this;
        }

        public Builder urlStatus(StatusUrlContrato v) {
            this.urlStatus = v;
            return this;
        }

        public Builder contratoStatus(StatusUrlContrato v) {
            this.contratoStatus = v;
            return this;
        }

        public Builder nomeResponsavel(String v) {
            this.nomeResponsavel = v;
            return this;
        }

        public Builder email(String v) {
            this.email = v;
            return this;
        }

        public Builder telefone(String v) {
            this.telefone = v;
            return this;
        }

        public Builder enviarIdTituloEmpresa(Boolean v) {
            this.enviarIdTituloEmpresa = v;
            return this;
        }

        public Builder header(String v) {
            this.header = v;
            return this;
        }

        public Builder token(String v) {
            this.token = v;
            return this;
        }

        public ContratoWebhook build() {
            Objects.requireNonNull(cooperativa, "cooperativa é obrigatória");
            Objects.requireNonNull(posto, "posto é obrigatório");
            Objects.requireNonNull(codigoBeneficiario, "codigoBeneficiario é obrigatório");
            Objects.requireNonNull(url, "url é obrigatória");
            if (!url.startsWith("https://")) {
                throw new IllegalStateException("url deve, obrigatoriamente, utilizar o protocolo https");
            }
            if (eventos.isEmpty()) {
                throw new IllegalStateException("é necessário informar ao menos um evento (ex.: LIQUIDACAO)");
            }
            return new ContratoWebhook(this);
        }
    }
}
