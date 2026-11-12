package br.ed1.candymatch;

import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;

/** Monta tabuleiros de teste a partir de texto: uma String por linha, '.' = vazio (ver TipoGema.getSimbolo). */
public final class FabricaTabuleiro {

    private FabricaTabuleiro() {
    }

    public static Tabuleiro deTexto(String... linhas) {
        Tabuleiro tabuleiro = new Tabuleiro(linhas.length, linhas[0].length());
        for (int linha = 0; linha < linhas.length; linha++) {
            if (linhas[linha].length() != linhas[0].length()) {
                throw new IllegalArgumentException("Todas as linhas devem ter o mesmo tamanho");
            }
            for (int coluna = 0; coluna < linhas[linha].length(); coluna++) {
                char simbolo = linhas[linha].charAt(coluna);
                if (simbolo != '.') {
                    tabuleiro.definirGema(new Posicao(linha, coluna), new Gema(TipoGema.deSimbolo(simbolo)));
                }
            }
        }
        return tabuleiro;
    }

    /** Inverso de {@link #deTexto}, para comparar tabuleiros em asserts legíveis. */
    public static String paraTexto(Tabuleiro tabuleiro) {
        StringBuilder sb = new StringBuilder();
        for (int linha = 0; linha < tabuleiro.getLinhas(); linha++) {
            for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
                Gema gema = tabuleiro.obterGema(new Posicao(linha, coluna));
                sb.append(gema == null ? '.' : gema.getTipo().getSimbolo());
            }
            if (linha < tabuleiro.getLinhas() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
