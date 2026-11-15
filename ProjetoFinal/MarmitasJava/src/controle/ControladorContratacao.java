package controle;

import colecoes.Assinantes;
import colecoes.Catalogo;
import dominio.Assinante;
import dominio.Assinatura;
import dominio.Cardapio;
import dominio.EnderecoEntrega;
import dominio.ItemCardapio;
import dominio.Pedido;
import dominio.Plano;
import dominio.PreferenciaAlimentar;
import servicos.ServicoSMS;
import servicos.PersistenciaCsv;

import java.io.IOException;

import java.util.List;

/**
 * Controlador do caso de uso "Contratar Assinatura": recebe os estímulos da Fronteira e
 * distribui o trabalho entre as collections, o processador e os objetos de domínio,
 * exatamente como no diagrama de sequência.
 */
public class ControladorContratacao {

    private final Assinantes assinantes;
    private final ServicoSMS servicoSMS;
    private final Catalogo catalogo;
    private final ProcessadorPagamento processador;
    private final PersistenciaCsv persistencia;
    private Assinante assinante;
    private Assinatura assinatura;
    private Cardapio cardapio;

    public ControladorContratacao(Assinantes assinantes, ServicoSMS servicoSMS, Catalogo catalogo,
                                  ProcessadorPagamento processador) {
        this.assinantes = assinantes;
        this.servicoSMS = servicoSMS;
        this.catalogo = catalogo;
        this.processador = processador;
        this.persistencia = new PersistenciaCsv();
    }

    // ---- Identificar o assinante (Telas 1 e 2) ----

    /** Estímulo: Solicitar código de acesso. */
    public void solicitarCodigoAcesso(String numeroCelular) {
        Assinante encontrado = assinantes.obterOuCadastrar(numeroCelular);
        String codigo = encontrado.gerarCodigoAcesso();
        servicoSMS.enviarSMS(encontrado.getNumeroCelular(), codigo);
    }

    /** Estímulo: Validar código de acesso. Devolve true quando o acesso é liberado. */
    public boolean validarCodigoAcesso(String numeroCelular, String codigo) {
        Assinante encontrado = assinantes.buscarPorCelular(numeroCelular);
        boolean valido = encontrado.validarCodigo(codigo);
        if (valido) {
            this.assinante = encontrado;
        }
        return valido;
    }

    // ---- Escolher o plano (Tela 3) ----

    /** Estímulo: Consultar planos disponíveis. */
    public List<Plano> consultarPlanosDisponiveis() {
        exigirAssinante();
        return catalogo.listarPlanos();
    }

    /** Estímulo: Escolher plano. */
    public void escolherPlano(int quantidadeDeRefeicoes) {
        exigirAssinante();
        Plano plano = catalogo.buscarPlano(quantidadeDeRefeicoes);
        this.assinatura = assinante.contratarPlano(plano);
    }

    // ---- Definir preferências alimentares (Tela 4) ----

    /** Estímulo: Definir preferências alimentares. */
    public void definirPreferenciasAlimentares(List<String> nomes) {
        exigirAssinante();
        List<PreferenciaAlimentar> preferencias = catalogo.buscarPreferencias(nomes);
        assinante.definirPreferencias(preferencias);
    }

    // ---- Montar o pedido (Telas 5, 6, 6b e 6c) ----

    /** Estímulo: Consultar cardápio por tipo de item. */
    public List<ItemCardapio> consultarCardapio(String tipoItem) {
        exigirAssinatura();
        this.cardapio = catalogo.obterCardapioVigente();
        return cardapio.listarItens(tipoItem);
    }

    /** Estímulo: Selecionar item do cardápio. */
    public void selecionarItem(String nomeItem, int quantidade) {
        exigirAssinatura();
        exigirCardapio();
        ItemCardapio item = cardapio.buscarItem(nomeItem);
        assinatura.adicionarItemAoPedido(item, quantidade);
    }

    /** Estímulo: Remover item do pedido. */
    public void removerItem(String nomeItem) {
        exigirAssinatura();
        assinatura.removerItemDoPedido(nomeItem);
    }

    /** Estímulo: Alterar quantidade de item. */
    public void alterarQuantidadeItem(String nomeItem, int novaQuantidade) {
        exigirAssinatura();
        assinatura.alterarQuantidadeDoItem(nomeItem, novaQuantidade);
    }

    // ---- Conferir o pedido e cadastrar a entrega (Tela 7) ----

    /** Estímulo: Conferir resumo do pedido. Devolve a descrição de cada item do pedido. */
    public List<String> conferirResumoPedido() {
        exigirAssinatura();
        Pedido pedido = assinatura.obterPedido();
        return pedido.descreverItens();
    }

    /** Estímulo: Cadastrar endereço de entrega. */
    public void cadastrarEnderecoEntrega(String cep, String rua, String numero,
                                         String complemento, String bairro, String cidade) {
        exigirAssinatura();
        EnderecoEntrega endereco = assinante.cadastrarEndereco(cep, rua, numero, complemento, bairro, cidade);
        assinatura.definirEnderecoDoPedido(endereco);
    }

    // ---- Pagar e confirmar a assinatura (Telas 8 a 11) ----

    /** Estímulo: Consultar valor da assinatura. */
    public double consultarValorAssinatura() {
        exigirAssinatura();
        return assinatura.calcularValorTotal();
    }

    /** Estímulo: Efetuar pagamento da assinatura. Devolve true se o pagamento foi aprovado. */
    public boolean efetuarPagamento(String numeroCartao, String nomeImpresso, String validade, String cvv) {
        exigirAssinatura();
        boolean aprovado = processador.processarPagamento(assinatura, numeroCartao, nomeImpresso, validade, cvv);
        if (aprovado) {
            try {
                persistencia.salvar(assinante, assinatura);
            } catch (IOException e) {
                throw new IllegalStateException("Pagamento aprovado, mas não foi possível salvar a assinatura em dados/assinaturas.csv: " + e.getMessage(), e);
            }
        }
        return aprovado;
    }

    /** Mensagem enviada pela Fronteira após a aprovação (Tela 10). */
    public List<String> consultarComprovanteAssinatura() {
        exigirAssinatura();
        return assinatura.gerarComprovante();
    }

    private void exigirAssinante() {
        if (assinante == null) {
            throw new IllegalStateException("Acesso não liberado. Valide o código de acesso primeiro.");
        }
    }

    private void exigirAssinatura() {
        exigirAssinante();
        if (assinatura == null) {
            throw new IllegalStateException("Nenhum plano escolhido. Escolha um plano primeiro.");
        }
    }

    private void exigirCardapio() {
        if (cardapio == null) {
            throw new IllegalStateException("Consulte o cardápio antes de selecionar itens.");
        }
    }
}
