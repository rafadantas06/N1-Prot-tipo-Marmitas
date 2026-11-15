package dominio;

import java.time.LocalDateTime;

/** Resposta da operadora do cartão a uma solicitação de autorização. */
public class RespostaAutorizacao {

    private final boolean aprovada;
    private final String codigoAutorizacao;
    private final LocalDateTime dataHora;

    public RespostaAutorizacao(boolean aprovada, String codigoAutorizacao, LocalDateTime dataHora) {
        this.aprovada = aprovada;
        this.codigoAutorizacao = codigoAutorizacao;
        this.dataHora = dataHora;
    }

    public boolean isAprovada() {
        return aprovada;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
