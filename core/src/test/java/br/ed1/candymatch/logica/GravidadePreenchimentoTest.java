package br.ed1.candymatch.logica;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravidadePreenchimentoTest {

    private static final TipoGema[] TIPOS = {TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE};

    @Test
    void gravidadeLevaGemasParaBaixoMantendoOrdem() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VA",
                ".E",
                "A.",
                "..");
        Fila<EventoTabuleiro> eventos = new Fila<>();
        new GerenciadorGravidade().aplicar(t, eventos);
        assertEquals("..\n..\nVA\nAE", FabricaTabuleiro.paraTexto(t));

        // Coluna 0: A cai de (2,0) p/ (3,0), depois V de (0,0) p/ (2,0); coluna 1: E e depois A.
        assertEquals(4, eventos.tamanho());
        EventoTabuleiro primeiro = eventos.desenfileirar();
        assertEquals(EventoTabuleiro.Tipo.QUEDA, primeiro.tipo());
        assertEquals(new Posicao(2, 0), primeiro.origem());
        assertEquals(new Posicao(3, 0), primeiro.destino());
    }

    @Test
    void gravidadeSemVaziosNaoGeraEventos() {
        Tabuleiro t = FabricaTabuleiro.deTexto("VA", "AV");
        Fila<EventoTabuleiro> eventos = new Fila<>();
        new GerenciadorGravidade().aplicar(t, eventos);
        assertTrue(eventos.estaVazia());
    }

    @Test
    void preencherVaziosCompletaTabuleiroEGeraEventosDeCriacao() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "..",
                ".V",
                "AV");
        Fila<EventoTabuleiro> eventos = new Fila<>();
        new PreenchedorTabuleiro(TIPOS, new Random(1)).preencherVazios(t, eventos);
        assertEquals(3, eventos.tamanho());
        assertFalse(FabricaTabuleiro.paraTexto(t).contains("."));
        while (!eventos.estaVazia()) {
            assertEquals(EventoTabuleiro.Tipo.CRIACAO, eventos.desenfileirar().tipo());
        }
    }

    @Test
    void preenchimentoInicialNuncaTemCombinacoes() {
        BuscadorCombinacoes buscador = new BuscadorCombinacoes();
        for (int semente = 0; semente < 200; semente++) {
            Tabuleiro t = new Tabuleiro();
            new PreenchedorTabuleiro(TIPOS, new Random(semente)).preencherInicial(t);
            assertFalse(buscador.existeCombinacao(t), "semente " + semente + " gerou combinação:\n" + t);
            assertFalse(FabricaTabuleiro.paraTexto(t).contains("."));
        }
    }

    @Test
    void preenchimentoInicialExigeTresTipos() {
        PreenchedorTabuleiro p = new PreenchedorTabuleiro(
                new TipoGema[]{TipoGema.VERMELHA, TipoGema.AZUL}, new Random(0));
        assertThrows(IllegalStateException.class, () -> p.preencherInicial(new Tabuleiro()));
    }
}
