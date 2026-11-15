package dominio;

/** Especialização de ItemCardapio. */
public class PratoPrincipal extends ItemCardapio {

    public PratoPrincipal(String nome, String descricao) {
        super(nome, descricao);
    }

    @Override
    public String getTipo() {
        return TIPO_PRATO_PRINCIPAL;
    }
}
