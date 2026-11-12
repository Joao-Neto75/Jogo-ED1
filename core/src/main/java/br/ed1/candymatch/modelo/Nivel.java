package br.ed1.candymatch.modelo;

import java.util.Objects;

/**
 * Configuração de um nível, lida de {@code assets/niveis/nivelN.txt}.
 * Requisito: Vetor (tipos de gema disponíveis).
 */
public final class Nivel {

    private final int numero;
    private final int metaPontos;
    private final int limiteJogadas;
    private final TipoGema[] tiposDisponiveis;

    public Nivel(int numero, int metaPontos, int limiteJogadas, TipoGema[] tiposDisponiveis) {
        if (numero <= 0) {
            throw new IllegalArgumentException("Número do nível deve ser positivo: " + numero);
        }
        if (metaPontos <= 0) {
            throw new IllegalArgumentException("Meta de pontos deve ser positiva: " + metaPontos);
        }
        if (limiteJogadas <= 0) {
            throw new IllegalArgumentException("Limite de jogadas deve ser positivo: " + limiteJogadas);
        }
        Objects.requireNonNull(tiposDisponiveis, "tipos disponíveis não podem ser nulos");
        // Com menos de 3 cores o tabuleiro inicial sem combinações fica praticamente impossível de gerar.
        if (tiposDisponiveis.length < 3) {
            throw new IllegalArgumentException("Nível precisa de pelo menos 3 tipos de gema");
        }
        this.numero = numero;
        this.metaPontos = metaPontos;
        this.limiteJogadas = limiteJogadas;
        this.tiposDisponiveis = tiposDisponiveis.clone();
    }

    public int getNumero() {
        return numero;
    }

    public int getMetaPontos() {
        return metaPontos;
    }

    public int getLimiteJogadas() {
        return limiteJogadas;
    }

    /** Retorna uma cópia, para que o nível continue imutável. */
    public TipoGema[] getTiposDisponiveis() {
        return tiposDisponiveis.clone();
    }
}
