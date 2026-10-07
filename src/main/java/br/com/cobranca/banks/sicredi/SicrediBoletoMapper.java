package br.com.cobranca.banks.sicredi;

import br.com.cobranca.model.BeneficiarioFinal;
import br.com.cobranca.model.Boleto;
import br.com.cobranca.model.BoletoConsulta;
import br.com.cobranca.model.BoletoLiquidado;
import br.com.cobranca.model.BoletoRegistrado;
import br.com.cobranca.model.Desconto;
import br.com.cobranca.model.Endereco;
import br.com.cobranca.model.Pagador;
import br.com.cobranca.model.enums.EspecieDocumento;
import br.com.cobranca.model.enums.SituacaoBoleto;
import br.com.cobranca.model.enums.TipoValor;
import br.com.cobranca.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Converte {@link Boleto} <-> JSON no formato exato exigido pelo Sicredi (item
 * 7.2).
 */
final class SicrediBoletoMapper {

    private final ObjectMapper mapper;

    SicrediBoletoMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    ObjectNode toRequestJson(Boleto boleto) {
        ObjectNode root = mapper.createObjectNode();
        root.put("codigoBeneficiario", boleto.getCodigoBeneficiario());
        root.put("dataVencimento", boleto.getDataVencimento().toString());
        root.put("especieDocumento", toEspecieSicredi(boleto.getEspecieDocumento()));

        ObjectNode pagador = root.putObject("pagador");
        Pagador p = boleto.getPagador();
        pagador.put("tipoPessoa", p.getTipoPessoa().name());
        pagador.put("documento", p.getDocumento());
        pagador.put("nome", p.getNome());
        if (p.getEndereco() != null) {
            Endereco e = p.getEndereco();
            if (e.getCep() != null) {
                pagador.put("cep", e.getCep());
            }
            if (e.getCidade() != null) {
                pagador.put("cidade", e.getCidade());
            }
            if (e.getUf() != null) {
                pagador.put("uf", e.getUf());
            }
            if (e.getLogradouro() != null) {
                pagador.put("endereco", e.getLogradouro());
            }
        }

        if (boleto.getBeneficiarioFinal() != null) {
            BeneficiarioFinal bf = boleto.getBeneficiarioFinal();
            ObjectNode benef = root.putObject("beneficiarioFinal");
            benef.put("tipoPessoa", bf.getTipoPessoa().name());
            benef.put("documento", bf.getDocumento());
            benef.put("nome", bf.getNome());
            if (bf.getEndereco() != null) {
                Endereco e = bf.getEndereco();
                if (e.getCep() != null) {
                    benef.put("cep", e.getCep());
                }
                if (e.getCidade() != null) {
                    benef.put("cidade", e.getCidade());
                }
                if (e.getUf() != null) {
                    benef.put("uf", e.getUf());
                }
                if (e.getLogradouro() != null) {
                    benef.put("logradouro", e.getLogradouro());
                }
                if (e.getNumero() != null) {
                    benef.put("numeroEndereco", e.getNumero());
                }
            }
        }

        root.put("tipoCobranca", boleto.getTipoCobranca().name());
        if (boleto.getNossoNumero() != null && boleto.getNossoNumero() > 0) {
            root.put("nossoNumero", boleto.getNossoNumero());
        }
        if (boleto.getSeuNumero() != null) {
            root.put("seuNumero", boleto.getSeuNumero());
        }
        if (boleto.getIdTituloEmpresa() != null) {
            root.put("idTituloEmpresa", boleto.getIdTituloEmpresa());
        }
        root.put("valor", boleto.getValor().setScale(2, RoundingMode.HALF_UP));

        if (boleto.getTipoDesconto() != null && boleto.getTipoDesconto() != TipoValor.ISENTO) {
            root.put("tipoDesconto", boleto.getTipoDesconto().name());
            List<Desconto> d = boleto.getDescontos();
            for (int i = 0; i < d.size() && i < 3; i++) {
                int n = i + 1;
                if (d.get(i).getValor() != null) {
                    root.put("valorDesconto" + n, d.get(i).getValor().setScale(2, RoundingMode.HALF_UP));
                }
                if (d.get(i).getPercentual() != null) {
                    root.put("percentualDesconto" + n, d.get(i).getPercentual().setScale(2, RoundingMode.HALF_UP));
                }
                root.put("dataDesconto" + n, d.get(i).getData().toString());
            }
        }

        if (boleto.getTipoJuros() != null) {
            root.put("tipoJuros", boleto.getTipoJuros().name());
            if (boleto.getTipoJuros() != TipoValor.ISENTO) {
                if (boleto.getTipoJurosPercentual() != null) {
                    root.put("tipoJurosPercentual", boleto.getTipoJurosPercentual().name());
                }
                root.put("juros", boleto.getJuros().setScale(2, RoundingMode.HALF_UP));
                if (boleto.getDataInicioJuros() != null) {
                    root.put("dataInicioJuros", boleto.getDataInicioJuros().toString());
                }
            }
        }

        if (boleto.getTipoMulta() != null) {
            root.put("tipoMulta", boleto.getTipoMulta().name());
            if (boleto.getTipoMulta() != TipoValor.ISENTO) {
                root.put("multa", boleto.getMulta().setScale(2, RoundingMode.HALF_UP));
                if (boleto.getDataInicioMulta() != null) {
                    root.put("dataInicioMulta", boleto.getDataInicioMulta().toString());
                }
            }
        }

        if (boleto.getValorAbatimento() != null) {
            root.put("valorAbatimento", boleto.getValorAbatimento().setScale(2, RoundingMode.HALF_UP));
        }

        if (!boleto.getInformativos().isEmpty()) {
            var arr = root.putArray("informativos");
            boleto.getInformativos().forEach(arr::add);
        }
        if (!boleto.getMensagens().isEmpty()) {
            var arr = root.putArray("mensagens");
            boleto.getMensagens().forEach(arr::add);
        }

        return root;
    }

    BoletoRegistrado toBoletoRegistrado(JsonNode json) {
        return new BoletoRegistrado(
                textOrNull(json, "linhaDigitavel"),
                textOrNull(json, "codigoBarras"),
                textOrNull(json, "nossoNumero"),
                textOrNull(json, "cooperativa"),
                textOrNull(json, "posto"),
                textOrNull(json, "qrCode"),
                textOrNull(json, "url")
        );
    }

    BoletoConsulta toBoletoConsulta(JsonNode json) {
        return new BoletoConsulta(
                textOrNull(json, "linhaDigitavel"),
                textOrNull(json, "codigoBarras"),
                textOrNull(json, "qrCode"),
                textOrNull(json, "seuNumero"),
                textOrNull(json, "nossoNumero"),
                textOrNull(json, "idTituloEmpresa"),
                json.hasNonNull("valorNominal") ? new BigDecimal(json.get("valorNominal").asText()) : null,
                dateOrNull(json, "dataEmissao"),
                dateOrNull(json, "dataVencimento"),
                mapSituacao(textOrNull(json, "situacao")),
                textOrNull(json.path("pagador"), "documento"),
                textOrNull(json.path("pagador"), "nome")
        );
    }

    BoletoLiquidado toBoletoLiquidado(JsonNode json) {
        return new BoletoLiquidado(
                textOrNull(json, "nossoNumero"),
                textOrNull(json, "seuNumero"),
                dateOrNull(json, "dataPagamento"),
                json.hasNonNull("valor") ? new BigDecimal(json.get("valor").asText()) : null,
                json.hasNonNull("valorLiquidado") ? new BigDecimal(json.get("valorLiquidado").asText()) : null,
                json.hasNonNull("jurosLiquido") ? new BigDecimal(json.get("jurosLiquido").asText()) : null,
                json.hasNonNull("descontoLiquido") ? new BigDecimal(json.get("descontoLiquido").asText()) : null,
                json.hasNonNull("multaLiquida") ? new BigDecimal(json.get("multaLiquida").asText()) : null,
                json.hasNonNull("abatimentoLiquido") ? new BigDecimal(json.get("abatimentoLiquido").asText()) : null,
                textOrNull(json, "tipoLiquidacao")
        );
    }

    SituacaoBoleto mapSituacao(String raw) {
        if (raw == null) {
            return SituacaoBoleto.DESCONHECIDO;
        }
        switch (raw.trim().toUpperCase()) {
            case "EM CARTEIRA":
                return SituacaoBoleto.EM_ABERTO;
            case "EM CARTEIRA PIX":
                return SituacaoBoleto.EM_CARTEIRA_PIX;
            case "VENCIDO":
                return SituacaoBoleto.VENCIDO;
            case "LIQUIDADO":
                return SituacaoBoleto.LIQUIDADO;
            case "LIQUIDADO CARTORIO":
                return SituacaoBoleto.LIQUIDADO_CARTORIO;
            case "LIQUIDADO REDE":
                return SituacaoBoleto.LIQUIDADO_REDE;
            case "LIQUIDADO COMPE":
                return SituacaoBoleto.LIQUIDADO_COMPE;
            case "LIQUIDADO PIX":
                return SituacaoBoleto.LIQUIDADO_PIX;
            case "LIQUIDADO CHEQUE":
                return SituacaoBoleto.LIQUIDADO_CHEQUE;
            case "BAIXADO POR SOLICITACAO":
                return SituacaoBoleto.BAIXADO;
            case "PROTESTADO":
                return SituacaoBoleto.PROTESTADO;
            case "EM CARTORIO":
                return SituacaoBoleto.EM_CARTORIO;
            case "NEGATIVADO":
                return SituacaoBoleto.NEGATIVADO;
            case "AGUARDANDO ENTRADA EM CARTORIO":
                return SituacaoBoleto.AGUARDANDO_ENTRADA_CARTORIO;
            case "AGUARDANDO SUSTACAO DE CARTORIO":
                return SituacaoBoleto.AGUARDANDO_SUSTACAO_CARTORIO;
            case "REJEITADO":
                return SituacaoBoleto.REJEITADO;
            default:
                return SituacaoBoleto.DESCONHECIDO;
        }
    }

    private String toEspecieSicredi(EspecieDocumento especie) {
        // O Sicredi aceita o nome textual da espécie (ex.: DUPLICATA_MERCANTIL_INDICACAO).
        return especie.name();
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static LocalDate dateOrNull(JsonNode node, String field) {
        String v = textOrNull(node, field);
        return v == null ? null : DateUtils.parseToLocalDate(v);
    }
}
