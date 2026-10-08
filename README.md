# ⚡ Bolix - Boleto Híbrido - Cliente Java

Cliente Java moderno, leve e fluido para integração e processamento de documentos de cobrança (Boleto + Pix / Bolix), projetado para simplificar a comunicação com APIs de pagamento de forma direta.

## ✨ Bancos suportados 
- Caixa Econômica Federal 
- Sicredi

---

## 📦 Instalação

## Maven
<dependency>
    <groupId>com.github.eduardokuhn89</groupId>
    <artifactId>bolix</artifactId>
    <version>1.0.0</version>
</dependency>

## ✨ Exemplo de uso

Endereco enderecoPagador = Endereco.builder()
        .logradouro("Rua Exemplo 123")
        .numero(123)
        .complemento("Sala 1")
        .bairro("Centro")
        .cidade("São Paulo")
        .uf("SP")
        .cep("01001000")
        .build();

Pagador pagador = Pagador.builder()
        .tipoPessoa(TipoPessoa.PESSOA_JURIDICA)
        .documento("12345678000199")
        .nome("Empresa Exemplo LTDA")
        .endereco(enderecoPagador)
        .build();

Boleto.Builder boletoBuilder = Boleto.builder()
        .codigoBeneficiario("123456")
        .tipoCobranca(TipoCobranca.HIBRIDO) 
        .nossoNumero(0L) // 0 = deixa o banco gerar o Nosso Número
        .seuNumero("DOC-0001")
        .dataVencimento(LocalDate.of(2026, 10, 8))
        .dataEmissao(LocalDate.now(ZoneId.of("America/Sao_Paulo")))
        .valor(new BigDecimal("1500.00"))
        .especieDocumento(EspecieDocumento.DUPLICATA_MERCANTIL_INDICACAO)
        .aceite(false)
        .pagador(pagador);

if (new BigDecimal("15.00").compareTo(BigDecimal.ZERO) > 0) {
    boletoBuilder.comJurosValorDiario(new BigDecimal("15.00"), LocalDate.of(2026, 10, 8).plusDays(1));
}

if (new BigDecimal("30.00").compareTo(BigDecimal.ZERO) > 0) {
    boletoBuilder.comMultaValor(new BigDecimal("30.00"), LocalDate.of(2026, 10, 8).plusDays(1));
}

if (new BigDecimal("100.00") != null && LocalDate.of(2026, 10, 1) != null) {
    boletoBuilder.comDescontoValor(new BigDecimal("100.00"), LocalDate.of(2026, 10, 1));
}

Stream.of(
        "Instrução de exemplo 1",
        "Instrução de exemplo 2",
        "Instrução de exemplo 3",
        "Instrução de exemplo 4",
        "Instrução de desconto de exemplo"
).filter(Objects::nonNull).forEach(boletoBuilder::comInformativo);

Boleto boleto = boletoBuilder.build();

//Define qual é o Banco alvo e carrega as credenciais 
BancoCobranca banco = service.getBankAndCredentials(cc);

//Registro do Boleto via API
BoletoRegistrado registrado = banco.boletos().register(boleto);

//Obtenção do boleto via API
byte[] pdfBoleto = banco.boletos().printByLinhaDigitavel(registrado.getLinhaDigitavel());
