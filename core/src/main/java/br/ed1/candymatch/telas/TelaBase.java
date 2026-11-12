package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.visual.GrupoBotoes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Objects;

/**
 * Base comum das telas: mundo virtual de tamanho fixo (FitViewport), fundo em degradê,
 * grupo de botões e ciclo atualizar → desenhar formas → desenhar textos.
 */
public abstract class TelaBase extends ScreenAdapter {

    public static final float LARGURA_MUNDO = 800f;
    public static final float ALTURA_MUNDO = 900f;

    private static final Color FUNDO_TOPO = new Color(0.36f, 0.13f, 0.42f, 1f);
    private static final Color FUNDO_BASE = new Color(0.1f, 0.05f, 0.2f, 1f);

    protected final JogoPrincipal jogo;
    protected final FitViewport viewport;
    protected final GrupoBotoes botoes;
    private final GlyphLayout layout = new GlyphLayout();

    protected TelaBase(JogoPrincipal jogo) {
        this.jogo = Objects.requireNonNull(jogo, "jogo não pode ser nulo");
        this.viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);
        this.botoes = new GrupoBotoes(viewport, jogo.getSom());
    }

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        InputMultiplexer entrada = new InputMultiplexer(botoes);
        InputProcessor extra = criarEntradaExtra();
        if (extra != null) {
            entrada.addProcessor(extra);
        }
        Gdx.input.setInputProcessor(entrada);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }

    @Override
    public final void render(float delta) {
        // Limita o delta para uma travada (ex.: arrastar a janela) não "pular" animações inteiras.
        float deltaSeguro = Math.min(delta, 1f / 20f);
        atualizar(deltaSeguro);

        ScreenUtils.clear(FUNDO_BASE);
        viewport.apply();
        ShapeRenderer formas = jogo.getFormas();
        SpriteBatch lote = jogo.getLote();
        formas.setProjectionMatrix(viewport.getCamera().combined);
        lote.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.rect(0, 0, LARGURA_MUNDO, ALTURA_MUNDO, FUNDO_BASE, FUNDO_BASE, FUNDO_TOPO, FUNDO_TOPO);
        desenharFormas(formas);
        botoes.desenharFormas(formas);
        formas.end();

        lote.begin();
        desenharTextos(lote);
        botoes.desenharTextos(lote, jogo.getFontes().getMedia());
        lote.end();

        desenharSobreposicao(formas, lote);
    }

    /** Entrada específica da tela (teclado, tabuleiro); recebe eventos depois dos botões. */
    protected InputProcessor criarEntradaExtra() {
        return null;
    }

    protected abstract void atualizar(float delta);

    /** Chamado com o ShapeRenderer já iniciado em modo preenchido. */
    protected abstract void desenharFormas(ShapeRenderer formas);

    /** Chamado com o SpriteBatch já iniciado. */
    protected abstract void desenharTextos(SpriteBatch lote);

    /** Camada opcional desenhada por cima de tudo (begin/end por conta da tela). */
    protected void desenharSobreposicao(ShapeRenderer formas, SpriteBatch lote) {
    }

    protected void textoCentralizado(SpriteBatch lote, BitmapFont fonte, String texto, float centroX, float topo) {
        layout.setText(fonte, texto);
        fonte.draw(lote, layout, centroX - layout.width / 2f, topo);
    }
}
