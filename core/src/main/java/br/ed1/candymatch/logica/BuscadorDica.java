package br.ed1.candymatch.logica;

import br.ed1.candymatch.algoritmos.Buscador;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;

import java.util.Objects;
import java.util.Optional;

/**
 * Procura uma troca válida para sugerir ao jogador.
 * Requisitos: Algoritmo de Busca (busca sequencial sobre o vetor de todas as trocas possíveis), Vetor.
 * Também serve para detectar "sem jogadas possíveis" e reorganizar o tabuleiro.
 *
 * <p>Complexidade: O(T × L × C), onde T ≈ 2·L·C é o número de trocas candidatas e cada teste varre a matriz.
 */
public class BuscadorDica {

    private final BuscadorCombinacoes buscadorCombinacoes;

    public BuscadorDica(BuscadorCombinacoes buscadorCombinacoes) {
        this.buscadorCombinacoes = Objects.requireNonNull(buscadorCombinacoes, "buscadorCombinacoes não pode ser nulo");
    }

    /** Retorna a primeira jogada que gera combinação, ou vazio se não houver nenhuma. */
    public Optional<Jogada> buscar(Tabuleiro tabuleiro) {
        Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        // Testa numa cópia para nunca alterar o tabuleiro real, nem temporariamente.
        Tabuleiro rascunho = tabuleiro.copiar();
        Jogada[] candidatas = gerarTrocasCandidatas(rascunho);
        int indice = Buscador.buscaSequencial(candidatas, jogada -> geraCombinacao(rascunho, jogada));
        return indice == Buscador.NAO_ENCONTRADO ? Optional.empty() : Optional.of(candidatas[indice]);
    }

    public boolean existeJogadaPossivel(Tabuleiro tabuleiro) {
        return buscar(tabuleiro).isPresent();
    }

    /** Cada célula com a vizinha da direita e a de baixo: cobre todos os pares adjacentes sem repetir. */
    private Jogada[] gerarTrocasCandidatas(Tabuleiro tabuleiro) {
        int linhas = tabuleiro.getLinhas();
        int colunas = tabuleiro.getColunas();
        Jogada[] candidatas = new Jogada[linhas * (colunas - 1) + (linhas - 1) * colunas];
        int quantidade = 0;
        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                Posicao atual = new Posicao(linha, coluna);
                if (coluna + 1 < colunas) {
                    candidatas[quantidade++] = new Jogada(atual, new Posicao(linha, coluna + 1));
                }
                if (linha + 1 < linhas) {
                    candidatas[quantidade++] = new Jogada(atual, new Posicao(linha + 1, coluna));
                }
            }
        }
        return candidatas;
    }

    private boolean geraCombinacao(Tabuleiro rascunho, Jogada jogada) {
        if (rascunho.estaVazia(jogada.origem()) || rascunho.estaVazia(jogada.destino())) {
            return false;
        }
        rascunho.trocar(jogada.origem(), jogada.destino());
        boolean gera = buscadorCombinacoes.existeCombinacao(rascunho);
        rascunho.trocar(jogada.origem(), jogada.destino());
        return gera;
    }
}
