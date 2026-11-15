package dominio;

/**
 * Item que pode compor um pedido (classe abstrata do diagrama de classes).
 * Especializações: PratoPrincipal, Acompanhamento e Sobremesa.
 */
public abstract class ItemCardapio {

    public static final String TIPO_PRATO_PRINCIPAL = "Prato Principal";
    public static final String TIPO_ACOMPANHAMENTO = "Acompanhamento";
    public static final String TIPO_SOBREMESA = "Sobremesa";

    private final String nome;
    private final String descricao;

    protected ItemCardapio(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do item do cardápio é obrigatório.");
        }
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    /** Mensagem getTipo() enviada pela Assinatura ao ItemCardapio. */
    public abstract String getTipo();

    /** Mensagem getNome() enviada pelo ItemPedido ao ItemCardapio. */
    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
