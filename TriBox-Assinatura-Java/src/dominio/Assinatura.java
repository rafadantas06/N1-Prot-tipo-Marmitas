package dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contrato do assinante com um plano. Conduz a montagem do pedido (respeitando o limite do plano),
 * registra os pagamentos e confirma a assinatura.
 */
public class Assinatura {

    public static final String STATUS_EM_CONTRATACAO = "Em contratação";
    public static final String STATUS_ATIVA = "Ativa";

    private String status;
    private LocalDate proximaEntrega;
    private final Plano plano;
    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<Pagamento> pagamentos = new ArrayList<>();

    /** Mensagem «create»(plano) enviada pelo Assinante. */
    public Assinatura(Plano plano) {
        if (plano == null) {
            throw new IllegalArgumentException("O plano é obrigatório.");
        }
        this.plano = plano;
        this.status = STATUS_EM_CONTRATACAO;
    }

    /**
     * Mensagem adicionarItemAoPedido(item, quantidade) enviada pelo controlador.
     * Sequência: [«create» Pedido no primeiro item] → getTipo → contarItens → getLimite → adicionarItem.
     */
    public ItemPedido adicionarItemAoPedido(ItemCardapio item, int quantidade) {
        if (item == null) {
            throw new IllegalArgumentException("O item do cardápio é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        Pedido pedido = obterOuCriarPedido();
        String tipo = item.getTipo();
        int quantidadeAtual = pedido.contarItens(tipo);
        int limite = plano.getLimite(tipo);
        validarLimite(tipo, quantidadeAtual + quantidade, limite);
        return pedido.adicionarItem(item, quantidade);
    }

    /** Mensagem removerItemDoPedido(nomeItem) enviada pelo controlador. */
    public void removerItemDoPedido(String nomeItem) {
        obterPedido().removerItem(nomeItem);
    }

    /**
     * Mensagem alterarQuantidadeDoItem(nomeItem, novaQuantidade) enviada pelo controlador.
     * Sequência: obterTipoDoItem → contarItensAposAlteracao → getLimite → alterarQuantidade.
     */
    public void alterarQuantidadeDoItem(String nomeItem, int novaQuantidade) {
        if (novaQuantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero. Para excluir o item, use a remoção.");
        }
        Pedido pedido = obterPedido();
        String tipo = pedido.obterTipoDoItem(nomeItem);
        int quantidadeResultante = pedido.contarItensAposAlteracao(nomeItem, novaQuantidade);
        int limite = plano.getLimite(tipo);
        validarLimite(tipo, quantidadeResultante, limite);
        pedido.alterarQuantidade(nomeItem, novaQuantidade);
    }

    /** Mensagem obterPedido() enviada pelo controlador: devolve o pedido em andamento. */
    public Pedido obterPedido() {
        if (pedidos.isEmpty()) {
            throw new IllegalStateException("Nenhum pedido em andamento. Selecione itens do cardápio.");
        }
        return pedidos.get(pedidos.size() - 1);
    }

    /** Mensagem definirEnderecoDoPedido(endereco) enviada pelo controlador. */
    public void definirEnderecoDoPedido(EnderecoEntrega endereco) {
        obterPedido().definirEndereco(endereco);
    }

    /** Mensagem calcularValorTotal() enviada pelo controlador e pelo ProcessadorPagamento. */
    public double calcularValorTotal() {
        return plano.getPreco();
    }

    /** Mensagem registrarPagamento(valor, cartao) enviada pelo ProcessadorPagamento; cria («create») o Pagamento. */
    public Pagamento registrarPagamento(double valor, CartaoCredito cartao) {
        obterPedido().validarParaPagamento();
        Pagamento pagamento = new Pagamento(valor, cartao);
        pagamentos.add(pagamento);
        return pagamento;
    }

    /**
     * Mensagem confirmarAssinatura() enviada pelo ProcessadorPagamento após a aprovação.
     * Calcula a primeira entrega, confirma o pedido (que gera o protocolo e a Entrega) e ativa a assinatura.
     */
    public String confirmarAssinatura() {
        if (!possuiPagamentoAprovado()) {
            throw new IllegalStateException("A assinatura só pode ser confirmada após um pagamento aprovado.");
        }
        LocalDateTime previsaoPrimeiraEntrega = calcularPrimeiraEntrega();
        String protocolo = obterPedido().confirmar(previsaoPrimeiraEntrega);
        this.status = STATUS_ATIVA;
        this.proximaEntrega = previsaoPrimeiraEntrega.toLocalDate();
        return protocolo;
    }

    /** Mensagem gerarComprovante() enviada pelo controlador: dados exibidos na confirmação da assinatura. */
    public List<String> gerarComprovante() {
        if (!STATUS_ATIVA.equals(status)) {
            throw new IllegalStateException("A assinatura ainda não foi confirmada.");
        }
        List<String> linhas = new ArrayList<>(obterPedido().gerarComprovante());
        linhas.add(1, "Plano contratado: " + plano.getQuantidadeDeRefeicoes()
                + " refeições (periodicidade: " + plano.getPeriodicidade() + ")");
        return linhas;
    }

    /** Primeira entrega: próxima quarta-feira com ao menos 2 dias de antecedência, a partir das 11h. */
    private LocalDateTime calcularPrimeiraEntrega() {
        LocalDate data = LocalDate.now().plusDays(2);
        while (data.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            data = data.plusDays(1);
        }
        return data.atTime(11, 0);
    }

    private Pedido obterOuCriarPedido() {
        if (pedidos.isEmpty()) {
            pedidos.add(new Pedido());
        }
        return obterPedido();
    }

    private void validarLimite(String tipo, int quantidadeResultante, int limite) {
        if (quantidadeResultante > limite) {
            throw new LimitePlanoExcedidoException(String.format(
                    "Limite do plano excedido para %s: máximo de %d (o pedido ficaria com %d).",
                    tipo, limite, quantidadeResultante));
        }
    }

    private boolean possuiPagamentoAprovado() {
        for (Pagamento pagamento : pagamentos) {
            if (pagamento.isAprovado()) {
                return true;
            }
        }
        return false;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getProximaEntrega() {
        return proximaEntrega;
    }

    public Plano getPlano() {
        return plano;
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }
}
