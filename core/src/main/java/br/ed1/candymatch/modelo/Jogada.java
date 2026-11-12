package br.ed1.candymatch.modelo;

import java.util.Objects;

/** Troca solicitada pelo jogador (ou sugerida pela dica) entre duas posições. */
public record Jogada(Posicao origem, Posicao destino) {

    public Jogada {
        Objects.requireNonNull(origem, "origem da jogada não pode ser nula");
        Objects.requireNonNull(destino, "destino da jogada não pode ser nulo");
    }
}
