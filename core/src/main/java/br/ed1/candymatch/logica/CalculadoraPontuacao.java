package br.ed1.candymatch.logica;

import br.ed1.candymatch.modelo.Combinacao;

import java.util.Objects;

/** Calcula os pontos de uma combinação, considerando o tamanho e o nível da cascata. */
public class CalculadoraPontuacao {

    public static final int PONTOS_POR_GEMA = 10;
    public static final int BONUS_POR_GEMA_EXTRA = 20;

    /**
     * @param nivelCascata 1 para a combinação gerada pela troca do jogador, 2 para a primeira
     *                     cascata, e assim por diante (funciona como multiplicador)
     */
    public int calcular(Combinacao combinacao, int nivelCascata) {
        Objects.requireNonNull(combinacao, "combinação não pode ser nula");
        if (nivelCascata < 1) {
            throw new IllegalArgumentException("Nível de cascata deve ser >= 1: " + nivelCascata);
        }
        int extras = combinacao.tamanho() - Combinacao.TAMANHO_MINIMO;
        return (combinacao.tamanho() * PONTOS_POR_GEMA + extras * BONUS_POR_GEMA_EXTRA) * nivelCascata;
    }
}
