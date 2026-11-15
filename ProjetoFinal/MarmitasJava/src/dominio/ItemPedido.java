package dominio;

/** Itens de um pedido: associa um ItemCardapio à quantidade escolhida (composição de Pedido). */
public class ItemPedido {

    private final ItemCardapio item;
    private int quantidade;

    /** Mensagem «create»(item, quantidade) enviada pelo Pedido. */
    public ItemPedido(ItemCardapio item, int quantidade) {
        if (item == null) {
            throw new IllegalArgumentException("O item do cardápio é obrigatório.");
        }
        validarQuantidade(quantidade);
        this.item = item;
        this.quantidade = quantidade;
    }

    /** Mensagem descrever() enviada pelo Pedido; consulta getNome() e getTipo() no ItemCardapio. */
    public String descrever() {
        return quantidade + "x " + item.getNome() + " (" + item.getTipo() + ")";
    }

    /** Mensagem setQuantidade(novaQuantidade) enviada pelo Pedido. */
    public void setQuantidade(int quantidade) {
        validarQuantidade(quantidade);
        this.quantidade = quantidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public ItemCardapio getItem() {
        return item;
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
