package br.com.cobranca.model;

import java.util.Objects;

/**
 * Endereço genérico, usado tanto para Pagador quanto para Beneficiário Final.
 */
public final class Endereco {

    private final String logradouro;
    private final Integer numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String cep;

    private Endereco(Builder b) {
        this.logradouro = b.logradouro;
        this.numero = b.numero;
        this.complemento = b.complemento;
        this.bairro = b.bairro;
        this.cidade = b.cidade;
        this.uf = b.uf;
        this.cep = b.cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getCep() {
        return cep;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String logradouro;
        private Integer numero;
        private String complemento;
        private String bairro;
        private String cidade;
        private String uf;
        private String cep;

        public Builder logradouro(String v) {
            this.logradouro = v;
            return this;
        }

        public Builder numero(Integer v) {
            this.numero = v;
            return this;
        }

        public Builder complemento(String v) {
            this.complemento = v;
            return this;
        }

        public Builder bairro(String v) {
            this.bairro = v;
            return this;
        }

        public Builder cidade(String v) {
            this.cidade = v;
            return this;
        }

        public Builder uf(String v) {
            this.uf = v;
            return this;
        }

        public Builder cep(String v) {
            this.cep = v;
            return this;
        }

        public Endereco build() {
            Objects.requireNonNull(cidade, "cidade é obrigatória");
            Objects.requireNonNull(uf, "uf é obrigatória");
            return new Endereco(this);
        }
    }
}
