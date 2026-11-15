package dominio;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Operadora do cartão de crédito (sistema externo simulado).
 * Regras da simulação: o cartão precisa ter número válido (algoritmo de Luhn), CVV de 3 ou 4 dígitos
 * e validade futura. Por convenção de teste, cartões terminados em 0002 são sempre recusados
 * (exemplo: 4000 0000 0000 0002); o cartão 4242 4242 4242 4242 é aprovado.
 */
public class OperadoraCartaoCredito {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final String nome;
    private final List<Pagamento> pagamentosAutorizados = new ArrayList<>();

    public OperadoraCartaoCredito(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da operadora é obrigatório.");
        }
        this.nome = nome.trim();
    }

    /** Mensagem autorizar(pagamento, numeroCartao, validade, cvv) enviada pelo ProcessadorPagamento. */
    public RespostaAutorizacao autorizar(Pagamento pagamento, String numeroCartao, String validade, String cvv) {
        double valor = pagamento.getValor(); // mensagem getValor() enviada ao Pagamento
        String digitos = numeroCartao == null ? "" : numeroCartao.replaceAll("[\\s-]", "");
        LocalDateTime agora = LocalDateTime.now();

        boolean aprovada = valor > 0
                && cvvValido(cvv)
                && !validadeExpirada(validade)
                && passaNoAlgoritmoDeLuhn(digitos)
                && !digitos.endsWith("0002");

        if (!aprovada) {
            return new RespostaAutorizacao(false, null, agora);
        }
        String codigo = String.format("AUT%06d", ALEATORIO.nextInt(1_000_000));
        pagamentosAutorizados.add(pagamento);
        return new RespostaAutorizacao(true, codigo, agora);
    }

    public String getNome() {
        return nome;
    }

    public List<Pagamento> getPagamentosAutorizados() {
        return Collections.unmodifiableList(pagamentosAutorizados);
    }

    private static boolean cvvValido(String cvv) {
        return cvv != null && cvv.trim().matches("\\d{3,4}");
    }

    private static boolean validadeExpirada(String validade) {
        if (validade == null || !validade.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return true;
        }
        String[] partes = validade.trim().split("/");
        YearMonth vencimento = YearMonth.of(2000 + Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        return vencimento.isBefore(YearMonth.now());
    }

    private static boolean passaNoAlgoritmoDeLuhn(String digitos) {
        if (!digitos.matches("\\d{13,19}")) {
            return false;
        }
        int soma = 0;
        boolean dobrar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (dobrar) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            soma += d;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }
}
