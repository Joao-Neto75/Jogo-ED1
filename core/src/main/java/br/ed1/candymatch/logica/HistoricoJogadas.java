package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.Pilha;
import br.ed1.candymatch.modelo.EstadoPartida;

import java.util.Objects;

/**
 * Botão "Desfazer jogada": guarda o estado anterior a cada jogada válida.
 * Requisito: Pilha (o último estado salvo é o primeiro a ser restaurado).
 *
 * <p>Complexidade: registrar e desfazer O(1).
 */
public class HistoricoJogadas {

    private final Pilha<EstadoPartida> historico = new Pilha<>();

    /** @param estadoAntesDaJogada o tabuleiro dele já deve ser uma cópia */
    public void registrar(EstadoPartida estadoAntesDaJogada) {
        historico.empilhar(Objects.requireNonNull(estadoAntesDaJogada, "estado a registrar não pode ser nulo"));
    }

    /** @throws IllegalStateException se não houver jogada para desfazer */
    public EstadoPartida desfazer() {
        if (historico.estaVazia()) {
            throw new IllegalStateException("Não há jogada para desfazer");
        }
        return historico.desempilhar();
    }

    public boolean podeDesfazer() {
        return !historico.estaVazia();
    }

    public int quantidadeDisponivel() {
        return historico.tamanho();
    }

    public void limpar() {
        historico.limpar();
    }
}
