package dominio;

/** Plano de assinatura (item de catálogo): 5, 10 ou 15 refeições por semana. */
public class Plano {

    private final int quantidadeDeRefeicoes;
    private final double preco;
    private final String periodicidade;

    public Plano(int quantidadeDeRefeicoes, double preco, String periodicidade) {
        if (quantidadeDeRefeicoes <= 0) {
            throw new IllegalArgumentException("A quantidade de refeições deve ser maior que zero.");
        }
        if (preco <= 0) {
            throw new IllegalArgumentException("O preço do plano deve ser maior que zero.");
        }
        if (periodicidade == null || periodicidade.isBlank()) {
            throw new IllegalArgumentException("A periodicidade do plano é obrigatória.");
        }
        this.quantidadeDeRefeicoes = quantidadeDeRefeicoes;
        this.preco = preco;
        this.periodicidade = periodicidade.trim();
    }

    /** Mensagem getPreco() enviada pela Assinatura ao Plano. */
    public double getPreco() {
        return preco;
    }

    /**
     * Mensagem getLimite(tipo) enviada pela Assinatura ao Plano.
     * Regra derivada do protótipo: pratos principais e acompanhamentos limitados à
     * quantidade de refeições do plano; sobremesas limitadas a 2 para cada 5 refeições
     * (no plano de 5 refeições o protótipo mostra "Sobremesas: 1 de 2").
     */
    public int getLimite(String tipo) {
        if (ItemCardapio.TIPO_PRATO_PRINCIPAL.equals(tipo) || ItemCardapio.TIPO_ACOMPANHAMENTO.equals(tipo)) {
            return quantidadeDeRefeicoes;
        }
        if (ItemCardapio.TIPO_SOBREMESA.equals(tipo)) {
            return quantidadeDeRefeicoes * 2 / 5;
        }
        return 0;
    }

    public int getQuantidadeDeRefeicoes() {
        return quantidadeDeRefeicoes;
    }

    public String getPeriodicidade() {
        return periodicidade;
    }
}
