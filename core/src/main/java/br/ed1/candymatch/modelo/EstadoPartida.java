package br.ed1.candymatch.modelo;

import java.util.Objects;

/**
 * Fotografia da partida em um instante: usada pelo desfazer (Pilha) e pelo salvamento (Arquivos).
 * O tabuleiro deve ser uma cópia ({@link Tabuleiro#copiar()}), nunca o tabuleiro em uso.
 */
public record EstadoPartida(Tabuleiro tabuleiro, int numeroNivel, int pontos, int jogadasRestantes) {

    public EstadoPartida {
        Objects.requireNonNull(tabuleiro, "tabuleiro do estado não pode ser nulo");
        if (pontos < 0 || jogadasRestantes < 0) {
            throw new IllegalArgumentException(
                    "Estado inválido: pontos=" + pontos + ", jogadasRestantes=" + jogadasRestantes);
        }
    }
}
