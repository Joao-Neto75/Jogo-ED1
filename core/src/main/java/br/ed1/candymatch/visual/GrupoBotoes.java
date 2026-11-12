package br.ed1.candymatch.visual;

import br.ed1.candymatch.estruturas.ListaEncadeada;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Objects;

/**
 * Conjunto de botões de uma tela: trata mouse (hover, clique) e desenha todos de uma vez.
 * Usa a {@link ListaEncadeada} própria porque a quantidade de botões varia por tela.
 */
public class GrupoBotoes extends InputAdapter {

    private final ListaEncadeada<Botao> botoes = new ListaEncadeada<>();
    private final Viewport viewport;
    private final GerenciadorSom som;
    private final Vector2 auxiliar = new Vector2();
    private Botao pressionado;

    public GrupoBotoes(Viewport viewport, GerenciadorSom som) {
        this.viewport = Objects.requireNonNull(viewport, "viewport não pode ser nulo");
        this.som = Objects.requireNonNull(som, "gerenciador de som não pode ser nulo");
    }

    public Botao adicionar(Botao botao) {
        botoes.adicionar(Objects.requireNonNull(botao, "botão não pode ser nulo"));
        return botao;
    }

    public void desenharFormas(ShapeRenderer sr) {
        for (Botao botao : botoes) {
            botao.desenharForma(sr);
        }
    }

    public void desenharTextos(SpriteBatch lote, BitmapFont fonte) {
        for (Botao botao : botoes) {
            botao.desenharTexto(lote, fonte);
        }
    }

    @Override
    public boolean mouseMoved(int telaX, int telaY) {
        Vector2 mundo = paraMundo(telaX, telaY);
        for (Botao botao : botoes) {
            botao.setSobMouse(botao.contem(mundo.x, mundo.y));
        }
        return false;
    }

    @Override
    public boolean touchDown(int telaX, int telaY, int ponteiro, int botaoMouse) {
        Botao alvo = botaoEm(telaX, telaY);
        if (alvo == null || !alvo.isHabilitado()) {
            return false;
        }
        pressionado = alvo;
        alvo.setPressionado(true);
        return true;
    }

    @Override
    public boolean touchUp(int telaX, int telaY, int ponteiro, int botaoMouse) {
        if (pressionado == null) {
            return false;
        }
        Botao solto = pressionado;
        solto.setPressionado(false);
        pressionado = null;
        if (solto == botaoEm(telaX, telaY)) {
            som.tocar(GerenciadorSom.Som.CLIQUE);
            solto.acionar();
        }
        return true;
    }

    private Botao botaoEm(int telaX, int telaY) {
        Vector2 mundo = paraMundo(telaX, telaY);
        for (Botao botao : botoes) {
            if (botao.contem(mundo.x, mundo.y)) {
                return botao;
            }
        }
        return null;
    }

    private Vector2 paraMundo(int telaX, int telaY) {
        return viewport.unproject(auxiliar.set(telaX, telaY));
    }
}
