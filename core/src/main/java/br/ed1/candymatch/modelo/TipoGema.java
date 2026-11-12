package br.ed1.candymatch.modelo;

/** Cores possíveis de gema. Cada nível define quais delas aparecem (ver {@link Nivel}). */
public enum TipoGema {
    VERMELHA('V'),
    AZUL('A'),
    VERDE('E'),
    AMARELA('M'),
    ROXA('R'),
    LARANJA('L');

    /** Símbolo único usado na impressão do tabuleiro no console e nos testes (iniciais colidem). */
    private final char simbolo;

    TipoGema(char simbolo) {
        this.simbolo = simbolo;
    }

    public char getSimbolo() {
        return simbolo;
    }

    /** @throws IllegalArgumentException se nenhum tipo usar o símbolo */
    public static TipoGema deSimbolo(char simbolo) {
        char normalizado = Character.toUpperCase(simbolo);
        for (TipoGema tipo : values()) {
            if (tipo.simbolo == normalizado) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Símbolo de gema desconhecido: '" + simbolo + "'");
    }
}
