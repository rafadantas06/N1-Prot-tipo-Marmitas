package dominio;

/** Endereço de entrega cadastrado pelo assinante e vinculado ao pedido. */
public class EnderecoEntrega {

    private final String cep;
    private final String rua;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;

    /** Mensagem «create»(cep, rua, numero, complemento, bairro, cidade) enviada pelo Assinante. */
    public EnderecoEntrega(String cep, String rua, String numero, String complemento, String bairro, String cidade) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("CEP inválido. Informe 8 dígitos (ex.: 04538-132).");
        }
        this.cep = digitos.substring(0, 5) + "-" + digitos.substring(5);
        this.rua = exigir(rua, "A rua");
        this.numero = exigir(numero, "O número");
        this.complemento = complemento == null ? "" : complemento.trim();
        this.bairro = exigir(bairro, "O bairro");
        this.cidade = exigir(cidade, "A cidade");
    }

    /** Mensagem descrever() enviada pelo Pedido ao montar o comprovante. */
    public String descrever() {
        String complementoTexto = complemento.isEmpty() ? "" : ", " + complemento;
        return rua + ", " + numero + complementoTexto + " - " + bairro + ", " + cidade + " - CEP " + cep;
    }

    public String getCep() {
        return cep;
    }

    public String getRua() {
        return rua;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    private static String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório(a).");
        }
        return valor.trim();
    }
}
