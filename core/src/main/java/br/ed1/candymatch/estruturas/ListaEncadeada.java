package br.ed1.candymatch.estruturas;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Lista simplesmente encadeada genérica, com ponteiros para início e fim.
 * Requisitos: Lista Encadeada, Generic.
 * Uso no jogo: combinações encontradas em cada rodada (BuscadorCombinacoes) e ranking lido do arquivo.
 *
 * <p>Complexidade: adicionar e adicionarNoInicio O(1); obter, remover, removerElemento e contem O(n);
 * paraVetor e deVetor O(n).
 *
 * <p>Não aceita elementos nulos, para que buscas por igualdade nunca precisem tratar null.
 */
public class ListaEncadeada<T> implements Iterable<T> {

    private No<T> inicio;
    private No<T> fim;
    private int tamanho;

    public static <T> ListaEncadeada<T> deVetor(T[] vetor) {
        Objects.requireNonNull(vetor, "vetor não pode ser nulo");
        ListaEncadeada<T> lista = new ListaEncadeada<>();
        for (T elemento : vetor) {
            lista.adicionar(elemento);
        }
        return lista;
    }

    public void adicionar(T elemento) {
        No<T> novo = new No<>(exigirNaoNulo(elemento));
        if (inicio == null) {
            inicio = novo;
        } else {
            fim.setProximo(novo);
        }
        fim = novo;
        tamanho++;
    }

    public void adicionarNoInicio(T elemento) {
        No<T> novo = new No<>(exigirNaoNulo(elemento));
        novo.setProximo(inicio);
        inicio = novo;
        if (fim == null) {
            fim = novo;
        }
        tamanho++;
    }

    public T obter(int indice) {
        return noNaPosicao(indice).getDado();
    }

    public T remover(int indice) {
        validarIndice(indice);
        No<T> removido;
        if (indice == 0) {
            removido = inicio;
            inicio = inicio.getProximo();
            if (inicio == null) {
                fim = null;
            }
        } else {
            No<T> anterior = noNaPosicao(indice - 1);
            removido = anterior.getProximo();
            anterior.setProximo(removido.getProximo());
            if (removido == fim) {
                fim = anterior;
            }
        }
        tamanho--;
        return removido.getDado();
    }

    /** Remove a primeira ocorrência do elemento; retorna false se não encontrado. */
    public boolean removerElemento(T elemento) {
        No<T> anterior = null;
        No<T> atual = inicio;
        while (atual != null) {
            if (atual.getDado().equals(elemento)) {
                if (anterior == null) {
                    inicio = atual.getProximo();
                } else {
                    anterior.setProximo(atual.getProximo());
                }
                if (atual == fim) {
                    fim = anterior;
                }
                tamanho--;
                return true;
            }
            anterior = atual;
            atual = atual.getProximo();
        }
        return false;
    }

    public boolean contem(T elemento) {
        for (T atual : this) {
            if (atual.equals(elemento)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copia os elementos para um vetor. Necessário para ordenar e aplicar busca binária,
     * que exigem acesso por índice em O(1). Ex.: {@code lista.paraVetor(RegistroPontuacao[]::new)}.
     */
    public T[] paraVetor(IntFunction<T[]> criarVetor) {
        Objects.requireNonNull(criarVetor, "criarVetor não pode ser nulo");
        T[] vetor = criarVetor.apply(tamanho);
        int i = 0;
        for (T elemento : this) {
            vetor[i++] = elemento;
        }
        return vetor;
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

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private No<T> atual = inicio;

            @Override
            public boolean hasNext() {
                return atual != null;
            }

            @Override
            public T next() {
                if (atual == null) {
                    throw new NoSuchElementException("Fim da lista encadeada");
                }
                T dado = atual.getDado();
                atual = atual.getProximo();
                return dado;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (No<T> atual = inicio; atual != null; atual = atual.getProximo()) {
            sb.append(atual.getDado());
            if (atual.getProximo() != null) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }

    private No<T> noNaPosicao(int indice) {
        validarIndice(indice);
        No<T> atual = inicio;
        for (int i = 0; i < indice; i++) {
            atual = atual.getProximo();
        }
        return atual;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException(
                    "Índice " + indice + " fora dos limites da lista (tamanho " + tamanho + ")");
        }
    }

    private static <T> T exigirNaoNulo(T elemento) {
        return Objects.requireNonNull(elemento, "ListaEncadeada não aceita elementos nulos");
    }
}
