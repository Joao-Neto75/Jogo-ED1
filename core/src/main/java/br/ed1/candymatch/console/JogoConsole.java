package br.ed1.candymatch.console;

import br.ed1.candymatch.arquivos.ArquivoNivel;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * Versão de console (Fase 2): joga usando apenas a lógica, sem libGDX gráfico.
 * Útil para demonstrar o ciclo de combinações/cascata e depurar regras.
 * Rode com {@code ./gradlew :core:runConsole}.
 */
public final class JogoConsole {

    private static final String PASTA_NIVEIS = "assets/niveis";

    private JogoConsole() {
    }

    public static void main(String[] args) {
        ArquivoNivel carregador = new ArquivoNivel(new FileHandle(new File(PASTA_NIVEIS)));
        int numeroNivel = args.length > 0 ? lerInteiro(args[0], 1) : 1;
        Nivel nivel;
        try {
            nivel = carregador.carregar(numeroNivel);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao carregar nível: " + e.getMessage());
            return;
        }
        Partida partida = Partida.criar(nivel, new Random());
        Scanner entrada = new Scanner(System.in);

        System.out.println("CandyMatch - console. Comandos: 'l1 c1 l2 c2' (troca), 'd' (dica), 'u' (desfazer), 'q' (sair)");
        while (!partida.estaFinalizada()) {
            imprimir(partida);
            System.out.print("> ");
            if (!entrada.hasNextLine()) {
                return;
            }
            String comando = entrada.nextLine().trim();
            if (comando.equalsIgnoreCase("q")) {
                return;
            }
            executar(partida, comando);
            partida.getEventos().limpar();
        }
        imprimir(partida);
        System.out.println(partida.venceu() ? "VITÓRIA!" : "Fim das jogadas. Derrota.");
    }

    private static void executar(Partida partida, String comando) {
        if (comando.equalsIgnoreCase("d")) {
            Optional<Jogada> dica = partida.obterDica();
            System.out.println(dica.map(j -> "Dica: troque " + texto(j.origem()) + " com " + texto(j.destino()))
                    .orElse("Sem jogadas possíveis"));
            return;
        }
        if (comando.equalsIgnoreCase("u")) {
            System.out.println(partida.desfazerJogada() ? "Jogada desfeita" : "Nada para desfazer");
            return;
        }
        String[] partes = comando.split("\\s+");
        if (partes.length != 4) {
            System.out.println("Comando inválido");
            return;
        }
        try {
            Jogada jogada = new Jogada(
                    new Posicao(Integer.parseInt(partes[0]), Integer.parseInt(partes[1])),
                    new Posicao(Integer.parseInt(partes[2]), Integer.parseInt(partes[3])));
            if (partida.realizarJogada(jogada)) {
                System.out.println("+" + partida.getPontosUltimaJogada() + " pontos"
                        + (partida.getMaiorCascataUltimaJogada() > 1
                        ? " (cascata x" + partida.getMaiorCascataUltimaJogada() + ")" : ""));
            } else {
                System.out.println("Troca não forma combinação");
            }
        } catch (NumberFormatException e) {
            System.out.println("Use números: linha coluna linha coluna");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void imprimir(Partida partida) {
        Tabuleiro t = partida.getTabuleiro();
        System.out.println();
        System.out.println("Nível " + partida.getNivel().getNumero() + " | Pontos " + partida.getPontos() + "/"
                + partida.getNivel().getMetaPontos() + " | Jogadas " + partida.getJogadasRestantes());
        StringBuilder cabecalho = new StringBuilder("   ");
        for (int coluna = 0; coluna < t.getColunas(); coluna++) {
            cabecalho.append(coluna).append(' ');
        }
        System.out.println(cabecalho);
        String[] linhas = t.toString().split("\n");
        for (int linha = 0; linha < linhas.length; linha++) {
            System.out.println(linha + "  " + linhas[linha]);
        }
    }

    private static String texto(Posicao p) {
        return "(" + p.linha() + "," + p.coluna() + ")";
    }

    private static int lerInteiro(String texto, int padrao) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
