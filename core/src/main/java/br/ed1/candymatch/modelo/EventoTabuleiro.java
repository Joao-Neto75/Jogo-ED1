package br.ed1.candymatch.modelo;

import java.util.Objects;

/**
 * Mudança ocorrida no tabuleiro, produzida pela lógica e consumida em ordem pela animação (Fila).
 * Os campos usados dependem do tipo; prefira os métodos de fábrica.
 */
public record EventoTabuleiro(Tipo tipo, Posicao origem, Posicao destino, Gema gema) {

    public enum Tipo {
        TROCA,
        REMOCAO,
        QUEDA,
        CRIACAO,
        /** Tabuleiro reorganizado por falta de jogadas: a UI deve ressincronizar com a lógica. */
        EMBARALHAMENTO
    }

    public EventoTabuleiro {
        Objects.requireNonNull(tipo, "tipo do evento não pode ser nulo");
    }

    public static EventoTabuleiro troca(Posicao a, Posicao b) {
        return new EventoTabuleiro(Tipo.TROCA, exigir(a), exigir(b), null);
    }

    public static EventoTabuleiro remocao(Posicao posicao, Gema gema) {
        return new EventoTabuleiro(Tipo.REMOCAO, exigir(posicao), null, gema);
    }

    public static EventoTabuleiro queda(Posicao de, Posicao para, Gema gema) {
        return new EventoTabuleiro(Tipo.QUEDA, exigir(de), exigir(para), gema);
    }

    public static EventoTabuleiro criacao(Posicao posicao, Gema gema) {
        return new EventoTabuleiro(Tipo.CRIACAO, null, exigir(posicao), gema);
    }

    public static EventoTabuleiro embaralhamento() {
        return new EventoTabuleiro(Tipo.EMBARALHAMENTO, null, null, null);
    }

    private static Posicao exigir(Posicao posicao) {
        return Objects.requireNonNull(posicao, "posição do evento não pode ser nula");
    }
}
