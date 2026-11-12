package br.ed1.candymatch.algoritmos;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Algoritmos de busca genéricos.
 * Requisitos: Algoritmo de Busca, Generic.
 * Uso no jogo: busca binária do recorde de um jogador no ranking ordenado por nome;
 * busca sequencial da primeira jogada válida na dica (BuscadorDica).
 *
 * <p>Complexidade: buscaBinaria O(log n) (exige vetor ordenado pelo mesmo comparador);
 * buscaSequencial O(n).
 */
public final class Buscador {

    public static final int NAO_ENCONTRADO = -1;

    private Buscador() {
    }

    /** Retorna o índice do alvo ou {@link #NAO_ENCONTRADO}. */
    public static <T> int buscaBinaria(T[] vetorOrdenado, T alvo, Comparator<? super T> comparador) {
        Objects.requireNonNull(vetorOrdenado, "vetor não pode ser nulo");
        Objects.requireNonNull(alvo, "alvo da busca não pode ser nulo");
        Objects.requireNonNull(comparador, "comparador não pode ser nulo");
        int inicio = 0;
        int fim = vetorOrdenado.length - 1;
        while (inicio <= fim) {
            // Evita estouro de int que (inicio + fim) / 2 teria em vetores enormes.
            int meio = inicio + (fim - inicio) / 2;
            int comparacao = comparador.compare(vetorOrdenado[meio], alvo);
            if (comparacao == 0) {
                return meio;
            }
            if (comparacao < 0) {
                inicio = meio + 1;
            } else {
                fim = meio - 1;
            }
        }
        return NAO_ENCONTRADO;
    }

    /** Retorna o índice do primeiro elemento que satisfaz o critério ou {@link #NAO_ENCONTRADO}. */
    public static <T> int buscaSequencial(T[] vetor, Predicate<? super T> criterio) {
        Objects.requireNonNull(vetor, "vetor não pode ser nulo");
        Objects.requireNonNull(criterio, "critério de busca não pode ser nulo");
        for (int i = 0; i < vetor.length; i++) {
            if (criterio.test(vetor[i])) {
                return i;
            }
        }
        return NAO_ENCONTRADO;
    }
}
