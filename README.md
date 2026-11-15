# Minha Refeição — Implementação em Java

Implementação do cenário do **diagrama de sequência** (`docs/sequencia_unica.*`) e do **diagrama de classes de projeto**
(`docs/classes_unico.*`), desde a **fronteira** e o **controlador** até as **entidades de domínio**.
Cada classe, método e mensagem do diagrama existe no código com o mesmo nome e a mesma assinatura.

## Como executar (JDK 17 ou superior)

| Sistema | Programa interativo | Teste automatizado |
|---|---|---|
| Linux / macOS |`cd ProjetoFinal/MarmitasJava`, `chmod +x compilar_e_executar.sh executar_teste.sh`, `./compilar_e_executar.sh`| `./executar_teste.sh` |
| Windows |`cd ProjetoFinal\MarmitasJava`, `compilar_e_executar.bat` | `executar_teste.bat` |

O código de acesso enviado por SMS é **simulado**: ele aparece no console (`[SMS simulado] ... seu código de acesso é 123456`).
Cartões para teste: `4242 4242 4242 4242` (aprovado), `4000 0000 0000 0002` (recusado). Use validade futura (ex.: `12/39`) e CVV de 3 dígitos.

## Arquitetura (pacotes)

| Pacote | Papel | Classes |
|---|---|---|
| `fronteira` | Objeto de **fronteira** | `Fronteira` (cada método público = um estímulo do ator), `Main` (monta os objetos) |
| `controle` | **Controlador** e **processador** | `ControladorContratacao`, `ProcessadorPagamento` |
| `colecoes` | **Collections** | `Assinantes`, `Catalogo` |
| `servicos` | Serviço externo simulado | `ServicoSMS` |
| `dominio` | **Entidades** | `Assinante`, `CodigoConfirmacao`, `Plano`, `PreferenciaAlimentar`, `Cardapio`, `ItemCardapio` (abstrata), `PratoPrincipal`, `Acompanhamento`, `Sobremesa`, `Assinatura`, `Pedido`, `ItemPedido`, `EnderecoEntrega`, `Entrega`, `CartaoCredito`, `Pagamento`, `OperadoraCartaoCredito`, `RespostaAutorizacao`, `LimitePlanoExcedidoException` |
| `teste` | Teste do cenário | `TesteCenarioPrincipal` |

## Correspondência com o diagrama de sequência

### Estímulos do ator → métodos da `Fronteira` → mensagens ao controlador

| Estímulo (diagrama) | Método da `Fronteira` | Mensagem ao `ControladorContratacao` |
|---|---|---|
| Solicitar código de acesso | `solicitarCodigoDeAcesso()` | `solicitarCodigoAcesso(numeroCelular)` |
| Validar código de acesso | `validarCodigoDeAcesso()` | `validarCodigoAcesso(numeroCelular, codigo)` |
| Consultar planos disponíveis | `consultarPlanosDisponiveis()` | `consultarPlanosDisponiveis()` |
| Escolher plano | `escolherPlano()` | `escolherPlano(quantidadeDeRefeicoes)` |
| Definir preferências alimentares | `definirPreferenciasAlimentares()` | `definirPreferenciasAlimentares(nomes)` |
| Consultar cardápio por tipo de item | `consultarCardapioPorTipoDeItem(tipoItem)` | `consultarCardapio(tipoItem)` |
| Selecionar item do cardápio | `selecionarItemDoCardapio()` | `selecionarItem(nomeItem, quantidade)` |
| Remover item do pedido | `removerItemDoPedido()` | `removerItem(nomeItem)` |
| Alterar quantidade de item | `alterarQuantidadeDeItem()` | `alterarQuantidadeItem(nomeItem, novaQuantidade)` |
| Conferir resumo do pedido | `conferirResumoDoPedido()` | `conferirResumoPedido()` |
| Cadastrar endereço de entrega | `cadastrarEnderecoDeEntrega()` | `cadastrarEnderecoEntrega(cep, rua, numero, complemento, bairro, cidade)` |
| Consultar valor da assinatura | `consultarValorDaAssinatura()` | `consultarValorAssinatura()` |
| Efetuar pagamento da assinatura | `efetuarPagamentoDaAssinatura()` (usa `exibirProcessamento()`) | `efetuarPagamento(numeroCartao, nomeImpresso, validade, cvv)` e depois `consultarComprovanteAssinatura()` |

### Mensagens entre objetos → onde estão no código

| Mensagem do diagrama | Origem → destino | Implementação |
|---|---|---|
| `obterOuCadastrar` / `buscarPorCelular` | Controlador → `Assinantes` | `colecoes/Assinantes.java` |
| `gerarCodigoAcesso()` + `«create»` | Controlador → `Assinante` → `CodigoConfirmacao` | `Assinante.gerarCodigoAcesso()` cria `new CodigoConfirmacao()` |
| `enviarSMS(numeroCelular, codigo)` | Controlador → `ServicoSMS` | `servicos/ServicoSMS.java` |
| `validarCodigo(codigo)` / `validar(codigo)` | Controlador → `Assinante` → `CodigoConfirmacao` | `Assinante.validarCodigo`, `CodigoConfirmacao.validar` |
| `listarPlanos`, `buscarPlano`, `buscarPreferencias`, `obterCardapioVigente` | Controlador → `Catalogo` | `colecoes/Catalogo.java` |
| `contratarPlano(plano)` + `«create»(plano)` | Controlador → `Assinante` → `Assinatura` | `Assinante.contratarPlano` cria `new Assinatura(plano)` |
| `definirPreferencias(preferencias)` | Controlador → `Assinante` | `Assinante.definirPreferencias` |
| `listarItens(tipoItem)`, `buscarItem(nomeItem)` | Controlador → `Cardapio` | `Cardapio.java` |
| `adicionarItemAoPedido(item, quantidade)` | Controlador → `Assinatura` | `Assinatura.adicionarItemAoPedido` |
| `«create»` Pedido (primeiro item) | `Assinatura` → `Pedido` | `Assinatura.obterOuCriarPedido()` |
| `getTipo()` / `contarItens(tipo)` / `getLimite(tipo)` | `Assinatura` → `ItemCardapio` / `Pedido` / `Plano` | `Assinatura.adicionarItemAoPedido` |
| `adicionarItem(item, quantidade)` + `«create»(item, quantidade)` | `Assinatura` → `Pedido` → `ItemPedido` | `Pedido.adicionarItem` cria `new ItemPedido(...)` |
| `removerItemDoPedido`, `removerItem` | Controlador → `Assinatura` → `Pedido` | `Assinatura`, `Pedido` |
| `alterarQuantidadeDoItem`, `obterTipoDoItem`, `contarItensAposAlteracao`, `alterarQuantidade`, `setQuantidade` | `Assinatura` → `Pedido` → `ItemPedido` | `Assinatura.alterarQuantidadeDoItem`, `Pedido`, `ItemPedido` |
| `obterPedido()`, `descreverItens()`, `descrever()`, `getNome()`, `getTipo()` | Controlador → `Assinatura`/`Pedido` → `ItemPedido` → `ItemCardapio` | `Pedido.descreverItens` percorre os `ItemPedido` |
| `cadastrarEndereco(...)` + `«create»(...)` | Controlador → `Assinante` → `EnderecoEntrega` | `Assinante.cadastrarEndereco` |
| `definirEnderecoDoPedido`, `definirEndereco` | Controlador → `Assinatura` → `Pedido` | `Assinatura`, `Pedido` |
| `calcularValorTotal()` / `getPreco()` | Controlador ou Processador → `Assinatura` → `Plano` | `Assinatura.calcularValorTotal` |
| `processarPagamento(...)` | Controlador → `ProcessadorPagamento` | `controle/ProcessadorPagamento.java` |
| `«create»(numeroCartao, nomeImpresso, validade)` | Processador → `CartaoCredito` | `new CartaoCredito(...)` |
| `registrarPagamento(valor, cartao)`, `validarParaPagamento()`, `«create»(valor, cartao)` | Processador → `Assinatura` → `Pedido` / `Pagamento` | `Assinatura.registrarPagamento` |
| `autorizar(pagamento, numeroCartao, validade, cvv)` + `getValor()` | Processador → `OperadoraCartaoCredito` → `Pagamento` | `OperadoraCartaoCredito.autorizar` |
| `aprovar(...)` / `recusar()` | Processador → `Pagamento` | `Pagamento.java` |
| `confirmarAssinatura()`, `calcularPrimeiraEntrega()`, `confirmar(previsao)`, `gerarProtocolo()`, `«create»(previsao)` | Processador → `Assinatura` → `Pedido` → `Entrega` | `Assinatura.confirmarAssinatura`, `Pedido.confirmar` |
| `gerarComprovante()`, `descrever()`, `getPrevisaoHorario()`, `getQuantidadeDeRefeicoes()`, `getPeriodicidade()` | Controlador → `Assinatura` → `Pedido` / `EnderecoEntrega` / `Entrega` / `Plano` | `Assinatura.gerarComprovante`, `Pedido.gerarComprovante` |

### Fluxos alternativos (blocos `alt`/`opt` do diagrama)

| Fluxo | Onde está |
|---|---|
| Código inválido ou expirado (permite reenvio) | `ControladorContratacao.validarCodigoAcesso` devolve `false`; `Fronteira.iniciar()` oferece o reenvio |
| Limite do plano excedido | `Assinatura.validarLimite` lança `LimitePlanoExcedidoException`; a `Fronteira` exibe "Limite atingido" |
| Ajustar o pedido (Tela 6c) | `removerItem` e `alterarQuantidadeItem` (também validam o limite do plano) |
| Pagamento recusado (Tela 11) | `ProcessadorPagamento` chama `Pagamento.recusar()` e devolve `false`; a `Fronteira` oferece tentar de novo |

## Correspondência de tipos (diagrama → Java)

`Plano[]`/`ItemCardapio[]` → `List<...>` · `Double` → `double` · `DateTime` → `LocalDateTime` · `Date` → `LocalDate` · `int`, `boolean` e `String` iguais.
Getters simples e construtores triviais não aparecem no diagrama.

## Regras e premissas (derivadas do protótipo)

- **Limites por plano:** pratos e acompanhamentos até a quantidade de refeições do plano; sobremesas até 2 para cada 5 refeições (o protótipo mostra "Sobremesas: 1 de 2" no plano de 5).
- **Primeira entrega:** próxima quarta-feira com ao menos 2 dias de antecedência, das 11h às 13h (como na Tela 10).
- **Valor da assinatura:** preço do plano (R$ 149,90 / 269,90 / 359,90, da Tela 3).
- **Operadora simulada:** aprova cartões com número válido (Luhn), CVV de 3 ou 4 dígitos e validade futura; recusa cartões terminados em `0002`.
- O pedido precisa ter ao menos um item e um endereço antes do pagamento; depois de confirmado não pode mais ser alterado.
- O nome do assinante não é coletado neste cenário (a Tela 2 pede apenas o celular).
- Cardápio vigente e catálogo são montados em memória por `Catalogo.padrao()` (sem banco de dados).

## Persistência de dados (CSV)

O projeto grava cada assinatura confirmada em `dados/assinaturas.csv`, usando UTF-8 e separador `;`.
A gravação acontece automaticamente depois que o pagamento é aprovado e a assinatura é ativada.

O arquivo contém: data do registro, celular do assinante, protocolo, status, quantidade de refeições do plano,
periodicidade, valor, itens do pedido, endereço e previsão da entrega. Os dados são acrescentados ao final do arquivo,
sem apagar registros anteriores. A pasta `dados` e o arquivo CSV são criados automaticamente na primeira gravação.

**Privacidade:** o projeto não grava número do cartão, CVV nem código SMS. O CSV contém celular e endereço de entrega;
proteja o arquivo e não o compartilhe publicamente. Para consultar os registros, abra `dados/assinaturas.csv` no Excel
ou em um editor de texto. A persistência é local, em arquivo, não é um banco de dados compartilhado.
