package br.ed1.candymatch.estruturas;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Pilha genérica (LIFO) encadeada pelo topo.
 * Requisitos: Pilha, Generic.
 * Uso no jogo: HistoricoJogadas empilha o estado da partida antes de cada jogada.
 *
 * <p>Complexidade: empilhar, desempilhar, espiar, tamanho e estaVazia O(1).
 */
public class Pilha<T> {

    private No<T> topo;
    private int tamanho;

    public void empilhar(T elemento) {
        No<T> novo = new No<>(Objects.requireNonNull(elemento, "Pilha não aceita elementos nulos"));
        novo.setProximo(topo);
        topo = novo;
        tamanho++;
    }

    public T desempilhar() {
        exigirNaoVazia("desempilhar");
        T dado = topo.getDado();
        topo = topo.getProximo();
        tamanho--;
        return dado;
    }

    /** Retorna o elemento do topo sem removê-lo. */
    public T espiar() {
        exigirNaoVazia("espiar");
        return topo.getDado();
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public void limpar() {
        topo = null;
        tamanho = 0;
    }

    private void exigirNaoVazia(String operacao) {
        if (estaVazia()) {
            throw new NoSuchElementException("Pilha vazia: não é possível " + operacao);
        }
    }
}
