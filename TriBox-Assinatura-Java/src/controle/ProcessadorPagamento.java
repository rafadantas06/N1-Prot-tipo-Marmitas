package controle;

import dominio.Assinatura;
import dominio.CartaoCredito;
import dominio.OperadoraCartaoCredito;
import dominio.Pagamento;
import dominio.RespostaAutorizacao;

/** Processador: orquestra a cobrança da assinatura junto à operadora do cartão. */
public class ProcessadorPagamento {

    private final OperadoraCartaoCredito operadora;

    public ProcessadorPagamento(OperadoraCartaoCredito operadora) {
        if (operadora == null) {
            throw new IllegalArgumentException("A operadora do cartão é obrigatória.");
        }
        this.operadora = operadora;
    }

    /**
     * Mensagem processarPagamento(assinatura, numeroCartao, nomeImpresso, validade, cvv) enviada pelo controlador.
     * Devolve true quando o pagamento foi aprovado (e a assinatura confirmada) e false quando foi recusado.
     */
    public boolean processarPagamento(Assinatura assinatura, String numeroCartao, String nomeImpresso,
                                      String validade, String cvv) {
        double valor = assinatura.calcularValorTotal();
        CartaoCredito cartao = new CartaoCredito(numeroCartao, nomeImpresso, validade);
        Pagamento pagamento = assinatura.registrarPagamento(valor, cartao);

        RespostaAutorizacao resposta = operadora.autorizar(pagamento, numeroCartao, validade, cvv);
        if (resposta.isAprovada()) {
            pagamento.aprovar(resposta.getCodigoAutorizacao(), resposta.getDataHora());
            assinatura.confirmarAssinatura();
            return true;
        }
        pagamento.recusar();
        return false;
    }
}
