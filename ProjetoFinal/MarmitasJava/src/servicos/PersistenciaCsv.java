package servicos;

import dominio.Assinante;
import dominio.Assinatura;
import dominio.EnderecoEntrega;
import dominio.ItemPedido;
import dominio.Pedido;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.StringJoiner;

/** Persistência simples das assinaturas confirmadas em arquivo CSV (UTF-8). */
public class PersistenciaCsv {

    private final Path arquivo;

    /** Por padrão, grava em dados/assinaturas.csv na pasta em que o programa foi iniciado. */
    public PersistenciaCsv() {
        this(Path.of("dados", "assinaturas.csv"));
    }

    public PersistenciaCsv(Path arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("O caminho do arquivo CSV é obrigatório.");
        }
        this.arquivo = arquivo;
    }

    /** Acrescenta uma linha com os dados da assinatura já confirmada. Não grava dados do cartão. */
    public void salvar(Assinante assinante, Assinatura assinatura) throws IOException {
        if (assinante == null || assinatura == null || assinatura.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("Assinante, assinatura e pedido são obrigatórios para persistir.");
        }

        Pedido pedido = assinatura.obterPedido();
        if (!Pedido.STATUS_CONFIRMADO.equals(pedido.getStatus())) {
            throw new IllegalStateException("Somente pedidos confirmados podem ser salvos.");
        }

        Path pasta = arquivo.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        boolean novoArquivo = !Files.exists(arquivo) || Files.size(arquivo) == 0;

        try (BufferedWriter writer = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (novoArquivo) {
                writer.write("data_registro;celular;protocolo;status;refeicoes_plano;periodicidade;valor;itens;endereco;previsao_entrega");
                writer.newLine();
            }

            StringJoiner itens = new StringJoiner(" | ");
            for (ItemPedido item : pedido.getItens()) {
                itens.add(item.descrever());
            }
            EnderecoEntrega endereco = pedido.getEndereco();
            String enderecoTexto = endereco == null ? "" : endereco.descrever();
            String previsao = pedido.getEntrega() == null || pedido.getEntrega().getPrevisaoHorario() == null
                    ? "" : pedido.getEntrega().getPrevisaoHorario().toString();

            String[] campos = {
                    LocalDateTime.now().toString(),
                    assinante.getNumeroCelular(),
                    pedido.getProtocolo(),
                    assinatura.getStatus(),
                    Integer.toString(assinatura.getPlano().getQuantidadeDeRefeicoes()),
                    assinatura.getPlano().getPeriodicidade(),
                    String.format(java.util.Locale.US, "%.2f", assinatura.calcularValorTotal()),
                    itens.toString(),
                    enderecoTexto,
                    previsao
            };
            for (int i = 0; i < campos.length; i++) {
                if (i > 0) writer.write(';');
                writer.write(escapar(campos[i]));
            }
            writer.newLine();
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    private static String escapar(String valor) {
        String seguro = valor == null ? "" : valor;
        return '"' + seguro.replace("\"", "\"\"") + '"';
    }
}
