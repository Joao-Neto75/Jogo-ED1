package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.Combinacao;
import br.ed1.candymatch.modelo.EstadoPartida;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Coordena uma partida: valida a troca, executa o ciclo
 * combinações → remoção → gravidade → refill (repetido até estabilizar), pontua e controla o fim de jogo.
 * É a única porta de entrada da interface para a lógica.
 */
public class Partida {

    /** Proteção contra laço infinito na cascata; na prática ela estabiliza em poucas rodadas. */
    static final int MAXIMO_RODADAS_CASCATA = 100;
    /** Tentativas de gerar um tabuleiro com pelo menos uma jogada possível. */
    static final int MAXIMO_TENTATIVAS_REORGANIZAR = 1000;

    private final Nivel nivel;
    private final Tabuleiro tabuleiro;
    private final BuscadorCombinacoes buscadorCombinacoes;
    private final Gravidade gravidade;
    private final PreenchedorTabuleiro preenchedor;
    private final CalculadoraPontuacao calculadora;
    private final HistoricoJogadas desfazer;
    private final BuscadorDica buscadorDica;
    private final Fila<EventoTabuleiro> eventos = new Fila<>();

    private int pontos;
    private int jogadasRestantes;
    private int pontosUltimaJogada;
    private int maiorCascataUltimaJogada;

    public Partida(Nivel nivel, Tabuleiro tabuleiro, BuscadorCombinacoes buscadorCombinacoes,
                   Gravidade gravidade, PreenchedorTabuleiro preenchedor,
                   CalculadoraPontuacao calculadora, HistoricoJogadas desfazer, BuscadorDica buscadorDica) {
        this.nivel = Objects.requireNonNull(nivel, "nível não pode ser nulo");
        this.tabuleiro = Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        this.buscadorCombinacoes = Objects.requireNonNull(buscadorCombinacoes, "buscadorCombinacoes não pode ser nulo");
        this.gravidade = Objects.requireNonNull(gravidade, "gravidade não pode ser nula");
        this.preenchedor = Objects.requireNonNull(preenchedor, "preenchedor não pode ser nulo");
        this.calculadora = Objects.requireNonNull(calculadora, "calculadora não pode ser nula");
        this.desfazer = Objects.requireNonNull(desfazer, "desfazer não pode ser nulo");
        this.buscadorDica = Objects.requireNonNull(buscadorDica, "buscadorDica não pode ser nulo");
        this.jogadasRestantes = nivel.getLimiteJogadas();
    }

    /** Monta uma partida nova com todas as dependências padrão e um tabuleiro jogável. */
    public static Partida criar(Nivel nivel, Random aleatorio) {
        Partida partida = montar(nivel, new Tabuleiro(), aleatorio);
        partida.reorganizarTabuleiro();
        partida.eventos.limpar();
        return partida;
    }

    /** Recria uma partida salva (continuar jogo). O histórico de desfazer começa vazio. */
    public static Partida restaurar(Nivel nivel, EstadoPartida estado, Random aleatorio) {
        Objects.requireNonNull(estado, "estado salvo não pode ser nulo");
        if (estado.numeroNivel() != Objects.requireNonNull(nivel, "nível não pode ser nulo").getNumero()) {
            throw new IllegalArgumentException("Estado salvo é do nível " + estado.numeroNivel()
                    + ", mas foi informado o nível " + nivel.getNumero());
        }
        Partida partida = montar(nivel, estado.tabuleiro().copiar(), aleatorio);
        partida.pontos = estado.pontos();
        partida.jogadasRestantes = estado.jogadasRestantes();
        return partida;
    }

    private static Partida montar(Nivel nivel, Tabuleiro tabuleiro, Random aleatorio) {
        Objects.requireNonNull(nivel, "nível não pode ser nulo");
        Objects.requireNonNull(aleatorio, "gerador aleatório não pode ser nulo");
        BuscadorCombinacoes buscador = new BuscadorCombinacoes();
        PreenchedorTabuleiro preenchedor = new PreenchedorTabuleiro(nivel.getTiposDisponiveis(), aleatorio);
        return new Partida(nivel, tabuleiro, buscador, new Gravidade(), preenchedor,
                new CalculadoraPontuacao(), new HistoricoJogadas(), new BuscadorDica(buscador));
    }

    /**
     * Executa a jogada. Se a troca não gerar combinação ela é desfeita e retorna false
     * (sem gastar jogada). Os eventos gerados ficam em {@link #getEventos()}.
     *
     * @throws IllegalStateException    se a partida já terminou
     * @throws IllegalArgumentException se as posições forem inválidas ou não adjacentes
     */
    public boolean realizarJogada(Jogada jogada) {
        Objects.requireNonNull(jogada, "jogada não pode ser nula");
        if (estaFinalizada()) {
            throw new IllegalStateException("A partida já terminou; nenhuma jogada é aceita");
        }
        Posicao origem = jogada.origem();
        Posicao destino = jogada.destino();
        if (!tabuleiro.estaDentro(origem) || !tabuleiro.estaDentro(destino)) {
            throw new IllegalArgumentException("Jogada fora do tabuleiro: " + jogada);
        }
        if (!origem.ehAdjacente(destino)) {
            throw new IllegalArgumentException("Só é possível trocar gemas adjacentes: " + jogada);
        }

        EstadoPartida antes = capturarEstado();
        tabuleiro.trocar(origem, destino);
        eventos.enfileirar(EventoTabuleiro.troca(origem, destino));

        if (!buscadorCombinacoes.existeCombinacao(tabuleiro)) {
            tabuleiro.trocar(origem, destino);
            eventos.enfileirar(EventoTabuleiro.troca(origem, destino));
            return false;
        }

        desfazer.registrar(antes);
        jogadasRestantes--;
        resolverCascata();
        if (!buscadorDica.existeJogadaPossivel(tabuleiro)) {
            reorganizarTabuleiro();
        }
        return true;
    }

    /** Restaura o estado anterior à última jogada; retorna false se não houver o que desfazer. */
    public boolean desfazerJogada() {
        if (!desfazer.podeDesfazer()) {
            return false;
        }
        EstadoPartida anterior = desfazer.desfazer();
        tabuleiro.restaurar(anterior.tabuleiro());
        pontos = anterior.pontos();
        jogadasRestantes = anterior.jogadasRestantes();
        pontosUltimaJogada = 0;
        maiorCascataUltimaJogada = 0;
        // Eventos pendentes descrevem uma jogada que não existe mais.
        eventos.limpar();
        return true;
    }

    public boolean podeDesfazer() {
        return desfazer.podeDesfazer() && !estaFinalizada();
    }

    public Optional<Jogada> obterDica() {
        return buscadorDica.buscar(tabuleiro);
    }

    /** Fotografia atual (tabuleiro copiado), usada pelo desfazer e pelo salvamento. */
    public EstadoPartida capturarEstado() {
        return new EstadoPartida(tabuleiro.copiar(), nivel.getNumero(), pontos, jogadasRestantes);
    }

    public boolean venceu() {
        return pontos >= nivel.getMetaPontos();
    }

    public boolean estaFinalizada() {
        return venceu() || jogadasRestantes == 0;
    }

    /** Fila consumida pela interface para animar as mudanças na ordem em que ocorreram. */
    public Fila<EventoTabuleiro> getEventos() {
        return eventos;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public int getPontos() {
        return pontos;
    }

    public int getJogadasRestantes() {
        return jogadasRestantes;
    }

    public int getPontosUltimaJogada() {
        return pontosUltimaJogada;
    }

    /** 1 = só a combinação da troca; 2+ = houve cascata. */
    public int getMaiorCascataUltimaJogada() {
        return maiorCascataUltimaJogada;
    }

    private void resolverCascata() {
        pontosUltimaJogada = 0;
        maiorCascataUltimaJogada = 0;
        int nivelCascata = 1;
        ListaEncadeada<Combinacao> combinacoes = buscadorCombinacoes.buscar(tabuleiro);
        while (!combinacoes.estaVazia()) {
            if (nivelCascata > MAXIMO_RODADAS_CASCATA) {
                throw new IllegalStateException("Cascata não estabilizou após " + MAXIMO_RODADAS_CASCATA + " rodadas");
            }
            for (Combinacao combinacao : combinacoes) {
                int ganho = calculadora.calcular(combinacao, nivelCascata);
                pontos += ganho;
                pontosUltimaJogada += ganho;
            }
            removerCombinacoes(combinacoes);
            gravidade.aplicar(tabuleiro, eventos);
            preenchedor.preencherVazios(tabuleiro, eventos);
            maiorCascataUltimaJogada = nivelCascata;
            nivelCascata++;
            combinacoes = buscadorCombinacoes.buscar(tabuleiro);
        }
    }

    private void removerCombinacoes(ListaEncadeada<Combinacao> combinacoes) {
        for (Combinacao combinacao : combinacoes) {
            for (Posicao posicao : combinacao.getPosicoes()) {
                // Em L/T a mesma posição aparece em duas combinações; só remove (e anima) uma vez.
                Gema removida = tabuleiro.removerGema(posicao);
                if (removida != null) {
                    eventos.enfileirar(EventoTabuleiro.remocao(posicao, removida));
                }
            }
        }
    }

    /** Gera um novo tabuleiro sem combinações prontas e com pelo menos uma jogada possível. */
    private void reorganizarTabuleiro() {
        for (int tentativa = 0; tentativa < MAXIMO_TENTATIVAS_REORGANIZAR; tentativa++) {
            preenchedor.preencherInicial(tabuleiro);
            if (buscadorDica.existeJogadaPossivel(tabuleiro)) {
                eventos.enfileirar(EventoTabuleiro.embaralhamento());
                return;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um tabuleiro com jogadas possíveis após "
                + MAXIMO_TENTATIVAS_REORGANIZAR + " tentativas");
    }
}
