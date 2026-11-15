package dominio;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/** Pedido com as refeições escolhidas (compõe ItemPedido), o endereço de entrega e a entrega gerada. */
public class Pedido {

    public static final String STATUS_AGUARDANDO_PAGAMENTO = "Aguardando Pagamento";
    public static final String STATUS_CONFIRMADO = "Confirmado";

    private static final AtomicInteger SEQUENCIAL = new AtomicInteger(1000);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private String protocolo;
    private final LocalDateTime dataCriacao;
    private String status;
    private final List<ItemPedido> itens = new ArrayList<>();
    private EnderecoEntrega endereco;
    private Entrega entrega;

    /** Mensagem «create» enviada pela Assinatura (primeiro item selecionado). */
    public Pedido() {
        this.dataCriacao = LocalDateTime.now();
        this.status = STATUS_AGUARDANDO_PAGAMENTO;
    }

    /**
     * Mensagem adicionarItem(item, quantidade) enviada pela Assinatura.
     * Cria («create») o ItemPedido; se o item já estiver no pedido, soma a quantidade.
     */
    public ItemPedido adicionarItem(ItemCardapio item, int quantidade) {
        exigirEditavel();
        if (item == null) {
            throw new IllegalArgumentException("O item do cardápio é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        ItemPedido existente = procurarItemPedido(item.getNome());
        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
            return existente;
        }
        ItemPedido novo = new ItemPedido(item, quantidade);
        itens.add(novo);
        return novo;
    }

    /** Mensagem removerItem(nomeItem) enviada pela Assinatura. */
    public void removerItem(String nomeItem) {
        exigirEditavel();
        itens.remove(buscarItemPedido(nomeItem));
    }

    /** Mensagem alterarQuantidade(nomeItem, novaQuantidade) enviada pela Assinatura; envia setQuantidade ao ItemPedido. */
    public void alterarQuantidade(String nomeItem, int novaQuantidade) {
        exigirEditavel();
        buscarItemPedido(nomeItem).setQuantidade(novaQuantidade);
    }

    /** Mensagem contarItens(tipo) enviada pela Assinatura: soma das quantidades dos itens do tipo. */
    public int contarItens(String tipo) {
        int total = 0;
        for (ItemPedido itemPedido : itens) {
            if (itemPedido.getItem().getTipo().equals(tipo)) {
                total += itemPedido.getQuantidade();
            }
        }
        return total;
    }

    /** Mensagem obterTipoDoItem(nomeItem) enviada pela Assinatura (ajuste de quantidade). */
    public String obterTipoDoItem(String nomeItem) {
        return buscarItemPedido(nomeItem).getItem().getTipo();
    }

    /** Mensagem contarItensAposAlteracao(nomeItem, novaQuantidade) enviada pela Assinatura. */
    public int contarItensAposAlteracao(String nomeItem, int novaQuantidade) {
        ItemPedido itemPedido = buscarItemPedido(nomeItem);
        String tipo = itemPedido.getItem().getTipo();
        return contarItens(tipo) - itemPedido.getQuantidade() + novaQuantidade;
    }

    /** Mensagem descreverItens() enviada pelo controlador; envia descrever() a cada ItemPedido. */
    public List<String> descreverItens() {
        if (itens.isEmpty()) {
            throw new IllegalStateException("O pedido está vazio. Selecione ao menos um item do cardápio.");
        }
        List<String> descricoes = new ArrayList<>();
        for (ItemPedido itemPedido : itens) {
            descricoes.add(itemPedido.descrever());
        }
        return descricoes;
    }

    /** Mensagem definirEndereco(endereco) enviada pela Assinatura. */
    public void definirEndereco(EnderecoEntrega novoEndereco) {
        exigirEditavel();
        if (novoEndereco == null) {
            throw new IllegalArgumentException("O endereço de entrega é obrigatório.");
        }
        this.endereco = novoEndereco;
    }

    /** Mensagem validarParaPagamento() enviada pela Assinatura antes de registrar o pagamento. */
    public void validarParaPagamento() {
        exigirEditavel();
        if (itens.isEmpty()) {
            throw new IllegalStateException("O pedido está vazio. Selecione ao menos um item do cardápio.");
        }
        if (endereco == null) {
            throw new IllegalStateException("Informe o endereço de entrega antes de efetuar o pagamento.");
        }
    }

    /**
     * Mensagem confirmar(previsaoPrimeiraEntrega) enviada pela Assinatura.
     * Gera o protocolo e cria («create») a Entrega com a previsão da primeira entrega.
     */
    public String confirmar(LocalDateTime previsaoPrimeiraEntrega) {
        validarParaPagamento();
        this.protocolo = gerarProtocolo();
        this.status = STATUS_CONFIRMADO;
        this.entrega = new Entrega(previsaoPrimeiraEntrega);
        return protocolo;
    }

    /**
     * Mensagem gerarComprovante() enviada pela Assinatura.
     * Consulta descreverItens(), EnderecoEntrega.descrever() e Entrega.getPrevisaoHorario().
     */
    public List<String> gerarComprovante() {
        if (!STATUS_CONFIRMADO.equals(status)) {
            throw new IllegalStateException("O pedido ainda não foi confirmado.");
        }
        List<String> linhas = new ArrayList<>();
        linhas.add("Protocolo: " + protocolo);
        linhas.add("Refeições selecionadas:");
        for (String descricao : descreverItens()) {
            linhas.add("  - " + descricao);
        }
        linhas.add("Endereço de entrega: " + endereco.descrever());
        LocalDateTime previsao = entrega.getPrevisaoHorario();
        String dia = previsao.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", PT_BR));
        linhas.add("Previsão da primeira entrega: " + dia + ", entre "
                + previsao.getHour() + "h e " + (previsao.getHour() + 2) + "h");
        return linhas;
    }

    private String gerarProtocolo() {
        return String.format("NB-%d-%05d", Year.now().getValue(), SEQUENCIAL.incrementAndGet());
    }

    private ItemPedido buscarItemPedido(String nomeItem) {
        ItemPedido encontrado = procurarItemPedido(nomeItem);
        if (encontrado == null) {
            throw new IllegalArgumentException("O item \"" + nomeItem + "\" não está no pedido.");
        }
        return encontrado;
    }

    private ItemPedido procurarItemPedido(String nomeItem) {
        String procurado = nomeItem == null ? "" : nomeItem.trim();
        for (ItemPedido itemPedido : itens) {
            if (itemPedido.getItem().getNome().equalsIgnoreCase(procurado)) {
                return itemPedido;
            }
        }
        return null;
    }

    private void exigirEditavel() {
        if (!STATUS_AGUARDANDO_PAGAMENTO.equals(status)) {
            throw new IllegalStateException("O pedido já foi confirmado e não pode mais ser alterado.");
        }
    }

    public String getProtocolo() {
        return protocolo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public EnderecoEntrega getEndereco() {
        return endereco;
    }

    public Entrega getEntrega() {
        return entrega;
    }
}
