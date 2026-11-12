package br.ed1.candymatch.visual;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.TipoGema;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A animação não usa OpenGL fora de desenhar(), então a reprodução da fila pode ser testada no JUnit. */
class GerenciadorAnimacaoTest {

    private static final float PASSO = 1f / 60f;
    private static final int LIMITE_QUADROS = 10_000;

    private static void animarAteOFim(GerenciadorAnimacao animacao) {
        int quadros = 0;
        while (animacao.estaAnimando()) {
            animacao.atualizar(PASSO);
            if (++quadros > LIMITE_QUADROS) {
                throw new AssertionError("Animação não terminou");
            }
        }
    }

    @Test
    void reproduzirEventosDeixaVisualIgualAoTabuleiroReal() {
        Nivel nivel = new Nivel(1, 1_000_000, 40, new TipoGema[]{
                TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE, TipoGema.AMARELA});
        Random aleatorio = new Random(11);
        Partida partida = Partida.criar(nivel, aleatorio);
        int[] etapasRemocao = {0};
        GerenciadorAnimacao animacao = new GerenciadorAnimacao(partida.getTabuleiro(),
                (tipo, rodada) -> {
                    if (tipo == EventoTabuleiro.Tipo.REMOCAO) {
                        etapasRemocao[0]++;
                    }
                });

        int jogadasValidas = 0;
        for (int i = 0; i < 40; i++) {
            // Alterna jogadas da dica (sempre válidas) com trocas aleatórias, que podem ser inválidas (ida e volta).
            Jogada jogada;
            if (i % 3 == 2) {
                int linha = aleatorio.nextInt(8);
                int coluna = aleatorio.nextInt(7);
                jogada = new Jogada(new Posicao(linha, coluna), new Posicao(linha, coluna + 1));
            } else {
                jogada = partida.obterDica().orElseThrow();
            }
            if (partida.realizarJogada(jogada)) {
                jogadasValidas++;
            }
            animacao.receber(partida.getEventos());
            assertTrue(partida.getEventos().estaVazia());
            animarAteOFim(animacao);
            assertEquals(FabricaTabuleiro.paraTexto(partida.getTabuleiro()),
                    FabricaTabuleiro.paraTexto(animacao.getVisual()), "divergiu na jogada " + i);
            if (partida.estaFinalizada()) {
                break;
            }
        }
        assertTrue(etapasRemocao[0] >= jogadasValidas, "toda jogada válida deve ter ao menos uma etapa de remoção");
    }

    @Test
    void trocaInvalidaAnimaIdaEVoltaSemAlterarVisual() {
        Nivel nivel = new Nivel(1, 1000, 10, new TipoGema[]{TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE});
        Partida partida = Partida.criar(nivel, new Random(5));
        GerenciadorAnimacao animacao = new GerenciadorAnimacao(partida.getTabuleiro(), (tipo, rodada) -> { });
        String antes = FabricaTabuleiro.paraTexto(partida.getTabuleiro());

        Jogada invalida = null;
        for (int coluna = 0; coluna < 7 && invalida == null; coluna++) {
            Jogada candidata = new Jogada(new Posicao(0, coluna), new Posicao(0, coluna + 1));
            if (!partida.realizarJogada(candidata)) {
                invalida = candidata;
            } else {
                partida.desfazerJogada();
            }
        }
        assertFalse(invalida == null, "fixture deveria ter alguma troca inválida na linha 0");
        animacao.sincronizar();
        partida.realizarJogada(invalida);
        animacao.receber(partida.getEventos());
        assertTrue(animacao.estaAnimando());
        animarAteOFim(animacao);
        assertEquals(antes, FabricaTabuleiro.paraTexto(animacao.getVisual()));
    }

    @Test
    void sincronizarDescartaPendentesECopiaTabuleiro() {
        Nivel nivel = new Nivel(1, 1_000_000, 10, new TipoGema[]{TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE});
        Partida partida = Partida.criar(nivel, new Random(2));
        GerenciadorAnimacao animacao = new GerenciadorAnimacao(partida.getTabuleiro(), (tipo, rodada) -> { });
        partida.realizarJogada(partida.obterDica().orElseThrow());
        animacao.receber(partida.getEventos());
        animacao.atualizar(PASSO);
        partida.desfazerJogada();
        animacao.sincronizar();
        assertFalse(animacao.estaAnimando());
        assertEquals(FabricaTabuleiro.paraTexto(partida.getTabuleiro()), FabricaTabuleiro.paraTexto(animacao.getVisual()));
    }
}
