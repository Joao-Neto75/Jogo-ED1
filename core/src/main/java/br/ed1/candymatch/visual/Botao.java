package br.ed1.candymatch.visual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.Objects;

/** Botão retangular simples desenhado com formas; a ação roda ao soltar o clique sobre ele. */
public class Botao {

    private static final float RAIO_CANTO = 14f;
    private static final float DESLOCAMENTO_SOMBRA = 5f;
    private static final Color COR_SOMBRA = new Color(0f, 0f, 0f, 0.3f);
    private static final Color COR_DESABILITADO = new Color(0.45f, 0.42f, 0.5f, 1f);

    private final Rectangle area;
    private final Color cor;
    private final Color corHover;
    private final Runnable acao;
    private final GlyphLayout layout = new GlyphLayout();
    private String texto;
    private boolean habilitado = true;
    private boolean visivel = true;
    private boolean sobMouse;
    private boolean pressionado;
    /** Desloca o texto para cima, deixando espaço para um subtítulo desenhado pela tela. */
    private float deslocamentoTexto;

    public Botao(String texto, float x, float y, float largura, float altura, Color cor, Runnable acao) {
        this.texto = Objects.requireNonNull(texto, "texto do botão não pode ser nulo");
        this.area = new Rectangle(x, y, largura, altura);
        this.cor = new Color(Objects.requireNonNull(cor, "cor do botão não pode ser nula"));
        this.corHover = new Color(cor).lerp(Color.WHITE, 0.2f);
        this.acao = Objects.requireNonNull(acao, "ação do botão não pode ser nula");
    }

    public void desenharForma(ShapeRenderer sr) {
        if (!visivel) {
            return;
        }
        float afundar = pressionado ? DESLOCAMENTO_SOMBRA * 0.6f : 0f;
        sr.setColor(COR_SOMBRA);
        Formas.retanguloArredondado(sr, area.x, area.y - DESLOCAMENTO_SOMBRA, area.width, area.height, RAIO_CANTO);
        sr.setColor(!habilitado ? COR_DESABILITADO : sobMouse ? corHover : cor);
        Formas.retanguloArredondado(sr, area.x, area.y - afundar, area.width, area.height, RAIO_CANTO);
        sr.setColor(1f, 1f, 1f, habilitado ? 0.18f : 0.08f);
        Formas.retanguloArredondado(sr, area.x + 6, area.y + area.height * 0.55f - afundar, area.width - 12,
                area.height * 0.38f, RAIO_CANTO * 0.7f);
    }

    public void desenharTexto(SpriteBatch lote, BitmapFont fonte) {
        if (!visivel) {
            return;
        }
        float afundar = pressionado ? DESLOCAMENTO_SOMBRA * 0.6f : 0f;
        fonte.setColor(habilitado ? Color.WHITE : Color.LIGHT_GRAY);
        layout.setText(fonte, texto);
        fonte.draw(lote, layout, area.x + (area.width - layout.width) / 2f,
                area.y + (area.height + layout.height) / 2f + deslocamentoTexto - afundar);
        fonte.setColor(Color.WHITE);
    }

    public boolean contem(float x, float y) {
        return visivel && area.contains(x, y);
    }

    void setSobMouse(boolean sobMouse) {
        this.sobMouse = sobMouse && habilitado;
    }

    void setPressionado(boolean pressionado) {
        this.pressionado = pressionado && habilitado;
    }

    /** @return true se a ação foi executada */
    boolean acionar() {
        if (!visivel || !habilitado) {
            return false;
        }
        acao.run();
        return true;
    }

    public void setTexto(String texto) {
        this.texto = Objects.requireNonNull(texto, "texto do botão não pode ser nulo");
    }

    public void setDeslocamentoTexto(float deslocamentoTexto) {
        this.deslocamentoTexto = deslocamentoTexto;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }
}
