package teste;

import colecoes.Assinantes;
import colecoes.Catalogo;
import controle.ControladorContratacao;
import controle.ProcessadorPagamento;
import dominio.Assinante;
import dominio.Assinatura;
import dominio.ItemCardapio;
import dominio.LimitePlanoExcedidoException;
import dominio.OperadoraCartaoCredito;
import dominio.Pedido;
import servicos.ServicoSMS;

import java.util.List;

/**
 * Teste automatizado (sem JUnit) que percorre o cenário do diagrama de sequência diretamente pelo
 * controlador, incluindo os fluxos alternativos (código inválido, limite do plano, pagamento recusado).
 * Execução: java -cp out teste.TesteCenarioPrincipal
 */
public class TesteCenarioPrincipal {

    private static int verificacoes = 0;
    private static int falhas = 0;

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, java.nio.charset.StandardCharsets.UTF_8));
        Assinantes assinantes = new Assinantes();
        ControladorContratacao controlador = new ControladorContratacao(
                assinantes, new ServicoSMS(), Catalogo.padrao(),
                new ProcessadorPagamento(new OperadoraCartaoCredito("Operadora simulada")));
        String celular = "(11) 98765-4321";

        // --- Identificar o assinante
        controlador.solicitarCodigoAcesso(celular);
        Assinante assinante = assinantes.buscarPorCelular(celular);
        String codigoCorreto = assinante.getCodigos().get(0).getCodigo();
        verificar(!controlador.validarCodigoAcesso(celular, "000000".equals(codigoCorreto) ? "111111" : "000000"),
                "código incorreto deve ser recusado");
        verificar(controlador.validarCodigoAcesso(celular, codigoCorreto), "código correto libera o acesso");

        // --- Escolher plano e preferências
        verificar(controlador.consultarPlanosDisponiveis().size() == 3, "catálogo possui 3 planos");
        controlador.escolherPlano(5);
        controlador.definirPreferenciasAlimentares(List.of("Tradicional", "Sem Lactose"));
        verificar(assinante.getPreferencias().size() == 2, "assinante registra 2 preferências");
        verificar(assinante.getAssinaturas().size() == 1, "assinante possui 1 assinatura em contratação");

        // --- Montar o pedido (plano de 5 refeições: 5 pratos, 5 acompanhamentos, 2 sobremesas)
        List<ItemCardapio> pratos = controlador.consultarCardapio(ItemCardapio.TIPO_PRATO_PRINCIPAL);
        verificar(pratos.size() == 4, "cardápio possui 4 pratos principais");
        controlador.selecionarItem("Frango mediterrâneo", 3);
        controlador.selecionarItem("Salmão cítrico", 2);
        verificarLimite(() -> controlador.selecionarItem("Risoto integral", 1), "6º prato excede o limite de 5");

        controlador.consultarCardapio(ItemCardapio.TIPO_ACOMPANHAMENTO);
        controlador.selecionarItem("Arroz integral com ervas", 5);
        verificarLimite(() -> controlador.selecionarItem("Legumes assados", 1), "6º acompanhamento excede o limite");

        controlador.consultarCardapio(ItemCardapio.TIPO_SOBREMESA);
        controlador.selecionarItem("Mousse de cacau 70%", 2);
        verificarLimite(() -> controlador.selecionarItem("Pudim de chia e manga", 1), "3ª sobremesa excede o limite de 2");

        // --- Ajustar o pedido (Tela 6c)
        controlador.removerItem("Mousse de cacau 70%");
        controlador.selecionarItem("Pudim de chia e manga", 1);
        controlador.alterarQuantidadeItem("Pudim de chia e manga", 2);
        verificarLimite(() -> controlador.alterarQuantidadeItem("Pudim de chia e manga", 3), "alterar para 3 sobremesas excede o limite");
        verificarErro(() -> controlador.alterarQuantidadeItem("Pudim de chia e manga", 0), "quantidade zero é inválida");

        // --- Resumo e endereço
        List<String> resumo = controlador.conferirResumoPedido();
        verificar(resumo.size() == 4, "resumo lista 4 itens distintos");
        verificarErro(() -> controlador.cadastrarEnderecoEntrega("123", "Rua das Flores", "128", "", "Vila Nova", "São Paulo"),
                "CEP inválido é recusado");
        controlador.cadastrarEnderecoEntrega("04538-132", "Rua das Flores", "128", "Apto 42", "Vila Nova", "São Paulo");

        // --- Pagamento: primeiro recusado, depois aprovado
        verificar(Math.abs(controlador.consultarValorAssinatura() - 149.90) < 0.001, "valor do plano de 5 refeições é 149,90");
        Assinatura assinatura = assinante.getAssinaturas().get(0);
        verificar(!controlador.efetuarPagamento("4000 0000 0000 0002", "Marina Silva", "12/39", "123"),
                "cartão final 0002 é recusado pela operadora");
        verificarErro(controlador::consultarComprovanteAssinatura, "não há comprovante antes da aprovação");
        verificar(controlador.efetuarPagamento("4242 4242 4242 4242", "Marina Silva", "12/39", "123"),
                "cartão 4242 é aprovado");
        verificar(assinatura.getPagamentos().size() == 2, "duas tentativas de pagamento registradas");
        verificar("Recusado".equals(assinatura.getPagamentos().get(0).getStatus()), "1º pagamento recusado");
        verificar("Aprovado".equals(assinatura.getPagamentos().get(1).getStatus()), "2º pagamento aprovado");
        verificar(Assinatura.STATUS_ATIVA.equals(assinatura.getStatus()), "assinatura ativa");

        Pedido pedido = assinatura.obterPedido();
        verificar(Pedido.STATUS_CONFIRMADO.equals(pedido.getStatus()), "pedido confirmado");
        verificar(pedido.getProtocolo() != null && pedido.getProtocolo().startsWith("NB-"), "protocolo gerado");
        verificar(pedido.getEntrega() != null && assinatura.getProximaEntrega() != null, "entrega criada com previsão");
        verificarErro(() -> controlador.removerItem("Frango mediterrâneo"), "pedido confirmado não pode mais ser alterado");

        System.out.println();
        for (String linha : controlador.consultarComprovanteAssinatura()) {
            System.out.println("   " + linha);
        }
        System.out.println();
        System.out.println(verificacoes + " verificações, " + falhas + " falha(s).");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        if (condicao) {
            System.out.println("[OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHA] " + descricao);
        }
    }

    private static void verificarLimite(Runnable acao, String descricao) {
        try {
            acao.run();
            verificar(false, descricao);
        } catch (LimitePlanoExcedidoException e) {
            verificar(true, descricao);
        }
    }

    private static void verificarErro(Runnable acao, String descricao) {
        try {
            acao.run();
            verificar(false, descricao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            verificar(true, descricao);
        }
    }
}
