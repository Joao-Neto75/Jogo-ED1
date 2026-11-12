package br.ed1.candymatch.modelo;

import br.ed1.candymatch.estruturas.ListaEncadeada;

import java.util.Objects;

/**
 * Sequência de 3 ou mais gemas iguais em linha ou coluna.
 * Requisito: Lista Encadeada (as posições ficam em uma {@link ListaEncadeada}).
 */
public final class Combinacao {

    public static final int TAMANHO_MINIMO = 3;

    private final TipoGema tipo;
    private final ListaEncadeada<Posicao> posicoes;

    public Combinacao(TipoGema tipo, ListaEncadeada<Posicao> posicoes) {
        this.tipo = Objects.requireNonNull(tipo, "tipo da combinação não pode ser nulo");
        this.posicoes = Objects.requireNonNull(posicoes, "posições da combinação não podem ser nulas");
        if (posicoes.tamanho() < TAMANHO_MINIMO) {
            throw new IllegalArgumentException(
                    "Combinação precisa de pelo menos " + TAMANHO_MINIMO + " posições, recebeu " + posicoes.tamanho());
        }
    }

    public TipoGema getTipo() {
        return tipo;
    }

    public ListaEncadeada<Posicao> getPosicoes() {
        return posicoes;
    }

    public int tamanho() {
        return posicoes.tamanho();
    }

    @Override
    public String toString() {
        return tipo + " " + posicoes;
    }
}
