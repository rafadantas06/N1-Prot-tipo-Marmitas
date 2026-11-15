package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pessoa que contrata a assinatura de refeições (identificada pelo número de celular). */
public class Assinante {

    private String nome;
    private final String numeroCelular;
    private final List<CodigoConfirmacao> codigos = new ArrayList<>();
    private final List<Assinatura> assinaturas = new ArrayList<>();
    private final List<PreferenciaAlimentar> preferencias = new ArrayList<>();
    private final List<EnderecoEntrega> enderecos = new ArrayList<>();

    public Assinante(String nome, String numeroCelular) {
        if (numeroCelular == null || numeroCelular.isBlank()) {
            throw new IllegalArgumentException("O número de celular é obrigatório.");
        }
        this.nome = nome == null ? "" : nome.trim();
        this.numeroCelular = numeroCelular.trim();
    }

    /**
     * Mensagem gerarCodigoAcesso() enviada pelo controlador.
     * Cria («create») um CodigoConfirmacao, associa ao assinante e devolve o código a ser enviado por SMS.
     */
    public String gerarCodigoAcesso() {
        CodigoConfirmacao novo = new CodigoConfirmacao();
        codigos.add(novo);
        return novo.getCodigo();
    }

    /** Mensagem validarCodigo(codigo) enviada pelo controlador; repassa validar(codigo) ao CodigoConfirmacao. */
    public boolean validarCodigo(String codigo) {
        CodigoConfirmacao ultimo = obterUltimoCodigo();
        return ultimo != null && ultimo.validar(codigo);
    }

    /** Mensagem contratarPlano(plano) enviada pelo controlador; cria («create») a Assinatura. */
    public Assinatura contratarPlano(Plano plano) {
        Assinatura nova = new Assinatura(plano);
        assinaturas.add(nova);
        return nova;
    }

    /** Mensagem definirPreferencias(preferencias) enviada pelo controlador. */
    public void definirPreferencias(List<PreferenciaAlimentar> novasPreferencias) {
        if (novasPreferencias == null) {
            throw new IllegalArgumentException("A lista de preferências é obrigatória.");
        }
        preferencias.clear();
        preferencias.addAll(novasPreferencias);
    }

    /** Mensagem cadastrarEndereco(...) enviada pelo controlador; cria («create») o EnderecoEntrega. */
    public EnderecoEntrega cadastrarEndereco(String cep, String rua, String numero,
                                             String complemento, String bairro, String cidade) {
        EnderecoEntrega endereco = new EnderecoEntrega(cep, rua, numero, complemento, bairro, cidade);
        enderecos.add(endereco);
        return endereco;
    }

    private CodigoConfirmacao obterUltimoCodigo() {
        return codigos.isEmpty() ? null : codigos.get(codigos.size() - 1);
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public List<CodigoConfirmacao> getCodigos() {
        return Collections.unmodifiableList(codigos);
    }

    public List<Assinatura> getAssinaturas() {
        return Collections.unmodifiableList(assinaturas);
    }

    public List<PreferenciaAlimentar> getPreferencias() {
        return Collections.unmodifiableList(preferencias);
    }

    public List<EnderecoEntrega> getEnderecos() {
        return Collections.unmodifiableList(enderecos);
    }
}
