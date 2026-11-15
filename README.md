# Minha Refeição

Implementação em **Java (JDK 17+)** do cenário de negócio de contratação de assinatura de marmitas e refeições saudáveis, cobrindo desde a **fronteira** (interface de console) e o **controlador** até as **entidades de domínio**, com **persistência em arquivos CSV** na pasta `dados/`. O projeto reflete rigorosamente o modelo definido no Diagrama de Sequência e no Diagrama de Classes de Projeto.

---

## 📂 Estrutura do Projeto

A organização dos arquivos e pacotes do repositório segue a estrutura abaixo:

```text
minha-refeicao/
├── compilar-e-executar.bat / .sh           # Scripts de compilação e execução interativa
├── dados/                                  # Arquivos CSV de persistência de dados
├── docs/                                   # Documentação, protótipo, diagramas e checklists
└── src/main/java/br/mackenzie/minharefeicao/
    ├── App.java                            # Ponto de entrada (Menu e montagem de dependências)
    ├── Demonstracao.java                   # Roteiros automáticos (fluxo principal e alternativos)
    ├── fronteira/                          # Objetos de «fronteira» (telas do console)
    ├── controle/                           # Objetos de «controle» e processadores
    ├── colecao/                            # Repositórios e «coleções» em memória / arquivos
    ├── servico/                            # Serviços externos simulados (SMS, CEP, Pagamento)
    ├── dominio/                            # Entidades de domínio, DTOs e exceções de negócio
    └── persistencia/                       # Classe técnica de leitura e gravação CSV
```

---

## 🚀 Como Executar (Requer JDK 17 ou Superior)

Você pode compilar e executar o projeto utilizando os scripts utilitários fornecidos na raiz do repositório:

| Sistema Operacional | Executar Modo Interativo | Executar Testes / Demonstração |
| :--- | :--- | :--- |
| **Linux / macOS** | `./compilar-e-executar.sh` | `./executar-teste.sh` |
| **Windows** | `compilar-e-executar.bat` | `executar-teste.bat` |

### Dados de Teste Úteis:
* **SMS Simulado:** O código OTP de 6 dígitos gerado aparece diretamente no console durante o login (`[SMS simulado] ...`).
* **Cartões de Crédito para Simulação:**
  * **Aprovado:** `4242 4242 4242 4242` (Titular: `Maria Silva`, Validade futura ex: `12/39`, CVV: `123`).
  * **Recusado pela Operadora:** `4000 0000 0000 0002`.

---

## 💾 Persistência de Dados (`dados/`)

O sistema conta com uma camada de persistência baseada em arquivos **CSV** localizados na pasta `dados/` (criada automaticamente na primeira execução):

* `planos.csv` — Planos de assinatura disponíveis.
* `preferencias.csv` — Opções de preferências e restrições alimentares.
* `cardapio.csv` — Itens vigentes do cardápio (pratos, acompanhamentos e sobremesas).
* `assinantes.csv` — Dados cadastrais dos clientes, telefones, preferências e endereços.
* `assinaturas.csv` — Histórico de planos contratados, itens dos pedidos, protocolos e status de pagamento.

---

## 🛠️ Tecnologias e Padrões Utilizados
* **Java 17** (Linguagem principal)
* **Padrões GRASP e SOLID** (Alta coesão e baixo acoplamento entre Fronteira, Controlador e Domínio)
* **Manipulação de Arquivos NIO.2** para gravação e leitura segura em CSV.
