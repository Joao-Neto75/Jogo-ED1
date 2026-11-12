package br.ed1.candymatch.modelo;

import java.util.Objects;

/** Coordenada de uma célula do tabuleiro (linha 0 = topo). */
public record Posicao(int linha, int coluna) {

    /** Adjacente = vizinha na horizontal ou vertical (diagonal não conta). */
    public boolean ehAdjacente(Posicao outra) {
        Objects.requireNonNull(outra, "posição para comparar não pode ser nula");
        return Math.abs(linha - outra.linha) + Math.abs(coluna - outra.coluna) == 1;
    }
}
