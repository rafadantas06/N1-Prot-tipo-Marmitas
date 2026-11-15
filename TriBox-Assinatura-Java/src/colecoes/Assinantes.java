package colecoes;

import dominio.Assinante;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collection de Assinante: busca por celular e cadastro de novos assinantes. */
public class Assinantes {

    private final List<Assinante> lista = new ArrayList<>();

    /** Mensagem obterOuCadastrar(numeroCelular) enviada pelo controlador. */
    public Assinante obterOuCadastrar(String numeroCelular) {
        String celular = normalizar(numeroCelular);
        for (Assinante assinante : lista) {
            if (assinante.getNumeroCelular().equals(celular)) {
                return assinante;
            }
        }
        Assinante novo = new Assinante("", celular);
        lista.add(novo);
        return novo;
    }

    /** Mensagem buscarPorCelular(numeroCelular) enviada pelo controlador. */
    public Assinante buscarPorCelular(String numeroCelular) {
        String celular = normalizar(numeroCelular);
        for (Assinante assinante : lista) {
            if (assinante.getNumeroCelular().equals(celular)) {
                return assinante;
            }
        }
        throw new IllegalArgumentException("Celular não cadastrado. Solicite um código de acesso primeiro.");
    }

    public List<Assinante> getLista() {
        return Collections.unmodifiableList(lista);
    }

    private static String normalizar(String numeroCelular) {
        String digitos = numeroCelular == null ? "" : numeroCelular.replaceAll("\\D", "");
        if (digitos.startsWith("55") && digitos.length() > 11) {
            digitos = digitos.substring(2);
        }
        if (!digitos.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Número de celular inválido. Informe DDD + número (10 ou 11 dígitos).");
        }
        return digitos;
    }
}
