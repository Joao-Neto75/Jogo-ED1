package br.ed1.candymatch.visual;

import br.ed1.candymatch.logica.Partida;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Objects;

/** Painel superior da partida: nível, pontos, barra de progresso até a meta, jogadas restantes e mensagens. */
public class Hud {

    private static final float DURACAO_MENSAGEM = 1.6f;
    private static final float ALTURA_BARRA = 16f;
    private static final Color COR_PAINEL = new Color(0.08f, 0.04f, 0.14f, 0.85f);
    private static final Color COR_BARRA_FUNDO = new Color(1f, 1f, 1f, 0.12f);
    private static final Color COR_BARRA = new Color(1f, 0.45f, 0.7f, 1f);
    private static final Color COR_BARRA_COMPLETA = new Color(0.35f, 0.9f, 0.45f, 1f);
    private static final Color COR_JOGADAS_POUCAS = new Color(1f, 0.4f, 0.4f, 1f);
    private static final int JOGADAS_POUCAS = 3;

    private final float x;
    private final float y;
    private final float largura;
    private final float altura;
    private final GlyphLayout layout = new GlyphLayout();
    private String mensagem = "";
    private float tempoMensagem;
    /** Pontos exibidos sobem gradualmente até o valor real, para o ganho ficar visível. */
    private float pontosExibidos;

    public Hud(float x, float y, float largura, float altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public void mostrarMensagem(String texto) {
        mensagem = Objects.requireNonNull(texto, "mensagem não pode ser nula");
        tempoMensagem = DURACAO_MENSAGEM;
    }

    public void atualizar(float delta, int pontosReais) {
        tempoMensagem = Math.max(0f, tempoMensagem - delta);
        float diferenca = pontosReais - pontosExibidos;
        // Aproximação exponencial: rápida para ganhos grandes, mas sempre visível.
        pontosExibidos += Math.abs(diferenca) < 1f ? diferenca : diferenca * Math.min(1f, delta * 6f);
    }

    /** Usado ao desfazer/restaurar, para não animar a pontuação "para trás". */
    public void sincronizarPontos(int pontos) {
        pontosExibidos = pontos;
    }

    public void desenharFormas(ShapeRenderer sr, Partida partida) {
        sr.setColor(COR_PAINEL);
        Formas.retanguloArredondado(sr, x, y, largura, altura, 20f);
        float progresso = Math.min(1f, pontosExibidos / partida.getNivel().getMetaPontos());
        float larguraBarra = largura - 40f;
        sr.setColor(COR_BARRA_FUNDO);
        Formas.retanguloArredondado(sr, x + 20f, y + 16f, larguraBarra, ALTURA_BARRA, ALTURA_BARRA / 2f);
        if (progresso > 0f) {
            sr.setColor(progresso >= 1f ? COR_BARRA_COMPLETA : COR_BARRA);
            Formas.retanguloArredondado(sr, x + 20f, y + 16f, Math.max(ALTURA_BARRA, larguraBarra * progresso),
                    ALTURA_BARRA, ALTURA_BARRA / 2f);
        }
    }

    public void desenharTextos(SpriteBatch lote, Fontes fontes, Partida partida) {
        BitmapFont pequena = fontes.getPequena();
        BitmapFont grande = fontes.getGrande();
        float linhaRotulo = y + altura - 14f;
        float linhaValor = y + altura - 44f;

        pequena.setColor(0.85f, 0.8f, 0.95f, 1f);
        pequena.draw(lote, "NÍVEL", x + 24f, linhaRotulo);
        centralizar(lote, pequena, "PONTOS / META", x + largura / 2f, linhaRotulo);
        alinharDireita(lote, pequena, "JOGADAS", x + largura - 24f, linhaRotulo);
        pequena.setColor(Color.WHITE);

        grande.draw(lote, String.valueOf(partida.getNivel().getNumero()), x + 24f, linhaValor);
        centralizar(lote, grande, Math.round(pontosExibidos) + " / " + partida.getNivel().getMetaPontos(),
                x + largura / 2f, linhaValor);
        if (partida.getJogadasRestantes() <= JOGADAS_POUCAS) {
            grande.setColor(COR_JOGADAS_POUCAS);
        }
        alinharDireita(lote, grande, String.valueOf(partida.getJogadasRestantes()), x + largura - 24f, linhaValor);
        grande.setColor(Color.WHITE);
    }

    /** Mensagem flutuante (ex.: "Cascata x3!") desenhada sobre o tabuleiro. */
    public void desenharMensagem(SpriteBatch lote, Fontes fontes, float centroX, float centroY) {
        if (tempoMensagem <= 0f) {
            return;
        }
        BitmapFont fonte = fontes.getTitulo();
        float alfa = Math.min(1f, tempoMensagem / (DURACAO_MENSAGEM * 0.4f));
        float subida = (DURACAO_MENSAGEM - tempoMensagem) * 30f;
        fonte.setColor(1f, 0.92f, 0.4f, alfa);
        centralizar(lote, fonte, mensagem, centroX, centroY + subida);
        fonte.setColor(Color.WHITE);
    }

    private void centralizar(SpriteBatch lote, BitmapFont fonte, String texto, float centroX, float topo) {
        layout.setText(fonte, texto);
        fonte.draw(lote, layout, centroX - layout.width / 2f, topo);
    }

    private void alinharDireita(SpriteBatch lote, BitmapFont fonte, String texto, float direita, float topo) {
        layout.setText(fonte, texto);
        fonte.draw(lote, layout, direita - layout.width, topo);
    }
}
