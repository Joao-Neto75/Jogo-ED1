package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.Combinacao;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;

import java.util.Objects;

/**
 * Varre linhas e colunas da matriz procurando 3 ou mais gemas iguais seguidas.
 * Requisitos: Lista Encadeada (resultado), Vetor ou Matriz (varredura).
 *
 * <p>Complexidade: O(linhas × colunas), cada célula é visitada uma vez por direção.
 * Formatos em L/T geram duas combinações (uma horizontal e uma vertical) que compartilham uma posição.
 */
public class BuscadorCombinacoes {

    /** Retorna todas as combinações do tabuleiro; lista vazia se não houver nenhuma. */
    public ListaEncadeada<Combinacao> buscar(Tabuleiro tabuleiro) {
        Objects.requireNonNull(tabuleiro, "tabuleiro não pode ser nulo");
        ListaEncadeada<Combinacao> encontradas = new ListaEncadeada<>();
        for (int linha = 0; linha < tabuleiro.getLinhas(); linha++) {
            varrer(tabuleiro, linha, 0, 0, 1, encontradas);
        }
        for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
            varrer(tabuleiro, 0, coluna, 1, 0, encontradas);
        }
        return encontradas;
    }

    public boolean existeCombinacao(Tabuleiro tabuleiro) {
        return !buscar(tabuleiro).estaVazia();
    }

    /**
     * Percorre uma linha ou coluna a partir de (linha, coluna) no sentido (passoLinha, passoColuna),
     * contando sequências do mesmo tipo e registrando as que atingem o tamanho mínimo.
     */
    private void varrer(Tabuleiro tabuleiro, int linha, int coluna, int passoLinha, int passoColuna,
                        ListaEncadeada<Combinacao> encontradas) {
        Posicao inicioSequencia = new Posicao(linha, coluna);
        int tamanhoSequencia = 0;
        Posicao atual = inicioSequencia;
        while (tabuleiro.estaDentro(atual)) {
            Gema gemaInicio = tabuleiro.obterGema(inicioSequencia);
            Gema gemaAtual = tabuleiro.obterGema(atual);
            if (gemaAtual != null && gemaAtual.mesmoTipo(gemaInicio)) {
                tamanhoSequencia++;
            } else {
                registrarSeValida(tabuleiro, inicioSequencia, tamanhoSequencia, passoLinha, passoColuna, encontradas);
                inicioSequencia = atual;
                tamanhoSequencia = gemaAtual == null ? 0 : 1;
            }
            atual = new Posicao(atual.linha() + passoLinha, atual.coluna() + passoColuna);
        }
        registrarSeValida(tabuleiro, inicioSequencia, tamanhoSequencia, passoLinha, passoColuna, encontradas);
    }

    private void registrarSeValida(Tabuleiro tabuleiro, Posicao inicio, int tamanho, int passoLinha, int passoColuna,
                                   ListaEncadeada<Combinacao> encontradas) {
        if (tamanho < Combinacao.TAMANHO_MINIMO) {
            return;
        }
        ListaEncadeada<Posicao> posicoes = new ListaEncadeada<>();
        for (int i = 0; i < tamanho; i++) {
            posicoes.adicionar(new Posicao(inicio.linha() + i * passoLinha, inicio.coluna() + i * passoColuna));
        }
        encontradas.adicionar(new Combinacao(tabuleiro.obterGema(inicio).getTipo(), posicoes));
    }
}
