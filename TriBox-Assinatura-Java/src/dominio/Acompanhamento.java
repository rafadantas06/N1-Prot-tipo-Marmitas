package dominio;

/** Especialização de ItemCardapio. */
public class Acompanhamento extends ItemCardapio {

    public Acompanhamento(String nome, String descricao) {
        super(nome, descricao);
    }

    @Override
    public String getTipo() {
        return TIPO_ACOMPANHAMENTO;
    }
}
