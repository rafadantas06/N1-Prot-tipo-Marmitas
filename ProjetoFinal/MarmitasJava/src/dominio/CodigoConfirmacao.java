package dominio;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/** Código de acesso enviado por SMS ao assinante (válido por 5 minutos). */
public class CodigoConfirmacao {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final long VALIDADE_EM_MINUTOS = 5;

    private final String codigo;
    private final LocalDateTime dataHoraEnvio;
    private final LocalDateTime dataHoraExpiracao;
    private boolean validado;

    /** Mensagem «create» enviada pelo Assinante (gera um código de 6 dígitos). */
    public CodigoConfirmacao() {
        this.codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        this.dataHoraEnvio = LocalDateTime.now();
        this.dataHoraExpiracao = dataHoraEnvio.plusMinutes(VALIDADE_EM_MINUTOS);
        this.validado = false;
    }

    /** Mensagem validar(codigo) enviada pelo Assinante. */
    public boolean validar(String codigoInformado) {
        if (expirou() || codigoInformado == null) {
            return false;
        }
        if (codigo.equals(codigoInformado.trim())) {
            validado = true;
            return true;
        }
        return false;
    }

    private boolean expirou() {
        return LocalDateTime.now().isAfter(dataHoraExpiracao);
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getDataHoraEnvio() {
        return dataHoraEnvio;
    }

    public LocalDateTime getDataHoraExpiracao() {
        return dataHoraExpiracao;
    }

    public boolean isValidado() {
        return validado;
    }
}
