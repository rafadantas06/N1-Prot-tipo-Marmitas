package dominio;

/**
 * Lançada quando a seleção de itens ultrapassa o limite previsto no plano
 * (ramo "Limite do plano excedido" do diagrama de sequência).
 */
public class LimitePlanoExcedidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LimitePlanoExcedidoException(String mensagem) {
        super(mensagem);
    }
}
