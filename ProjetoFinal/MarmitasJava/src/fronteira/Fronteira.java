package fronteira;

import controle.ControladorContratacao;
import dominio.ItemCardapio;
import dominio.LimitePlanoExcedidoException;
import dominio.Plano;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Objeto de fronteira (interface de console). Cada método público corresponde a um estímulo do
 * Assinante (ator) no diagrama de sequência e delega a operação ao ControladorContratacao.
 */
public class Fronteira {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final List<String> OPCOES_PREFERENCIAS = List.of(
            "Tradicional", "Vegetariano", "Vegano", "Sem Lactose", "Low Carb", "Sem Glúten", "Fitness");
    private static final List<String> TIPOS_DE_ITEM = List.of(
            ItemCardapio.TIPO_PRATO_PRINCIPAL, ItemCardapio.TIPO_ACOMPANHAMENTO, ItemCardapio.TIPO_SOBREMESA);

    private final ControladorContratacao controlador;
    private final Scanner entrada;
    private String numeroCelular;
    private List<ItemCardapio> itensListados = new ArrayList<>();

    public Fronteira(ControladorContratacao controlador, InputStream origemDeEntrada) {
        this.controlador = controlador;
        this.entrada = new Scanner(origemDeEntrada, StandardCharsets.UTF_8);
    }

    /** Executa o cenário principal do caso de uso, na ordem das telas do protótipo. */
    public void iniciar() {
        try {
            exibirBoasVindas();

            // Telas 1 e 2 - identificar o assinante
            boolean acessoLiberado = false;
            while (!acessoLiberado) {
                solicitarCodigoDeAcesso();
                acessoLiberado = validarCodigoDeAcesso();
                while (!acessoLiberado && !perguntar("Reenviar o código por SMS? (s = reenviar / n = digitar novamente)")) {
                    acessoLiberado = validarCodigoDeAcesso();
                }
            }

            // Telas 3 e 4 - plano e preferências
            consultarPlanosDisponiveis();
            escolherPlano();
            definirPreferenciasAlimentares();

            // Telas 5, 6, 6b e 6c - pedido
            montarPedido();
            boolean pedidoConfirmado = false;
            while (!pedidoConfirmado) {
                if (perguntar("Deseja ajustar o pedido (remover item ou alterar quantidade)? (s/n)")) {
                    ajustarPedido();
                }
                if (!conferirResumoDoPedido()) {
                    montarPedido();
                    continue;
                }
                pedidoConfirmado = perguntar("Confirmar o pedido e informar o endereço de entrega? (s = seguir / n = editar o pedido)");
            }

            // Tela 7 - endereço de entrega
            cadastrarEnderecoDeEntrega();

            // Telas 8 a 11 - pagamento e confirmação
            consultarValorDaAssinatura();
            boolean pagamentoAprovado = false;
            while (!pagamentoAprovado) {
                pagamentoAprovado = efetuarPagamentoDaAssinatura();
                if (!pagamentoAprovado && !perguntar("Deseja tentar novamente ou trocar a forma de pagamento? (s/n)")) {
                    System.out.println("\nContratação não concluída. Até logo!");
                    return;
                }
            }
            System.out.println("\nObrigado por assinar!");
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada encerrada. Fluxo interrompido.");
        }
    }

    // ------------------------------------------------------------------ Telas 1 e 2

    /** Estímulo: Solicitar código de acesso. */
    public void solicitarCodigoDeAcesso() {
        boolean enviado = false;
        while (!enviado) {
            System.out.print("Informe seu número de celular com DDD: ");
            String celular = lerLinha();
            try {
                controlador.solicitarCodigoAcesso(celular);
                this.numeroCelular = celular;
                enviado = true;
                System.out.println("Código de acesso enviado por SMS.");
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    /** Estímulo: Validar código de acesso. Devolve true quando o acesso é liberado. */
    public boolean validarCodigoDeAcesso() {
        System.out.print("Informe o código recebido por SMS: ");
        String codigo = lerLinha();
        try {
            boolean liberado = controlador.validarCodigoAcesso(numeroCelular, codigo);
            System.out.println(liberado ? "Acesso liberado." : "Código inválido ou expirado.");
            return liberado;
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------ Tela 3

    /** Estímulo: Consultar planos disponíveis. */
    public void consultarPlanosDisponiveis() {
        titulo("Tela 3 - Escolha seu plano");
        List<Plano> planos = controlador.consultarPlanosDisponiveis();
        for (Plano plano : planos) {
            System.out.printf(PT_BR, "  %d refeições - %s (%s) - %s por marmita%n",
                    plano.getQuantidadeDeRefeicoes(), moeda(plano.getPreco()), plano.getPeriodicidade(),
                    moeda(plano.getPreco() / plano.getQuantidadeDeRefeicoes()));
        }
    }

    /** Estímulo: Escolher plano. */
    public void escolherPlano() {
        boolean escolhido = false;
        while (!escolhido) {
            int quantidade = lerInteiro("Digite a quantidade de refeições do plano desejado: ");
            try {
                controlador.escolherPlano(quantidade);
                escolhido = true;
                System.out.println("Plano de " + quantidade + " refeições selecionado.");
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ Tela 4

    /** Estímulo: Definir preferências alimentares. */
    public void definirPreferenciasAlimentares() {
        titulo("Tela 4 - Suas preferências");
        for (int i = 0; i < OPCOES_PREFERENCIAS.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + OPCOES_PREFERENCIAS.get(i));
        }
        boolean registrado = false;
        while (!registrado) {
            System.out.print("Números das preferências separados por vírgula (ENTER para nenhuma): ");
            String linha = lerLinha();
            try {
                List<String> nomes = new ArrayList<>();
                if (!linha.isBlank()) {
                    for (String parte : linha.split(",")) {
                        int posicao = Integer.parseInt(parte.trim());
                        if (posicao < 1 || posicao > OPCOES_PREFERENCIAS.size()) {
                            throw new IllegalArgumentException("Opção inexistente: " + posicao);
                        }
                        nomes.add(OPCOES_PREFERENCIAS.get(posicao - 1));
                    }
                }
                controlador.definirPreferenciasAlimentares(nomes);
                registrado = true;
                System.out.println("Preferências registradas: " + (nomes.isEmpty() ? "nenhuma" : String.join(", ", nomes)));
            } catch (NumberFormatException e) {
                System.out.println("Erro: informe apenas números separados por vírgula.");
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ Telas 5, 6, 6b

    private void montarPedido() {
        for (String tipo : TIPOS_DE_ITEM) {
            consultarCardapioPorTipoDeItem(tipo);
            while (selecionarItemDoCardapio()) {
                // continua selecionando itens desta etapa
            }
        }
    }

    /** Estímulo: Consultar cardápio por tipo de item. */
    public void consultarCardapioPorTipoDeItem(String tipoItem) {
        titulo("Cardápio - " + tipoItem);
        try {
            itensListados = controlador.consultarCardapio(tipoItem);
            for (int i = 0; i < itensListados.size(); i++) {
                ItemCardapio item = itensListados.get(i);
                System.out.println("  " + (i + 1) + ") " + item.getNome() + " - " + item.getDescricao());
            }
        } catch (RuntimeException e) {
            itensListados = new ArrayList<>();
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Estímulo: Selecionar item do cardápio. Devolve false quando o assinante opta por avançar. */
    public boolean selecionarItemDoCardapio() {
        System.out.print("Número do item para adicionar (ENTER para avançar): ");
        String linha = lerLinha();
        if (linha.isBlank()) {
            return false;
        }
        try {
            int posicao = Integer.parseInt(linha.trim());
            if (posicao < 1 || posicao > itensListados.size()) {
                throw new IllegalArgumentException("Opção inexistente: " + posicao);
            }
            ItemCardapio item = itensListados.get(posicao - 1);
            int quantidade = lerInteiro("Quantidade: ");
            controlador.selecionarItem(item.getNome(), quantidade);
            System.out.println("Item adicionado ao pedido: " + quantidade + "x " + item.getNome());
        } catch (NumberFormatException e) {
            System.out.println("Erro: informe o número do item.");
        } catch (LimitePlanoExcedidoException e) {
            System.out.println("Limite atingido: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return true;
    }

    // ------------------------------------------------------------------ Tela 6c

    private void ajustarPedido() {
        titulo("Tela 6c - Editar seu pedido");
        boolean concluido = false;
        while (!concluido) {
            try {
                for (String descricao : controlador.conferirResumoPedido()) {
                    System.out.println("  - " + descricao);
                }
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
            System.out.print("1 = remover item | 2 = alterar quantidade | 0 = concluir ajustes: ");
            String opcao = lerLinha().trim();
            if (opcao.equals("1")) {
                removerItemDoPedido();
            } else if (opcao.equals("2")) {
                alterarQuantidadeDeItem();
            } else if (opcao.equals("0")) {
                concluido = true;
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    /** Estímulo: Remover item do pedido. */
    public void removerItemDoPedido() {
        System.out.print("Nome do item a remover: ");
        String nomeItem = lerLinha();
        try {
            controlador.removerItem(nomeItem);
            System.out.println("Item removido do pedido.");
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Estímulo: Alterar quantidade de item. */
    public void alterarQuantidadeDeItem() {
        System.out.print("Nome do item: ");
        String nomeItem = lerLinha();
        int novaQuantidade = lerInteiro("Nova quantidade: ");
        try {
            controlador.alterarQuantidadeItem(nomeItem, novaQuantidade);
            System.out.println("Quantidade atualizada.");
        } catch (LimitePlanoExcedidoException e) {
            System.out.println("Limite atingido: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ Tela 7

    /** Estímulo: Conferir resumo do pedido. Devolve false quando o pedido está vazio. */
    public boolean conferirResumoDoPedido() {
        titulo("Tela 7 - Seu pedido");
        try {
            for (String descricao : controlador.conferirResumoPedido()) {
                System.out.println("  - " + descricao);
            }
            return true;
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    /** Estímulo: Cadastrar endereço de entrega. */
    public void cadastrarEnderecoDeEntrega() {
        titulo("Tela 7 - Endereço de entrega");
        boolean cadastrado = false;
        while (!cadastrado) {
            System.out.print("CEP: ");
            String cep = lerLinha();
            System.out.print("Rua: ");
            String rua = lerLinha();
            System.out.print("Número: ");
            String numero = lerLinha();
            System.out.print("Complemento (opcional): ");
            String complemento = lerLinha();
            System.out.print("Bairro: ");
            String bairro = lerLinha();
            System.out.print("Cidade: ");
            String cidade = lerLinha();
            try {
                controlador.cadastrarEnderecoEntrega(cep, rua, numero, complemento, bairro, cidade);
                cadastrado = true;
                System.out.println("Endereço de entrega registrado.");
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ Telas 8 a 11

    /** Estímulo: Consultar valor da assinatura. */
    public void consultarValorDaAssinatura() {
        titulo("Tela 8 - Pagamento");
        try {
            System.out.println("Total da assinatura: " + moeda(controlador.consultarValorAssinatura())
                    + " (Aguardando Pagamento)");
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * Estímulo: Efetuar pagamento da assinatura. Exibe o processamento (Tela 9) e, conforme o resultado,
     * o comprovante (Tela 10) ou a recusa (Tela 11). Devolve true se o pagamento foi aprovado.
     */
    public boolean efetuarPagamentoDaAssinatura() {
        System.out.print("Número do cartão: ");
        String numeroCartao = lerLinha();
        System.out.print("Nome impresso no cartão: ");
        String nomeImpresso = lerLinha();
        System.out.print("Validade (MM/AA): ");
        String validade = lerLinha();
        System.out.print("CVV: ");
        String cvv = lerLinha();

        exibirProcessamento();
        try {
            boolean aprovado = controlador.efetuarPagamento(numeroCartao, nomeImpresso, validade, cvv);
            if (aprovado) {
                titulo("Tela 10 - Pedido confirmado!");
                for (String linha : controlador.consultarComprovanteAssinatura()) {
                    System.out.println(linha);
                }
                return true;
            }
            titulo("Tela 11 - Pagamento não aprovado");
            System.out.println("Não foi possível concluir a cobrança. Revise os dados ou use outro cartão.");
            return false;
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
            return false;
        }
    }

    /** Mensagem exibirProcessamento() da Fronteira para si mesma (Tela 9). */
    public void exibirProcessamento() {
        titulo("Tela 9 - Processando");
        System.out.println("Validando pagamento com a operadora...");
    }

    // ------------------------------------------------------------------ apoio

    private void exibirBoasVindas() {
        titulo("Tela 1 - Minha Refeição");
        System.out.println("Alimentação saudável na sua porta. Marmitas frescas, do seu jeito, entregues toda semana.");
        titulo("Tela 2 - Login por SMS");
    }

    private void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    private String moeda(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private String lerLinha() {
        return entrada.nextLine();
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String linha = lerLinha().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Erro: informe um número inteiro.");
            }
        }
    }

    private boolean perguntar(String pergunta) {
        while (true) {
            System.out.print(pergunta + " ");
            String resposta = lerLinha().trim().toLowerCase(PT_BR);
            if (resposta.equals("s") || resposta.equals("sim")) {
                return true;
            }
            if (resposta.equals("n") || resposta.equals("nao") || resposta.equals("não")) {
                return false;
            }
            System.out.println("Responda com s ou n.");
        }
    }
}
