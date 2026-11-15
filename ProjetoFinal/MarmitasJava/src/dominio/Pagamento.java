package dominio;

import java.time.LocalDateTime;

/** Tentativa de cobrança da assinatura (aprovada ou recusada pela operadora). */
public class Pagamento {

    public static final String STATUS_PENDENTE = "Pendente";
    public static final String STATUS_APROVADO = "Aprovado";
    public static final String STATUS_RECUSADO = "Recusado";

    private final double valor;
    private String status;
    private final LocalDateTime dataPagamento;
    private String codigoAutorizacao;
    private LocalDateTime dataHoraAutorizacao;
    private final CartaoCredito cartao;

    /** Mensagem «create»(valor, cartao) enviada pela Assinatura. */
    public Pagamento(double valor, CartaoCredito cartao) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }
        if (cartao == null) {
            throw new IllegalArgumentException("O cartão de crédito é obrigatório.");
        }
        this.valor = valor;
        this.cartao = cartao;
        this.status = STATUS_PENDENTE;
        this.dataPagamento = LocalDateTime.now();
    }

    /** Mensagem aprovar(codigoAutorizacao, dataHoraAutorizacao) enviada pelo ProcessadorPagamento. */
    public void aprovar(String codigoAutorizacao, LocalDateTime dataHoraAutorizacao) {
        exigirPendente();
        this.status = STATUS_APROVADO;
        this.codigoAutorizacao = codigoAutorizacao;
        this.dataHoraAutorizacao = dataHoraAutorizacao;
    }

    /** Mensagem recusar() enviada pelo ProcessadorPagamento. */
    public void recusar() {
        exigirPendente();
        this.status = STATUS_RECUSADO;
    }

    /** Mensagem getValor() enviada pela OperadoraCartaoCredito. */
    public double getValor() {
        return valor;
    }

    public boolean isAprovado() {
        return STATUS_APROVADO.equals(status);
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public LocalDateTime getDataHoraAutorizacao() {
        return dataHoraAutorizacao;
    }

    public CartaoCredito getCartao() {
        return cartao;
    }

    private void exigirPendente() {
        if (!STATUS_PENDENTE.equals(status)) {
            throw new IllegalStateException("O pagamento já foi processado (" + status + ").");
        }
    }
}
