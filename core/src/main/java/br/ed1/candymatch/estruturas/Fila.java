package br.ed1.candymatch.estruturas;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Fila genérica (FIFO) encadeada, com ponteiros para início e fim.
 * Requisitos: Fila, Generic.
 * Uso no jogo: eventos do tabuleiro (troca, remoção, queda, criação) são enfileirados pela lógica
 * e consumidos em ordem pelo GerenciadorAnimacao, o que mantém as cascatas na sequência correta.
 *
 * <p>Complexidade: enfileirar, desenfileirar, espiar, tamanho e estaVazia O(1).
 */
public class Fila<T> {

    private No<T> inicio;
    private No<T> fim;
    private int tamanho;

    public void enfileirar(T elemento) {
        No<T> novo = new No<>(Objects.requireNonNull(elemento, "Fila não aceita elementos nulos"));
        if (fim == null) {
            inicio = novo;
        } else {
            fim.setProximo(novo);
        }
        fim = novo;
        tamanho++;
    }

    public T desenfileirar() {
        exigirNaoVazia("desenfileirar");
        T dado = inicio.getDado();
        inicio = inicio.getProximo();
        if (inicio == null) {
            fim = null;
        }
        tamanho--;
        return dado;
    }

    /** Retorna o primeiro elemento sem removê-lo. */
    public T espiar() {
        exigirNaoVazia("espiar");
        return inicio.getDado();
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public void limpar() {
        inicio = null;
        fim = null;
        tamanho = 0;
    }

    private void exigirNaoVazia(String operacao) {
        if (estaVazia()) {
            throw new NoSuchElementException("Fila vazia: não é possível " + operacao);
        }
    }
}
