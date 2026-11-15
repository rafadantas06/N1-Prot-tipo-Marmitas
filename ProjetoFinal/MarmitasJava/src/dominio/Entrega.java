package dominio;

import java.time.LocalDateTime;

/** Entrega gerada na confirmação do pedido, com a previsão da primeira entrega. */
public class Entrega {

    public static final String STATUS_PREPARANDO = "Preparando";

    private String statusEntrega;
    private final LocalDateTime previsaoHorario;
    private double latitudeAtual;
    private double longitudeAtual;

    /** Mensagem «create»(previsaoPrimeiraEntrega) enviada pelo Pedido. */
    public Entrega(LocalDateTime previsaoHorario) {
        if (previsaoHorario == null) {
            throw new IllegalArgumentException("A previsão da entrega é obrigatória.");
        }
        this.previsaoHorario = previsaoHorario;
        this.statusEntrega = STATUS_PREPARANDO;
    }

    /** Mensagem getPrevisaoHorario() enviada pelo Pedido ao montar o comprovante. */
    public LocalDateTime getPrevisaoHorario() {
        return previsaoHorario;
    }

    public String getStatusEntrega() {
        return statusEntrega;
    }

    public double getLatitudeAtual() {
        return latitudeAtual;
    }

    public double getLongitudeAtual() {
        return longitudeAtual;
    }
}
