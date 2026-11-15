package dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Cardápio vigente em um período; agrega os itens que podem ser selecionados. */
public class Cardapio {

    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final List<ItemCardapio> itens = new ArrayList<>();

    public Cardapio(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Período de vigência do cardápio inválido.");
        }
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public void adicionarItem(ItemCardapio item) {
        if (item == null) {
            throw new IllegalArgumentException("O item do cardápio é obrigatório.");
        }
        itens.add(item);
    }

    /** Mensagem listarItens(tipoItem) enviada pelo controlador ao Cardapio. */
    public List<ItemCardapio> listarItens(String tipoItem) {
        List<ItemCardapio> resultado = new ArrayList<>();
        for (ItemCardapio item : itens) {
            if (item.getTipo().equalsIgnoreCase(tipoItem == null ? "" : tipoItem.trim())) {
                resultado.add(item);
            }
        }
        return resultado;
    }

    /** Mensagem buscarItem(nomeItem) enviada pelo controlador ao Cardapio. */
    public ItemCardapio buscarItem(String nomeItem) {
        String procurado = nomeItem == null ? "" : nomeItem.trim();
        for (ItemCardapio item : itens) {
            if (item.getNome().equalsIgnoreCase(procurado)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item não encontrado no cardápio: " + procurado);
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }
}
