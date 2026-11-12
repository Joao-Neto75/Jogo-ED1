package br.ed1.candymatch.logica;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.Combinacao;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuscadorCombinacoesTest {

    private final BuscadorCombinacoes buscador = new BuscadorCombinacoes();

    @Test
    void tabuleiroSemCombinacaoRetornaListaVazia() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VAV",
                "AVA",
                "VAV");
        assertTrue(buscador.buscar(t).estaVazia());
        assertFalse(buscador.existeCombinacao(t));
    }

    @Test
    void encontraCombinacaoHorizontalNoFimDaLinha() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "AVVV",
                "VAEA");
        ListaEncadeada<Combinacao> resultado = buscador.buscar(t);
        assertEquals(1, resultado.tamanho());
        Combinacao c = resultado.obter(0);
        assertEquals(TipoGema.VERMELHA, c.getTipo());
        assertEquals(3, c.tamanho());
        assertTrue(c.getPosicoes().contem(new Posicao(0, 1)));
        assertTrue(c.getPosicoes().contem(new Posicao(0, 3)));
    }

    @Test
    void encontraCombinacaoVerticalDeQuatro() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "EA",
                "EV",
                "EA",
                "EV");
        ListaEncadeada<Combinacao> resultado = buscador.buscar(t);
        assertEquals(1, resultado.tamanho());
        assertEquals(4, resultado.obter(0).tamanho());
        assertEquals(TipoGema.VERDE, resultado.obter(0).getTipo());
    }

    @Test
    void formatoEmLGeraDuasCombinacoes() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "MAV",
                "MVA",
                "MMM");
        assertEquals(2, buscador.buscar(t).tamanho());
    }

    @Test
    void celulasVaziasInterrompemSequencia() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VV.V",
                "....");
        assertTrue(buscador.buscar(t).estaVazia());
    }

    @Test
    void tabuleiroNuloLancaExcecao() {
        assertThrows(NullPointerException.class, () -> buscador.buscar(null));
    }
}
