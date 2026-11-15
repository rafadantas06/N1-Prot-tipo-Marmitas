package dominio;

/** Especialização de ItemCardapio. */
public class Sobremesa extends ItemCardapio {

    public Sobremesa(String nome, String descricao) {
        super(nome, descricao);
    }

    @Override
    public String getTipo() {
        return TIPO_SOBREMESA;
    }
}
