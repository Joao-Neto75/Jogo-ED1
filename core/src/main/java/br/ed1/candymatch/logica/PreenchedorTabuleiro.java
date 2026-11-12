package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;

import java.util.Objects;
import java.util.Random;

/**
 * Gera gemas novas: o tabuleiro inicial (sem combinações prontas) e o refill após a gravidade.
 * Requisitos: Vetor (tipos disponíveis e vetor de candidatos), Fila (eventos de criação).
 */
public class PreenchedorTabuleiro {

    /** Com 3+ tipos, no máximo 2 são proibidos por célula, então sempre sobra candidato. */
    public static final int MINIMO_TIPOS_PREENCHIMENTO_INICIAL = 3;

    private final TipoGema[] tiposDisponiveis;
    private final Random aleatorio;

    /** Recebe o Random por parâmetro para permitir testes com semente fixa. */
    public PreenchedorTabuleiro(TipoGema[] tiposDisponiveis, Random aleatorio) {
        Objects.requireNonNull(tiposDisponiveis, "tipos disponíveis não podem ser nulos");
        if (tiposDisponiveis.length == 0) {
            throw new IllegalArgumentException("É necessário pelo menos um tipo de gema");
        }
        for (TipoGema tipo : tiposDisponiveis) {
            Objects.requireNonNull(tipo, "tipos disponíveis não podem conter nulo");
        }
        this.tiposDisponiveis = tiposDisponiveis.clone();
        this.aleatorio = Objects.requireNonNull(aleatorio, "gerador aleatório não pode ser nulo");
    }

    /**
     * Preenche o tabuleiro inteiro garantindo que nenhuma combinação exista no início.
     * Preenche em ordem (esquerda→direita, cima→baixo), então basta olhar as duas células à esquerda e acima.
     */
    public void preencherInicial(Tabuleiro tabuleiro) {
        Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        if (tiposDisponiveis.length < MINIMO_TIPOS_PREENCHIMENTO_INICIAL) {
            throw new IllegalStateException("Preenchimento sem combinações exige pelo menos "
                    + MINIMO_TIPOS_PREENCHIMENTO_INICIAL + " tipos de gema");
        }
        for (int linha = 0; linha < tabuleiro.getLinhas(); linha++) {
            for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
                TipoGema proibidoHorizontal = tipoQueCompletaria(tabuleiro, linha, coluna, 0, -1);
                TipoGema proibidoVertical = tipoQueCompletaria(tabuleiro, linha, coluna, -1, 0);
                TipoGema escolhido = sortearExceto(proibidoHorizontal, proibidoVertical);
                tabuleiro.definirGema(new Posicao(linha, coluna), new Gema(escolhido));
            }
        }
    }

    /** Preenche apenas as células vazias (após a gravidade, os vazios ficam no topo). */
    public void preencherVazios(Tabuleiro tabuleiro, Fila<EventoTabuleiro> eventos) {
        Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        Objects.requireNonNull(eventos, "fila de eventos não pode ser nula");
        // Por coluna e de baixo para cima, para a animação "empilhar" as gemas novas na ordem natural.
        for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
            for (int linha = tabuleiro.getLinhas() - 1; linha >= 0; linha--) {
                Posicao posicao = new Posicao(linha, coluna);
                if (tabuleiro.estaVazia(posicao)) {
                    Gema nova = new Gema(tiposDisponiveis[aleatorio.nextInt(tiposDisponiveis.length)]);
                    tabuleiro.definirGema(posicao, nova);
                    eventos.enfileirar(EventoTabuleiro.criacao(posicao, nova));
                }
            }
        }
    }

    /** Se as duas células anteriores na direção indicada forem do mesmo tipo, retorna esse tipo; senão null. */
    private TipoGema tipoQueCompletaria(Tabuleiro tabuleiro, int linha, int coluna, int passoLinha, int passoColuna) {
        Posicao anterior = new Posicao(linha + passoLinha, coluna + passoColuna);
        Posicao antesDaAnterior = new Posicao(linha + 2 * passoLinha, coluna + 2 * passoColuna);
        if (!tabuleiro.estaDentro(antesDaAnterior)) {
            return null;
        }
        Gema a = tabuleiro.obterGema(anterior);
        Gema b = tabuleiro.obterGema(antesDaAnterior);
        return a != null && a.mesmoTipo(b) ? a.getTipo() : null;
    }

    private TipoGema sortearExceto(TipoGema proibido1, TipoGema proibido2) {
        TipoGema[] candidatos = new TipoGema[tiposDisponiveis.length];
        int quantidade = 0;
        for (TipoGema tipo : tiposDisponiveis) {
            if (tipo != proibido1 && tipo != proibido2) {
                candidatos[quantidade++] = tipo;
            }
        }
        return candidatos[aleatorio.nextInt(quantidade)];
    }
}
