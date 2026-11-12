package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;

import java.util.Objects;

/**
 * Faz as gemas caírem, coluna a coluna, ocupando as células vazias abaixo delas.
 * Requisito: Fila (cada queda vira um evento para a animação).
 *
 * <p>Complexidade: O(linhas × colunas). Ao final, os vazios de cada coluna ficam todos no topo.
 */
public class GerenciadorGravidade {

    public void aplicar(Tabuleiro tabuleiro, Fila<EventoTabuleiro> eventos) {
        Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        Objects.requireNonNull(eventos, "fila de eventos não pode ser nula");
        for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
            // Percorre de baixo para cima: "destino" aponta sempre para o vazio mais baixo ainda não ocupado.
            int destino = tabuleiro.getLinhas() - 1;
            for (int linha = tabuleiro.getLinhas() - 1; linha >= 0; linha--) {
                Posicao origem = new Posicao(linha, coluna);
                Gema gema = tabuleiro.obterGema(origem);
                if (gema == null) {
                    continue;
                }
                if (linha != destino) {
                    Posicao novaPosicao = new Posicao(destino, coluna);
                    tabuleiro.removerGema(origem);
                    tabuleiro.definirGema(novaPosicao, gema);
                    eventos.enfileirar(EventoTabuleiro.queda(origem, novaPosicao, gema));
                }
                destino--;
            }
        }
    }
}
