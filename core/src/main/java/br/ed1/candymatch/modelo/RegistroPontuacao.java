package br.ed1.candymatch.modelo;

import java.util.Comparator;
import java.util.Objects;

/**
 * Linha do ranking (nome do jogador e pontuação).
 * Requisitos: Generic (Comparable/Comparator usados pelo Ordenador e pelo Buscador).
 *
 * <p>A ordem natural é por nome, pois é a ordem exigida pela busca binária do recorde de um jogador.
 */
public final class RegistroPontuacao implements Comparable<RegistroPontuacao> {

    public static final Comparator<RegistroPontuacao> POR_NOME = Comparator.naturalOrder();

    public static final Comparator<RegistroPontuacao> POR_PONTOS_DECRESCENTE =
            Comparator.comparingInt(RegistroPontuacao::getPontos).reversed();

    private final String nome;
    private final int pontos;

    public RegistroPontuacao(String nome, int pontos) {
        Objects.requireNonNull(nome, "nome do jogador não pode ser nulo");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("nome do jogador não pode ser vazio");
        }
        if (pontos < 0) {
            throw new IllegalArgumentException("pontuação não pode ser negativa: " + pontos);
        }
        this.nome = nome.trim();
        this.pontos = pontos;
    }

    public String getNome() {
        return nome;
    }

    public int getPontos() {
        return pontos;
    }

    @Override
    public int compareTo(RegistroPontuacao outro) {
        return nome.compareToIgnoreCase(outro.nome);
    }

    @Override
    public String toString() {
        return nome + " - " + pontos;
    }
}
