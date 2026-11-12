package br.ed1.candymatch.algoritmos;

import java.util.Comparator;
import java.util.Objects;

/**
 * Ordenação genérica por MergeSort.
 * Requisitos: Algoritmo de Ordenação, Generic.
 * Uso no jogo: ranking por pontuação (exibição) e por nome (pré-requisito da busca binária).
 *
 * <p>Complexidade: O(n log n) em todos os casos; memória auxiliar O(n). É estável, então
 * jogadores com a mesma pontuação mantêm a ordem anterior.
 */
public final class Ordenador {

    private Ordenador() {
    }

    public static <T extends Comparable<? super T>> void ordenar(T[] vetor) {
        ordenar(vetor, Comparator.naturalOrder());
    }

    public static <T> void ordenar(T[] vetor, Comparator<? super T> comparador) {
        Objects.requireNonNull(vetor, "vetor a ordenar não pode ser nulo");
        Objects.requireNonNull(comparador, "comparador não pode ser nulo");
        if (vetor.length < 2) {
            return;
        }
        // Um único vetor auxiliar para toda a recursão evita alocar O(n log n) de memória.
        T[] auxiliar = vetor.clone();
        mergeSort(vetor, auxiliar, 0, vetor.length - 1, comparador);
    }

    private static <T> void mergeSort(T[] vetor, T[] auxiliar, int inicio, int fim, Comparator<? super T> comparador) {
        if (inicio >= fim) {
            return;
        }
        int meio = inicio + (fim - inicio) / 2;
        mergeSort(vetor, auxiliar, inicio, meio, comparador);
        mergeSort(vetor, auxiliar, meio + 1, fim, comparador);
        intercalar(vetor, auxiliar, inicio, meio, fim, comparador);
    }

    private static <T> void intercalar(T[] vetor, T[] auxiliar, int inicio, int meio, int fim,
                                       Comparator<? super T> comparador) {
        System.arraycopy(vetor, inicio, auxiliar, inicio, fim - inicio + 1);
        int esquerda = inicio;
        int direita = meio + 1;
        for (int destino = inicio; destino <= fim; destino++) {
            if (esquerda > meio) {
                vetor[destino] = auxiliar[direita++];
            } else if (direita > fim) {
                vetor[destino] = auxiliar[esquerda++];
            } else if (comparador.compare(auxiliar[direita], auxiliar[esquerda]) < 0) {
                vetor[destino] = auxiliar[direita++];
            } else {
                // Em empate, pega da esquerda: é isso que garante a estabilidade.
                vetor[destino] = auxiliar[esquerda++];
            }
        }
    }
}
