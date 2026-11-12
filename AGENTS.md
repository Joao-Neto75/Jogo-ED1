# AGENTS.md — CandyMatch (Match-3 em Java + libGDX)

Contexto do projeto para agentes de código (Claude Code, Codex, etc.).
Leia este arquivo inteiro antes de escrever ou alterar código.

## 1. O que é este projeto

Trabalho acadêmico da disciplina **Estrutura de Dados 1 (ED1)**, feito por uma dupla.
Jogo de combinação (match-3) inspirado em Candy Crush, em **Java** com o framework **libGDX** (https://libgdx.com/).

O objetivo não é só o jogo funcionar: **o professor avalia o uso correto das estruturas de dados**, e na apresentação a dupla mostrará exatamente onde e como cada requisito é usado.

> **O professor liberou o uso de `java.util` e de qualquer biblioteca necessária**, desde que todos os requisitos sejam cumpridos.
> Mesmo assim, **os itens avaliados (lista encadeada, pilha, fila, busca e ordenação) usam as implementações próprias** de `estruturas/` e `algoritmos/`, pois são o que será apresentado.
> `java.util` pode ser usado em partes auxiliares (UI, utilitários, código que não representa um requisito).

## 2. Checklist do professor (critérios de avaliação)

Requisitos obrigatórios, conforme o professor:

1. Qualidade do Jogo
2. Vetor ou Matriz
3. Generic
4. Arquivos
5. Pilha ou Fila (o plano implementa as duas)
6. Lista Encadeada
7. Algoritmo de Busca
8. Algoritmo de Ordenação

Cada item precisa estar **claramente presente e demonstrável** no código:

| Requisito | Implementação planejada | Onde |
|---|---|---|
| Qualidade do jogo | Jogo completo: menu, níveis, pontuação, game over, animações, sons | `telas/`, `visual/` |
| Vetor ou Matriz | `Gem[8][8]` no tabuleiro; vetores para tipos de gema, config de níveis, top 10 | `modelo/Tabuleiro`, `modelo/Nivel` |
| Generic | `No<T>`, `ListaEncadeada<T>`, `Pilha<T>`, `Fila<T>`; ordenação com `<T extends Comparable<T>>` ou `Comparator<T>` | `estruturas/`, `algoritmos/` |
| Arquivos | Ranking (`ranking.txt`, `nome;pontos`), save/continuar partida (`salvamento.txt`), leitura de níveis (`niveis/nivelN.txt`) | `arquivos/` |
| Pilha | Botão Desfazer jogada (empilha estados, desempilha ao desfazer) | `logica/GerenciadorDesfazer` + `estruturas/Pilha` |
| Fila | Fila de eventos/animações (matches e quedas em ordem, essencial p/ cascata) | `logica/Partida`, `GerenciadorGravidade`, `PreenchedorTabuleiro` produzem; `visual/GerenciadorAnimacao` consome |
| Lista encadeada | Lista de matches por rodada; ranking carregado do arquivo; botões da tela | `estruturas/ListaEncadeada`, usada em `BuscadorCombinacoes`, `Combinacao`, `logica/Ranking`, `GerenciadorArquivoRanking`, `visual/GrupoBotoes` |
| Algoritmo de busca | Busca binária do recorde por nome (tela Ranking); busca sequencial da dica e do jogador no ranking | `algoritmos/Buscador`, usado em `logica/Ranking` e `logica/BuscadorDica` |
| Algoritmo de ordenação | MergeSort (estável) para ranking por pontuação e por nome | `algoritmos/Ordenador`, usado em `logica/Ranking` |

Ao implementar qualquer feature, diga ao usuário **qual item do checklist ela cobre**.

## 3. Mecânica do jogo

- Tabuleiro **8x8** de gemas coloridas (`GemType` enum).
- Jogador troca duas gemas **adjacentes** (clique/arraste).
- Troca só é válida se gerar match (3+ iguais em linha ou coluna). Se não gerar, a troca é revertida.
- Ciclo após troca válida: `achar matches → remover → gravidade → refill → repetir até estabilizar` (cascata).
- Cascatas dão mais pontos (multiplicador por nível de combo).
- Limite de jogadas por nível; cada nível tem meta de pontos.

```
Clique/arraste → Valida troca → Procura matches → Remove gemas
        ↑                                              ↓
   Fim de jogo? ← Atualiza pontos ← Refill ← Gravidade
        (se houve match, repete o ciclo até estabilizar)
```

Fluxo de telas:

```
Menu Principal → Seleção de Nível → Tela do Jogo → Game Over / Vitória
      │                                                  │
      └──────────────→ Ranking ←─────────────────────────┘
```

## 4. Arquitetura e estrutura de pastas

```
Jogo-ED1/  (raiz do Gradle: settings.gradle, build.gradle, gradle.properties)
├── core/src/main/java/br/ed1/candymatch/
│   ├── JogoPrincipal.java → classe Game do libGDX
│   ├── telas/       TelaBase, TelaMenu, TelaSelecaoNivel, TelaJogo, TelaFimDeJogo, TelaRanking
│   ├── modelo/      Gema, TipoGema, Tabuleiro (matriz), Posicao, Jogada, Combinacao, Nivel,
│   │                RegistroPontuacao, EstadoPartida, EventoTabuleiro
│   ├── logica/      Partida (coordena o ciclo), BuscadorCombinacoes, GerenciadorGravidade,
│   │                PreenchedorTabuleiro, BuscadorDica, CalculadoraPontuacao, GerenciadorDesfazer,
│   │                Ranking (top 10, recorde por jogador, busca binária por nome)
│   ├── estruturas/  No<T>, ListaEncadeada<T>, Pilha<T>, Fila<T>
│   ├── algoritmos/  Ordenador (MergeSort), Buscador (binária e sequencial)
│   ├── arquivos/    GerenciadorArquivoRanking, GerenciadorSalvamento, CarregadorNivel
│   ├── visual/      RenderizadorTabuleiro, ControladorEntrada, GerenciadorAnimacao, Hud,
│   │                Fontes (FreeType), GerenciadorSom, Botao, GrupoBotoes, CampoTexto, Formas
│   └── console/     JogoConsole (Fase 2: jogo no terminal, só a lógica)
├── core/src/test/java/...  testes JUnit 5
├── assets/          imagens/, sons/, fontes/, niveis/ (nivel1.txt, nivel2.txt, ...)
└── lwjgl3/          Lwjgl3Launcher (desktop)
```

### Regras de dependência entre camadas

- `modelo`, `logica`, `estruturas`, `algoritmos`: **Java puro, sem import de `com.badlogic.gdx`**. Isso permite testar tudo no console.
- `arquivos`: pode usar `Gdx.files`, mas isole isso em poucos pontos.
- `visual` e `telas`: únicos pacotes que desenham, tocam som e leem input.
- Dependência permitida: `telas/visual → logica → modelo → estruturas`. Nunca o contrário.
- A lógica **não conhece** a UI. A UI consulta/comanda a lógica pela classe `Partida` (`logica/`).

## 5. Especificação dos componentes principais

**`estruturas/`** (todas genéricas, implementadas do zero)
- `No<T>`: guarda `T data` e referência `next` (e `prev`, se lista dupla).
- `ListaEncadeada<T>`: `adicionar`, `adicionarNoInicio`, `remover`, `removerElemento`, `obter`, `contem`, `tamanho`, `estaVazia`, `limpar`, iteração (`Iterable<T>`), `paraVetor`/`deVetor` (para ordenar e buscar).
- `Pilha<T>`: `empilhar`, `desempilhar`, `espiar`, `tamanho`, `estaVazia`, `limpar`. Encadeada pelo topo.
- `Fila<T>`: `enfileirar`, `desenfileirar`, `espiar`, `tamanho`, `estaVazia`, `limpar`. Encadeada com ponteiros início/fim.
- Tratar erros (pop/dequeue em estrutura vazia) de forma explícita e documentada.

**`modelo/Tabuleiro`**
- Contém `Gem[][] grid` (8x8). Expõe operações como: trocar duas posições, consultar gema, definir gema, copiar o estado (para undo), verificar adjacência.
- Sem lógica de regras pesada: quem acha matches é o `BuscadorCombinacoes`.

**`logica/BuscadorCombinacoes`**
- Varre linhas e colunas e devolve os matches como `ListaEncadeada<Match>`.

**`logica/GerenciadorGravidade` e `PreenchedorTabuleiro`**
- Gravidade: gemas caem para preencher vazios por coluna.
- Refill: gera novas gemas no topo para completar as colunas.
- Ambos devem produzir **eventos** (ex.: "gema X caiu de A para B") em uma `Fila` para a UI animar na ordem.

**`logica/GerenciadorDesfazer`**
- Usa `Pilha` para guardar snapshots do estado (tabuleiro + pontos + jogadas restantes) antes de cada jogada válida.
- `undo()` restaura o último snapshot. Definir limite de profundidade se necessário.

**`logica/BuscadorDica`**
- Busca sequencial por uma troca que gere match; devolve a primeira encontrada (ou nenhuma).
- Também útil para detectar "sem jogadas possíveis" e embaralhar.

**`logica/CalculadoraPontuacao`**
- Pontos por tamanho do match e multiplicador de cascata.

**`algoritmos/Ordenador`**
- Genérico (`<T extends Comparable<T>>` e/ou `Comparator<T>`): MergeSort ou QuickSort escrito à mão.
- Usado para ordenar ranking por pontuação (decrescente) e por nome (para a busca binária).

**`algoritmos/Buscador`**
- Busca binária genérica (exige coleção ordenada) e busca sequencial genérica.
- Uso: consultar recorde de um jogador por nome no ranking.

**`arquivos/`**
- `GerenciadorArquivoRanking`: lê/grava ranking em texto (`nome;pontos`), carrega para `ListaEncadeada<RegistroPontuacao>`, mantém top 10.
- `GerenciadorSalvamento`: serializa tabuleiro, nível, pontos e jogadas restantes; permite continuar partida.
- `CarregadorNivel`: lê `assets/niveis/nivelN.txt` (meta de pontos, limite de jogadas, tipos de gema, etc.).
- **Atenção libGDX:** `Gdx.files.internal` é somente leitura (use para ler assets/níveis). Para **escrever** ranking e save, use `Gdx.files.local`.

**`visual/`**
- `RenderizadorTabuleiro`: desenha a matriz na tela.
- `ControladorEntrada`: converte clique/arraste em posições do tabuleiro.
- `GerenciadorAnimacao`: consome a fila de eventos e anima troca, queda e explosão em ordem.
- `Hud`: pontos, jogadas restantes, botões Desfazer e Dica.

## 6. Prioridades de implementação

**MVP (obrigatório)**
- Tabuleiro com troca por clique/arraste
- Detecção de matches, gravidade, refill com cascata
- Pontuação e limite de jogadas
- Menu, game over e ranking

**Nível 2**
- Animações, sons e música
- Vários níveis com metas diferentes
- Desfazer e dica

**Nível 3 (só se sobrar tempo)**
- Gemas especiais (4 = bomba de linha, 5 = bomba de cor)
- Partículas e efeitos
- Salvar e continuar partida

## 7. Ordem de trabalho recomendada

1. **Fase 0 — Setup:** projeto via gdx-liftoff, repositório GitHub, padrão de nomes.
2. **Fase 1 — Estruturas:** `Node`, `ListaEncadeada`, `Pilha`, `Fila`, testadas isoladamente (console/JUnit).
3. **Fase 2 — Lógica no console:** jogo funcionando sem gráficos, imprimindo a matriz no terminal.
4. **Fase 3 — Interface libGDX:** desenhar tabuleiro, capturar cliques, ligar na lógica.
5. **Fase 4 — Pilha, fila e arquivos:** desfazer, fila de eventos, salvar/carregar.
6. **Fase 5 — Ranking, busca e ordenação:** fechar requisitos do professor.
7. **Fase 6 — Polimento:** animações, sons, níveis, bugs, ensaio da apresentação.

Respeite a ordem: **não construa UI sobre lógica que ainda não foi testada no console.**

## 8. Divisão da dupla

- **Pessoa A (lógica e estruturas):** `estruturas/`, `modelo/Tabuleiro`, `logica/*`, `algoritmos/*`.
- **Pessoa B (interface e arquivos):** setup libGDX, `telas/`, `visual/`, `arquivos/`, `TelaRanking`.
- Contrato inicial: a API pública de `Tabuleiro` e `Partida` (trocar, consultar, copiar, verificar adjacência) deve ser definida cedo para B desenhar sem esperar A.
- Trabalhar em pastas diferentes, commits pequenos e frequentes, para evitar conflitos de merge.

## 9. Convenções de código

- Java 17+ e libGDX 1.13.1 (versões em `gradle.properties`).
- Idioma de nomes de classes/métodos: **português** (pacote `br.ed1.candymatch`) — manter consistente em todo o projeto.
- Comentários e Javadoc em **português**, curtos, explicando o *porquê*, não o óbvio.
- Cada estrutura de dados e algoritmo deve ter comentário no topo indicando **qual requisito do professor ele cumpre** e sua complexidade (Big-O) dos métodos principais.
- Sem números mágicos: tamanho do tabuleiro, pontuações e limites em constantes.
- Sem `static` mutável espalhado; passe dependências por construtor.
- Classes pequenas e com uma responsabilidade.

## 10. Como o agente deve trabalhar neste repositório

1. **Antes de codar:** identifique a fase atual, o pacote correto e o item do checklist envolvido.
2. **Pergunte** quando houver decisão que afete a avaliação (ex.: formato dos arquivos).
3. **Implemente em passos pequenos** e diga como testar cada passo (console ou JUnit).
4. **Nunca** substitua uma estrutura própria que cumpre um requisito por uma da biblioteca padrão para "simplificar".
5. **Não** misture código libGDX em `modelo/`, `logica/`, `estruturas/` ou `algoritmos/`.
6. **Não** reescreva arquivos inteiros sem necessidade; faça mudanças focadas.
7. Ao terminar uma tarefa, liste: arquivos criados/alterados, requisito do professor coberto, e o que falta.
8. Mantenha o projeto compilando a cada etapa.

## 11. Comandos úteis

Requer apenas JDK 17+. Gradle e Maven **não** precisam estar instalados: os wrappers baixam a versão certa.
Os dois builds coexistem e usam as mesmas pastas (`src/main/java`, `src/test/java`); Gradle gera em `build/`, Maven em `target/`.

```bash
# Gradle (Windows: gradlew.bat)
./gradlew lwjgl3:run      # roda o jogo (desktop)
./gradlew build           # compila tudo
./gradlew test            # roda testes
./gradlew :core:runConsole --console=plain   # joga no terminal (modo console)

# Maven (Windows: mvnw.cmd)
./mvnw test                              # roda testes
./mvnw package                           # gera lwjgl3/target/CandyMatch.jar (com assets embutidos)
java -jar lwjgl3/target/CandyMatch.jar   # roda de qualquer pasta; ranking/salvamento ficam na pasta atual
./mvnw -pl lwjgl3 -am compile exec:exec  # compila e abre o jogo
```

No macOS, o lançador reinicia a JVM com `-XstartOnFirstThread` sozinho (`lwjgl3/IniciadorMacOS`).

## 12. Estado atual do projeto

> Atualize esta seção conforme o trabalho avança, para o agente saber onde parou.

- [x] Fase 0 — Setup (Gradle, wrapper 8.10.2, gdx-freetype)
- [x] Fase 1 — Estruturas genéricas (`No`, `ListaEncadeada`, `Pilha`, `Fila`)
- [x] Fase 2 — Lógica (combinações, gravidade, refill, cascata, pontuação, reorganização sem jogadas) + modo console
- [x] Fase 3 — Interface libGDX (menu, seleção de nível, jogo com clique/arraste, fim de jogo, ranking)
- [x] Fase 4 — Desfazer (Pilha), fila de animação, níveis em arquivo, salvar/continuar partida
- [x] Fase 5 — Ranking em arquivo, MergeSort, busca binária por nome
- [x] Fase 6 — Animações, sons (`scripts/gerar_sons.py`), 6 níveis calibrados por simulação
- [ ] Ensaio da apresentação; opcional: gemas especiais (Nível 3 da seção 6)

Notas de implementação:
- 57 testes JUnit passando (`./gradlew test`), incluindo arquivos (via `new FileHandle(File)`, sem iniciar o libGDX)
  e a reprodução da fila de animação (`GerenciadorAnimacaoTest`).
- Ranking guarda **um registro por jogador** (o recorde), para a busca binária por nome ter resposta única.
- O salvamento não guarda o histórico do desfazer: ao continuar, a pilha começa vazia.
- Sem jogadas possíveis após uma cascata, `Partida` reorganiza o tabuleiro e enfileira `EMBARALHAMENTO`.
- Gemas são desenhadas com `ShapeRenderer` (cor + forma por tipo), sem imagens; fonte Noto Sans (OFL) em `assets/fontes`.
