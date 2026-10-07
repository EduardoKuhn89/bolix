package br.com.cobranca.model;

import br.com.cobranca.model.enums.TipoPessoa;
import java.util.Objects;

/**
 * Pagador (sacado) do boleto.
 */
public final class Pagador {

    private final TipoPessoa tipoPessoa;
    private final String documento;
    private final String nome;
    private final Endereco endereco;

    private Pagador(Builder b) {
        this.tipoPessoa = b.tipoPessoa;
        this.documento = b.documento;
        this.nome = b.nome;
        this.endereco = b.endereco;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNome() {
        return nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private TipoPessoa tipoPessoa;
        private String documento;
        private String nome;
        private Endereco endereco;

        public Builder tipoPessoa(TipoPessoa v) {
            this.tipoPessoa = v;
            return this;
        }

        public Builder documento(String v) {
            this.documento = v;
            return this;
        }

        public Builder nome(String v) {
            this.nome = v;
            return this;
        }

        public Builder endereco(Endereco v) {
            this.endereco = v;
            return this;
        }

        public Pagador build() {
            Objects.requireNonNull(tipoPessoa, "tipoPessoa é obrigatório");
            Objects.requireNonNull(documento, "documento é obrigatório");
            Objects.requireNonNull(nome, "nome é obrigatório");
            return new Pagador(this);
        }
    }
}
