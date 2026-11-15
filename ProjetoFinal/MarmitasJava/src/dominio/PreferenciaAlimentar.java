package dominio;

/** Preferência ou restrição alimentar (Tradicional, Vegano, Sem Lactose, ...). */
public class PreferenciaAlimentar {

    private final String nome;

    public PreferenciaAlimentar(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da preferência alimentar é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }
}
