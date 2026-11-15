package dominio;

/** Cartão de crédito utilizado em um pagamento. O CVV não é armazenado. */
public class CartaoCredito {

    private final String numeroMascarado;
    private final String nomeImpresso;
    private final String validade;
    private final String numeroCartao;

    /** Mensagem «create»(numeroCartao, nomeImpresso, validade) enviada pelo ProcessadorPagamento. */
    public CartaoCredito(String numeroCartao, String nomeImpresso, String validade) {
        String digitos = numeroCartao == null ? "" : numeroCartao.replaceAll("[\\s-]", "");
        if (!digitos.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Número do cartão inválido. Informe de 13 a 19 dígitos.");
        }
        if (nomeImpresso == null || nomeImpresso.isBlank()) {
            throw new IllegalArgumentException("O nome impresso no cartão é obrigatório.");
        }
        if (validade == null || !validade.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Validade inválida. Use o formato MM/AA.");
        }
        this.numeroCartao = digitos;
        this.numeroMascarado = "**** **** **** " + digitos.substring(digitos.length() - 4);
        this.nomeImpresso = nomeImpresso.trim().toUpperCase();
        this.validade = validade.trim();
    }

    public String getNumeroMascarado() {
        return numeroMascarado;
    }

    public String getNomeImpresso() {
        return nomeImpresso;
    }

    public String getValidade() {
        return validade;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }
}
