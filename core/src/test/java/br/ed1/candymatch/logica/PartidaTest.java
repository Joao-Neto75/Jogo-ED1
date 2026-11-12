package br.ed1.candymatch.logica;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.modelo.EstadoPartida;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidaTest {

    private static final TipoGema[] TIPOS = {
            TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE, TipoGema.AMARELA, TipoGema.ROXA};

    private static Nivel nivel(int meta, int jogadas) {
        return new Nivel(1, meta, jogadas, TIPOS);
    }

    /** Trocar (0,2)↔(1,2) traz o V de baixo e forma VVVV na linha 0; (7,0)↔(7,1) não forma nada. */
    private static Partida partidaComJogadaConhecida(int meta, int jogadas) {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VVAVEMRA",
                "AEVMRAEM",
                "MRERAMVE",
                "EAMVEREA",
                "RMAERVMR",
                "AEREMAEV",
                "MVMARMRA",
                "EREMVEAM");
        Nivel n = nivel(meta, jogadas);
        Random aleatorio = new Random(42);
        BuscadorCombinacoes buscador = new BuscadorCombinacoes();
        return new Partida(n, t, buscador, new Gravidade(),
                new PreenchedorTabuleiro(n.getTiposDisponiveis(), aleatorio), new CalculadoraPontuacao(),
                new HistoricoJogadas(), new BuscadorDica(buscador));
    }

    @Test
    void partidaCriadaComecaSemCombinacoesEComJogadaPossivel() {
        for (int semente = 0; semente < 30; semente++) {
            Partida p = Partida.criar(nivel(1000, 20), new Random(semente));
            assertFalse(new BuscadorCombinacoes().existeCombinacao(p.getTabuleiro()));
            assertTrue(p.obterDica().isPresent());
            assertTrue(p.getEventos().estaVazia());
            assertEquals(20, p.getJogadasRestantes());
            assertEquals(0, p.getPontos());
        }
    }

    @Test
    void jogadaValidaPontuaGastaJogadaEEstabiliza() {
        Partida p = partidaComJogadaConhecida(10_000, 20);
        assertTrue(p.realizarJogada(new Jogada(new Posicao(0, 2), new Posicao(1, 2))));
        assertEquals(19, p.getJogadasRestantes());
        assertTrue(p.getPontos() >= 30);
        assertEquals(p.getPontos(), p.getPontosUltimaJogada());
        assertTrue(p.getMaiorCascataUltimaJogada() >= 1);
        assertFalse(new BuscadorCombinacoes().existeCombinacao(p.getTabuleiro()));
        assertFalse(FabricaTabuleiro.paraTexto(p.getTabuleiro()).contains("."));

        Fila<EventoTabuleiro> eventos = p.getEventos();
        assertEquals(EventoTabuleiro.Tipo.TROCA, eventos.desenfileirar().tipo());
        assertEquals(EventoTabuleiro.Tipo.REMOCAO, eventos.desenfileirar().tipo());
    }

    @Test
    void jogadaInvalidaDesfazTrocaENaoGastaJogada() {
        Partida p = partidaComJogadaConhecida(10_000, 20);
        String antes = FabricaTabuleiro.paraTexto(p.getTabuleiro());
        assertFalse(p.realizarJogada(new Jogada(new Posicao(7, 0), new Posicao(7, 1))));
        assertEquals(antes, FabricaTabuleiro.paraTexto(p.getTabuleiro()));
        assertEquals(20, p.getJogadasRestantes());
        assertEquals(0, p.getPontos());
        // A UI anima a troca e o retorno.
        assertEquals(2, p.getEventos().tamanho());
        assertFalse(p.podeDesfazer());
    }

    @Test
    void jogadaNaoAdjacenteLancaExcecao() {
        Partida p = partidaComJogadaConhecida(10_000, 20);
        assertThrows(IllegalArgumentException.class,
                () -> p.realizarJogada(new Jogada(new Posicao(0, 0), new Posicao(2, 0))));
        assertThrows(IllegalArgumentException.class,
                () -> p.realizarJogada(new Jogada(new Posicao(0, 0), new Posicao(-1, 0))));
    }

    @Test
    void desfazerRestauraTabuleiroPontosEJogadas() {
        Partida p = partidaComJogadaConhecida(10_000, 20);
        String antes = FabricaTabuleiro.paraTexto(p.getTabuleiro());
        assertTrue(p.realizarJogada(new Jogada(new Posicao(0, 2), new Posicao(1, 2))));
        assertNotEquals(antes, FabricaTabuleiro.paraTexto(p.getTabuleiro()));
        assertTrue(p.podeDesfazer());

        assertTrue(p.desfazerJogada());
        assertEquals(antes, FabricaTabuleiro.paraTexto(p.getTabuleiro()));
        assertEquals(0, p.getPontos());
        assertEquals(20, p.getJogadasRestantes());
        assertTrue(p.getEventos().estaVazia());
        assertFalse(p.desfazerJogada());
    }

    @Test
    void atingirMetaFinalizaComVitoria() {
        Partida p = partidaComJogadaConhecida(30, 20);
        p.realizarJogada(new Jogada(new Posicao(0, 2), new Posicao(1, 2)));
        assertTrue(p.venceu());
        assertTrue(p.estaFinalizada());
        assertThrows(IllegalStateException.class,
                () -> p.realizarJogada(new Jogada(new Posicao(0, 0), new Posicao(0, 1))));
    }

    @Test
    void acabarJogadasSemMetaFinalizaComDerrota() {
        Partida p = partidaComJogadaConhecida(1_000_000, 1);
        p.realizarJogada(new Jogada(new Posicao(0, 2), new Posicao(1, 2)));
        assertFalse(p.venceu());
        assertTrue(p.estaFinalizada());
        assertFalse(p.podeDesfazer());
    }

    @Test
    void variasJogadasDeDicaMantemTabuleiroConsistente() {
        Partida p = Partida.criar(nivel(1_000_000, 50), new Random(7));
        for (int i = 0; i < 50; i++) {
            Jogada jogada = p.obterDica().orElseThrow();
            assertTrue(p.realizarJogada(jogada), "dica deveria ser sempre válida");
            p.getEventos().limpar();
            assertFalse(new BuscadorCombinacoes().existeCombinacao(p.getTabuleiro()));
            assertFalse(FabricaTabuleiro.paraTexto(p.getTabuleiro()).contains("."));
        }
        assertEquals(0, p.getJogadasRestantes());
    }

    @Test
    void restaurarRecriaPartidaSalva() {
        Partida original = partidaComJogadaConhecida(10_000, 20);
        original.realizarJogada(new Jogada(new Posicao(0, 2), new Posicao(1, 2)));
        EstadoPartida estado = original.capturarEstado();

        Partida restaurada = Partida.restaurar(original.getNivel(), estado, new Random(1));
        assertEquals(original.getPontos(), restaurada.getPontos());
        assertEquals(original.getJogadasRestantes(), restaurada.getJogadasRestantes());
        assertEquals(FabricaTabuleiro.paraTexto(original.getTabuleiro()),
                FabricaTabuleiro.paraTexto(restaurada.getTabuleiro()));
        assertThrows(IllegalArgumentException.class,
                () -> Partida.restaurar(new Nivel(2, 100, 10, TIPOS), estado, new Random(1)));
    }
}
