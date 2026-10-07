package br.com.cobranca.model;

import br.com.cobranca.model.enums.EspecieDocumento;
import br.com.cobranca.model.enums.TipoCobranca;
import br.com.cobranca.model.enums.TipoJurosPercentual;
import br.com.cobranca.model.enums.TipoValor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Modelo de domínio único (bank-agnostic) para cadastro de boleto, modelado no
 * formato do Sicredi (item 7.2 do manual) por ser o padrão escolhido para
 * unificar a integração entre bancos.
 * <p>
 * Cada {@link br.com.cobranca.gateway.BoletoBankGateway} é responsável por
 * converter esta representação para o contrato nativo do banco (JSON REST no
 * Sicredi, XML/SOAP na Caixa).
 */
public final class Boleto {

    private final String codigoBeneficiario;
    private final TipoCobranca tipoCobranca;
    private final Long nossoNumero;               // null/0 => gerado pelo banco
    private final String seuNumero;
    private final String idTituloEmpresa;         // usado na consulta 7.20 (Sicredi)
    private final LocalDate dataVencimento;
    private final LocalDate dataEmissao;
    private final BigDecimal valor;
    private final EspecieDocumento especieDocumento;
    private final boolean aceite;

    private final Pagador pagador;
    private final BeneficiarioFinal beneficiarioFinal;

    private final TipoValor tipoDesconto;
    private final List<Desconto> descontos;

    private final TipoValor tipoJuros;
    private final TipoJurosPercentual tipoJurosPercentual;
    private final BigDecimal juros;
    private final LocalDate dataInicioJuros;

    private final TipoValor tipoMulta;
    private final BigDecimal multa;
    private final LocalDate dataInicioMulta;

    private final BigDecimal valorAbatimento;

    private final List<String> informativos;
    private final List<String> mensagens;

    private Boleto(Builder b) {
        this.codigoBeneficiario = b.codigoBeneficiario;
        this.tipoCobranca = b.tipoCobranca;
        this.nossoNumero = b.nossoNumero;
        this.seuNumero = b.seuNumero;
        this.idTituloEmpresa = b.idTituloEmpresa;
        this.dataVencimento = b.dataVencimento;
        this.dataEmissao = b.dataEmissao;
        this.valor = b.valor;
        this.especieDocumento = b.especieDocumento;
        this.aceite = b.aceite;
        this.pagador = b.pagador;
        this.beneficiarioFinal = b.beneficiarioFinal;
        this.tipoDesconto = b.tipoDesconto;
        this.descontos = List.copyOf(b.descontos);
        this.tipoJuros = b.tipoJuros;
        this.tipoJurosPercentual = b.tipoJurosPercentual;
        this.juros = b.juros;
        this.dataInicioJuros = b.dataInicioJuros;
        this.tipoMulta = b.tipoMulta;
        this.multa = b.multa;
        this.dataInicioMulta = b.dataInicioMulta;
        this.valorAbatimento = b.valorAbatimento;
        this.informativos = List.copyOf(b.informativos);
        this.mensagens = List.copyOf(b.mensagens);
    }

    public String getCodigoBeneficiario() {
        return codigoBeneficiario;
    }

    public TipoCobranca getTipoCobranca() {
        return tipoCobranca;
    }

    public Long getNossoNumero() {
        return nossoNumero;
    }

    public String getSeuNumero() {
        return seuNumero;
    }

    public String getIdTituloEmpresa() {
        return idTituloEmpresa;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public EspecieDocumento getEspecieDocumento() {
        return especieDocumento;
    }

    public boolean isAceite() {
        return aceite;
    }

    public Pagador getPagador() {
        return pagador;
    }

    public BeneficiarioFinal getBeneficiarioFinal() {
        return beneficiarioFinal;
    }

    public TipoValor getTipoDesconto() {
        return tipoDesconto;
    }

    public List<Desconto> getDescontos() {
        return descontos;
    }

    public TipoValor getTipoJuros() {
        return tipoJuros;
    }

    public TipoJurosPercentual getTipoJurosPercentual() {
        return tipoJurosPercentual;
    }

    public BigDecimal getJuros() {
        return juros;
    }

    public LocalDate getDataInicioJuros() {
        return dataInicioJuros;
    }

    public TipoValor getTipoMulta() {
        return tipoMulta;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public LocalDate getDataInicioMulta() {
        return dataInicioMulta;
    }

    public BigDecimal getValorAbatimento() {
        return valorAbatimento;
    }

    public List<String> getInformativos() {
        return informativos;
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String codigoBeneficiario;
        private TipoCobranca tipoCobranca = TipoCobranca.NORMAL;
        private Long nossoNumero = null;
        private String seuNumero;
        private String idTituloEmpresa;
        private LocalDate dataVencimento;
        private LocalDate dataEmissao = LocalDate.now();
        private BigDecimal valor;
        private EspecieDocumento especieDocumento = EspecieDocumento.OUTROS;
        private boolean aceite = false;
        private Pagador pagador;
        private BeneficiarioFinal beneficiarioFinal;
        private TipoValor tipoDesconto;
        private List<Desconto> descontos = new java.util.ArrayList<>();
        private TipoValor tipoJuros;
        private TipoJurosPercentual tipoJurosPercentual;
        private BigDecimal juros;
        private LocalDate dataInicioJuros;
        private TipoValor tipoMulta;
        private BigDecimal multa;
        private LocalDate dataInicioMulta;
        private BigDecimal valorAbatimento;
        private List<String> informativos = new java.util.ArrayList<>();
        private List<String> mensagens = new java.util.ArrayList<>();

        public Builder codigoBeneficiario(String v) {
            this.codigoBeneficiario = v;
            return this;
        }

        public Builder tipoCobranca(TipoCobranca v) {
            this.tipoCobranca = v;
            return this;
        }

        public Builder nossoNumero(Long v) {
            this.nossoNumero = v;
            return this;
        }

        public Builder seuNumero(String v) {
            this.seuNumero = v;
            return this;
        }

        public Builder idTituloEmpresa(String v) {
            this.idTituloEmpresa = v;
            return this;
        }

        public Builder dataVencimento(LocalDate v) {
            this.dataVencimento = v;
            return this;
        }

        public Builder dataEmissao(LocalDate v) {
            this.dataEmissao = v;
            return this;
        }

        public Builder valor(BigDecimal v) {
            this.valor = v;
            return this;
        }

        public Builder especieDocumento(EspecieDocumento v) {
            this.especieDocumento = v;
            return this;
        }

        public Builder aceite(boolean v) {
            this.aceite = v;
            return this;
        }

        public Builder pagador(Pagador v) {
            this.pagador = v;
            return this;
        }

        public Builder beneficiarioFinal(BeneficiarioFinal v) {
            this.beneficiarioFinal = v;
            return this;
        }

        public Builder comDescontoValor(BigDecimal valor, LocalDate data) {
            this.tipoDesconto = TipoValor.VALOR;
            this.descontos.add(new Desconto(data, valor, null));
            return this;
        }

        public Builder comDescontoPercentual(BigDecimal percentual, LocalDate data) {
            this.tipoDesconto = TipoValor.PERCENTUAL;
            this.descontos.add(new Desconto(data, null, percentual));
            return this;
        }

        public Builder comJurosValorDiario(BigDecimal valorPorDia, LocalDate dataInicio) {
            this.tipoJuros = TipoValor.VALOR;
            this.tipoJurosPercentual = TipoJurosPercentual.DIARIO;
            this.juros = valorPorDia;
            this.dataInicioJuros = dataInicio;
            return this;
        }

        public Builder comJurosPercentualMensal(BigDecimal percentualMensal, LocalDate dataInicio) {
            this.tipoJuros = TipoValor.PERCENTUAL;
            this.tipoJurosPercentual = TipoJurosPercentual.MENSAL;
            this.juros = percentualMensal;
            this.dataInicioJuros = dataInicio;
            return this;
        }

        public Builder semJuros() {
            this.tipoJuros = TipoValor.ISENTO;
            this.juros = BigDecimal.ZERO;
            this.dataInicioJuros = null;
            return this;
        }

        public Builder comMultaPercentual(BigDecimal percentual, LocalDate dataInicio) {
            this.tipoMulta = TipoValor.PERCENTUAL;
            this.multa = percentual;
            this.dataInicioMulta = dataInicio;
            return this;
        }

        public Builder comMultaValor(BigDecimal valor, LocalDate dataInicio) {
            this.tipoMulta = TipoValor.VALOR;
            this.multa = valor;
            this.dataInicioMulta = dataInicio;
            return this;
        }

        public Builder valorAbatimento(BigDecimal v) {
            this.valorAbatimento = v;
            return this;
        }

        public Builder comInformativo(String v) {
            this.informativos.add(v);
            return this;
        }

        public Builder comMensagem(String v) {
            this.mensagens.add(v);
            return this;
        }

        public Boleto build() {
            Objects.requireNonNull(codigoBeneficiario, "codigoBeneficiario é obrigatório");
            Objects.requireNonNull(dataVencimento, "dataVencimento é obrigatória");
            Objects.requireNonNull(valor, "valor é obrigatório");
            Objects.requireNonNull(pagador, "pagador é obrigatório");
            if (informativos.size() > 5) {
                throw new IllegalStateException("informativos permite no máximo 5 ocorrências");
            }
            if (mensagens.size() > 4) {
                throw new IllegalStateException("mensagens permite no máximo 4 ocorrências");
            }
            if (tipoCobranca == TipoCobranca.HIBRIDO && descontos.size() > 1) {
                throw new IllegalStateException("boleto HIBRIDO permite apenas 1 faixa de desconto");
            }
            return new Boleto(this);
        }
    }
}
