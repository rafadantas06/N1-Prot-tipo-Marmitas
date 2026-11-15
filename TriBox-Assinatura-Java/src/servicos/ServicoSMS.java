package servicos;

/** Serviço externo de envio de SMS (simulado: a mensagem é exibida no console). */
public class ServicoSMS {

    /** Mensagem enviarSMS(numeroCelular, codigo) enviada pelo controlador. */
    public void enviarSMS(String numeroCelular, String codigo) {
        System.out.println();
        System.out.println("   [SMS simulado] Para " + numeroCelular + ": seu código de acesso é " + codigo
                + " (válido por 5 minutos).");
        System.out.println();
    }
}
