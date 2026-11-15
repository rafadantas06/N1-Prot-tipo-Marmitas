package colecoes;

import dominio.Acompanhamento;
import dominio.Cardapio;
import dominio.Plano;
import dominio.PratoPrincipal;
import dominio.PreferenciaAlimentar;
import dominio.Sobremesa;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/** Collection de Plano, PreferenciaAlimentar e Cardapio (dados do protótipo). */
public class Catalogo {

    private final List<Plano> planos = new ArrayList<>();
    private final List<PreferenciaAlimentar> preferencias = new ArrayList<>();
    private final List<Cardapio> cardapios = new ArrayList<>();

    /** Catálogo com os planos, preferências e o cardápio da semana exibidos no protótipo. */
    public static Catalogo padrao() {
        Catalogo catalogo = new Catalogo();

        catalogo.planos.add(new Plano(5, 149.90, "Semanal"));
        catalogo.planos.add(new Plano(10, 269.90, "Semanal"));
        catalogo.planos.add(new Plano(15, 359.90, "Semanal"));

        for (String nome : new String[]{"Tradicional", "Vegetariano", "Vegano", "Sem Lactose",
                "Low Carb", "Sem Glúten", "Fitness"}) {
            catalogo.preferencias.add(new PreferenciaAlimentar(nome));
        }

        LocalDate inicio = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Cardapio semana = new Cardapio(inicio, inicio.plusDays(6));
        semana.adicionarItem(new PratoPrincipal("Frango mediterrâneo", "Ingredientes frescos - Fitness"));
        semana.adicionarItem(new PratoPrincipal("Salmão cítrico", "Ingredientes frescos - Low Carb"));
        semana.adicionarItem(new PratoPrincipal("Carne com legumes", "Ingredientes frescos - Tradicional"));
        semana.adicionarItem(new PratoPrincipal("Risoto integral", "Ingredientes frescos - Sem Lactose"));
        semana.adicionarItem(new Acompanhamento("Arroz integral com ervas", "Porção individual"));
        semana.adicionarItem(new Acompanhamento("Legumes assados", "Porção individual"));
        semana.adicionarItem(new Acompanhamento("Purê de batata-doce", "Porção individual"));
        semana.adicionarItem(new Acompanhamento("Feijão temperado", "Porção individual"));
        semana.adicionarItem(new Sobremesa("Mousse de cacau 70%", "1 porção"));
        semana.adicionarItem(new Sobremesa("Cheesecake de frutas vermelhas", "1 porção"));
        semana.adicionarItem(new Sobremesa("Pudim de chia e manga", "1 porção"));
        catalogo.cardapios.add(semana);

        return catalogo;
    }

    /** Mensagem listarPlanos() enviada pelo controlador. */
    public List<Plano> listarPlanos() {
        return new ArrayList<>(planos);
    }

    /** Mensagem buscarPlano(quantidadeDeRefeicoes) enviada pelo controlador. */
    public Plano buscarPlano(int quantidadeDeRefeicoes) {
        for (Plano plano : planos) {
            if (plano.getQuantidadeDeRefeicoes() == quantidadeDeRefeicoes) {
                return plano;
            }
        }
        throw new IllegalArgumentException("Plano inexistente: " + quantidadeDeRefeicoes + " refeições.");
    }

    /** Mensagem buscarPreferencias(nomes) enviada pelo controlador. */
    public List<PreferenciaAlimentar> buscarPreferencias(List<String> nomes) {
        List<PreferenciaAlimentar> encontradas = new ArrayList<>();
        if (nomes == null) {
            return encontradas;
        }
        for (String nome : nomes) {
            PreferenciaAlimentar encontrada = null;
            for (PreferenciaAlimentar preferencia : preferencias) {
                if (preferencia.getNome().equalsIgnoreCase(nome == null ? "" : nome.trim())) {
                    encontrada = preferencia;
                }
            }
            if (encontrada == null) {
                throw new IllegalArgumentException("Preferência alimentar inexistente: " + nome);
            }
            if (!encontradas.contains(encontrada)) {
                encontradas.add(encontrada);
            }
        }
        return encontradas;
    }

    /** Mensagem obterCardapioVigente() enviada pelo controlador. */
    public Cardapio obterCardapioVigente() {
        LocalDate hoje = LocalDate.now();
        for (Cardapio cardapio : cardapios) {
            if (!hoje.isBefore(cardapio.getDataInicio()) && !hoje.isAfter(cardapio.getDataFim())) {
                return cardapio;
            }
        }
        throw new IllegalStateException("Não há cardápio vigente para a data de hoje.");
    }
}
