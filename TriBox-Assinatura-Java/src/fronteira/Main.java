package fronteira;

import colecoes.Assinantes;
import colecoes.Catalogo;
import controle.ControladorContratacao;
import controle.ProcessadorPagamento;
import dominio.OperadoraCartaoCredito;
import servicos.ServicoSMS;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Ponto de entrada: monta os objetos e inicia a Fronteira. */
public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        ControladorContratacao controlador = new ControladorContratacao(
                new Assinantes(),
                new ServicoSMS(),
                Catalogo.padrao(),
                new ProcessadorPagamento(new OperadoraCartaoCredito("Operadora de Cartão (simulada)")));

        new Fronteira(controlador, System.in).iniciar();
    }
}
