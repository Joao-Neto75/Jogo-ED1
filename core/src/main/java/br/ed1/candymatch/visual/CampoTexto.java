package br.ed1.candymatch.visual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Campo de texto de uma linha, alimentado por {@code keyTyped}.
 * Existe porque {@code Gdx.input.getTextInput} não é suportado no backend desktop (LWJGL3).
 */
public class CampoTexto {

    private static final float PERIODO_CURSOR = 1f;
    private static final float RAIO = 10f;
    private static final char BACKSPACE = '\b';

    private final StringBuilder texto = new StringBuilder();
    private final GlyphLayout layout = new GlyphLayout();
    private final float x;
    private final float y;
    private final float largura;
    private final float altura;
    private final int tamanhoMaximo;
    private final String dica;
    private float tempo;
    private boolean ativo = true;

    public CampoTexto(float x, float y, float largura, float altura, int tamanhoMaximo, String dica) {
        if (tamanhoMaximo <= 0) {
            throw new IllegalArgumentException("Tamanho máximo deve ser positivo: " + tamanhoMaximo);
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.tamanhoMaximo = tamanhoMaximo;
        this.dica = dica == null ? "" : dica;
    }

    /** @return true se o caractere foi consumido pelo campo */
    public boolean digitar(char caractere) {
        if (!ativo) {
            return false;
        }
        if (caractere == BACKSPACE) {
            if (texto.length() > 0) {
                texto.deleteCharAt(texto.length() - 1);
            }
            return true;
        }
        boolean permitido = Character.isLetterOrDigit(caractere) || caractere == ' ' || caractere == '_'
                || caractere == '-';
        if (permitido && texto.length() < tamanhoMaximo) {
            texto.append(caractere);
            return true;
        }
        return false;
    }

    public void atualizar(float delta) {
        tempo = (tempo + delta) % PERIODO_CURSOR;
    }

    public void desenharForma(ShapeRenderer sr) {
        sr.setColor(0f, 0f, 0f, 0.35f);
        Formas.retanguloArredondado(sr, x, y, largura, altura, RAIO);
        sr.setColor(ativo ? new Color(1f, 0.85f, 0.35f, 1f) : Color.GRAY);
        Formas.retanguloArredondado(sr, x, y, largura, 3f, 1.5f);
    }

    public void desenharTexto(SpriteBatch lote, BitmapFont fonte) {
        boolean vazio = texto.length() == 0;
        boolean cursorVisivel = ativo && tempo < PERIODO_CURSOR / 2f;
        String exibido = vazio ? dica : texto + (cursorVisivel ? "|" : "");
        fonte.setColor(vazio ? new Color(1f, 1f, 1f, 0.45f) : Color.WHITE);
        layout.setText(fonte, exibido);
        fonte.draw(lote, layout, x + 16f, y + (altura + layout.height) / 2f);
        fonte.setColor(Color.WHITE);
    }

    public String getTexto() {
        return texto.toString().trim();
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void limpar() {
        texto.setLength(0);
    }
}
