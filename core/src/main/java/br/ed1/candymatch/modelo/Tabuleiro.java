package br.ed1.candymatch.modelo;

import java.util.Objects;

/**
 * Tabuleiro do jogo, representado por uma matriz de gemas.
 * Requisito: Vetor ou Matriz ({@code Gema[8][8]}).
 *
 * <p>Célula nula = espaço vazio (existe entre a remoção de uma combinação e o refill).
 * Esta classe só guarda e manipula a matriz; as regras ficam no pacote {@code logica}.
 */
public class Tabuleiro {

    public static final int TAMANHO_PADRAO = 8;

    private final Gema[][] grade;

    public Tabuleiro() {
        this(TAMANHO_PADRAO, TAMANHO_PADRAO);
    }

    public Tabuleiro(int linhas, int colunas) {
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Dimensões inválidas: " + linhas + "x" + colunas);
        }
        this.grade = new Gema[linhas][colunas];
    }

    public int getLinhas() {
        return grade.length;
    }

    public int getColunas() {
        return grade[0].length;
    }

    public boolean estaDentro(Posicao posicao) {
        return posicao != null
                && posicao.linha() >= 0 && posicao.linha() < getLinhas()
                && posicao.coluna() >= 0 && posicao.coluna() < getColunas();
    }

    /** Retorna a gema na posição, ou null se a célula estiver vazia. */
    public Gema obterGema(Posicao posicao) {
        validar(posicao);
        return grade[posicao.linha()][posicao.coluna()];
    }

    public void definirGema(Posicao posicao, Gema gema) {
        validar(posicao);
        grade[posicao.linha()][posicao.coluna()] =
                Objects.requireNonNull(gema, "use removerGema para esvaziar uma célula");
    }

    /** Esvazia a célula e retorna a gema que estava nela (ou null). */
    public Gema removerGema(Posicao posicao) {
        validar(posicao);
        Gema removida = grade[posicao.linha()][posicao.coluna()];
        grade[posicao.linha()][posicao.coluna()] = null;
        return removida;
    }

    public boolean estaVazia(Posicao posicao) {
        return obterGema(posicao) == null;
    }

    /** Troca duas gemas adjacentes. Não verifica se a troca gera combinação. */
    public void trocar(Posicao a, Posicao b) {
        validar(a);
        validar(b);
        if (!a.ehAdjacente(b)) {
            throw new IllegalArgumentException("Posições não adjacentes: " + a + " e " + b);
        }
        Gema temporaria = grade[a.linha()][a.coluna()];
        grade[a.linha()][a.coluna()] = grade[b.linha()][b.coluna()];
        grade[b.linha()][b.coluna()] = temporaria;
    }

    /** Cópia independente da matriz, usada nos snapshots do desfazer e no salvamento. */
    public Tabuleiro copiar() {
        Tabuleiro copia = new Tabuleiro(getLinhas(), getColunas());
        for (int linha = 0; linha < getLinhas(); linha++) {
            System.arraycopy(grade[linha], 0, copia.grade[linha], 0, getColunas());
        }
        return copia;
    }

    /**
     * Sobrescreve esta matriz com o conteúdo de outra de mesmas dimensões.
     * Necessário porque a Partida mantém a mesma instância de tabuleiro (a UI guarda a referência).
     */
    public void restaurar(Tabuleiro origem) {
        Objects.requireNonNull(origem, "tabuleiro de origem não pode ser nulo");
        if (origem.getLinhas() != getLinhas() || origem.getColunas() != getColunas()) {
            throw new IllegalArgumentException("Dimensões diferentes: " + origem.getLinhas() + "x"
                    + origem.getColunas() + " em vez de " + getLinhas() + "x" + getColunas());
        }
        for (int linha = 0; linha < getLinhas(); linha++) {
            System.arraycopy(origem.grade[linha], 0, grade[linha], 0, getColunas());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Gema[] linha : grade) {
            for (Gema gema : linha) {
                sb.append(gema == null ? '.' : gema.getTipo().getSimbolo()).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private void validar(Posicao posicao) {
        Objects.requireNonNull(posicao, "posição não pode ser nula");
        if (!estaDentro(posicao)) {
            throw new IndexOutOfBoundsException(
                    "Posição " + posicao + " fora do tabuleiro " + getLinhas() + "x" + getColunas());
        }
    }
}
